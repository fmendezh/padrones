package fmendez.padron.imports;

import java.io.FileInputStream;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;
import fmendez.padron.dict.DistritosElectoralesDictionary;
import fmendez.padron.elastic.EsClientUtil;
import fmendez.padron.model.Elector;
import fmendez.padron.transforms.LineToElector;
import lombok.SneakyThrows;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Imports a CSV electorate information of Costa Rica.
 */
public class Importer {

  private static final Logger LOG = LoggerFactory.getLogger(Importer.class);

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @SneakyThrows
  private static IndexRequest toIndexRequest(Elector elector, String esIndex) {
    return new IndexRequest()
            .index(esIndex)
            .id(elector.getCedula())
            .opType(DocWriteRequest.OpType.INDEX)
            .source(MAPPER.writeValueAsString(elector), XContentType.JSON);
  }

  @SneakyThrows
  private static void javaImport(Options options) {
    String[] esHosts = options.getEsHosts().split(",");
    String esIndex = options.getEsIndex();

    DistritosElectoralesDictionary
      electoralDictionary = new DistritosElectoralesDictionary(options.getArchivoDistritosElectorales());

    LineToElector lineToElector = new LineToElector(electoralDictionary);
    try(Scanner scanner = new Scanner(new FileInputStream(options.getArchivoPadron()));
        EsClientUtil esClient = new EsClientUtil(esHosts)) {

      if (!options.isSchemaless()) {
        //Create Elasticsearch index
        esClient.createElectoralRollIndex(esIndex);
      }

      BulkRequest bulkRequest = new BulkRequest();
      int bulkCount = 0;
      int recordCount = 0;
      while (scanner.hasNext()) {
        Elector elector = lineToElector.apply(scanner.nextLine());
        bulkRequest.add(toIndexRequest(elector, esIndex));

        bulkCount +=1;
        recordCount +=1;
        if (bulkCount >= options.getEsBulkSize() || !scanner.hasNext()) {
          LOG.info("Records indexed so far {}", recordCount);
          esClient.bulk(bulkRequest);
          bulkRequest = new BulkRequest();
          bulkCount = 0;
        }
      }
      esClient.updateSearchSettings(esIndex);
    }
  }

  /**
   * Use --input=pathToFile --esHosts=http://elastisearch-host:9200/ --esIndex=indeName
   */
  public static void main(String[] args) {
    long start = System.currentTimeMillis();

    //Read command-line arguments
    Options options = PipelineOptionsFactory.fromArgs(args).withValidation().as(Options.class);

    LOG.info("Import started");

    javaImport(options);

    LOG.info("Import finished in {} seconds", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - start));
  }
}
