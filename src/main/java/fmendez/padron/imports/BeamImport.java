package fmendez.padron.imports;

import com.fasterxml.jackson.databind.ObjectMapper;
import fmendez.padron.dict.DistritosElectoralesDictionary;
import fmendez.padron.elastic.EsClientUtil;
import fmendez.padron.model.Elector;
import fmendez.padron.transforms.LineToElector;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.io.elasticsearch.ElasticsearchIO;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.values.TypeDescriptors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BeamImport {

  private static final Logger LOG = LoggerFactory.getLogger(BeamImport.class);

  /**
   * Beam transformation, reads a CSV and transforms it into JSON.
   */
  @RequiredArgsConstructor
  static class LineToJson implements SerializableFunction<String, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final SerializableFunction<String, Elector> toElectorFn;

    @SneakyThrows
    public String apply(String line) {
      return MAPPER.writeValueAsString(toElectorFn.apply(line));
    }
  }

  private static void beamImport(Options options) {
    final String electoralRollFile = options.getArchivoPadron();
    final String electoralDistrictsFile = options.getArchivoDistritosElectorales();
    final String[] esHosts = options.getEsHosts().split(",");
    final String esIndex = options.getEsIndex();
    final boolean schemaless = options.isSchemaless();

    final DistritosElectoralesDictionary
      electoralDictionary = new DistritosElectoralesDictionary(electoralDistrictsFile);

    try(EsClientUtil esClient = new EsClientUtil(esHosts)) {

      if (!schemaless) {
        //Create Elasticsearch index
        esClient.createElectoralRollIndex(esIndex);
      }

      Pipeline pipeline = Pipeline.create(options);

      pipeline
        //Read Input file
        .apply(TextIO.read().from(electoralRollFile))

        //Transform lines to JSON
        .apply(MapElements.into(TypeDescriptors.strings()).via(new LineToJson(new LineToElector(electoralDictionary))))

        //Write to Elasticsearch
        .apply(ElasticsearchIO.write()
                 .withConnectionConfiguration(ElasticsearchIO.ConnectionConfiguration.create(esHosts, esIndex, "_doc"))
                 .withIdFn(node -> node.get("cedula").asText()));

      pipeline.run().waitUntilFinish();

      esClient.updateSearchSettings(esIndex);
    }
  }

  /**
   * Use --input=pathToFile --esHosts=http://elastisearch-host:9200/ --esIndex=indeName
   */
  public static void main(String[] args) {

    //Read command-line arguments
    Options options = PipelineOptionsFactory.fromArgs(args).withValidation().as(Options.class);


    LOG.info("Beam import started");

    beamImport(options);

    LOG.info("Beam import finished");
  }
}
