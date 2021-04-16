package fmendez.padron;

import lombok.SneakyThrows;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.io.elasticsearch.ElasticsearchIO;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.values.TypeDescriptors;

/**
 * Imports a CSV electorate information of Costa Rica.
 */
public class Importer {

  /** Specific pipeline options. */
  public interface Options extends PipelineOptions {
    @Description("Input Path")
    String getInput();

    void setInput(String value);

    @Description("Elasticsearch server")
    String getEsHosts();

    void setEsHosts(String value);

    @Description("Elasticsearch index")
    String getEsIndex();

    void setEsIndex(String value);

    @Description("Elasticsearch index")
    Boolean isSchemaless();

    void setSchemaless(Boolean value);
  }

  /**
   * Beam transformation, reads a CSV and transforms it into JSON.
   */
  static class LineToJson implements SerializableFunction<String, String> {

    public String apply(String line) {
      String[] fields = line.split(",");
      String json = "{\"cedula\":\"" + fields[0] + "\","
             + "\"codelec\":\"" + fields[1] + "\","
             + "\"fecha_caducidad\":\"" + fields[3] + "\","
             + "\"junta\":\"" + fields[4] + "\","
             + "\"nombre\":\"" + fields[5].trim() + "\","
             + "\"apellido1\":\"" + fields[6].trim() + "\","
             + "\"apellido2\":\"" + fields[7].trim() + "\","
             + "\"nombre_completo\":\"" + fields[5].trim() + ' ' + fields[6].trim() + ' ' + fields[7].trim()
             + "\"}";
      return json;
    }
  }

  /**
   * Creates the electoral roll index.
   */
  @SneakyThrows
  private static void createElectoralRollIndex(String[] esHosts, String esIndex) {
    try (EsClientUtil esClientUtil = new EsClientUtil(esHosts)) {
      esClientUtil.createIndex(esIndex,
                               Importer.class.getClassLoader().getResourceAsStream("padron-es-settings.json"),
                               Importer.class.getClassLoader().getResourceAsStream("padron-es-mapping.json"));
    }
  }

  /**
   * Use --input=pathToFile --esHosts=http://elastisearch-host:9200/ --esIndex=indeName
   */
  public static void main(String[] args) {

    //Read command-line arguments
    Options options = PipelineOptionsFactory.fromArgs(args).withValidation().as(Options.class);
    final String inputPath = options.getInput();
    final String[] esHosts = options.getEsHosts().split(",");
    final String esIndex = options.getEsIndex();
    final boolean schemaless = options.isSchemaless();

    if (!schemaless) {
      //Create Elasticsearch index
      createElectoralRollIndex(esHosts, esIndex);
    }

    Pipeline pipeline = Pipeline.create(options);

    pipeline
      //Read Input file
      .apply(TextIO.read().from(inputPath))

      //Transform lines to JSON
      .apply(MapElements
               .into(TypeDescriptors.strings())
               .via(new LineToJson()))

      //Write to Elasticsearch
      .apply(ElasticsearchIO.write()
               .withConnectionConfiguration(ElasticsearchIO.ConnectionConfiguration
                                              .create(esHosts, esIndex, "_doc"))
               .withIdFn(node -> node.get("cedula").asText()));

    pipeline.run().waitUntilFinish();

  }
}
