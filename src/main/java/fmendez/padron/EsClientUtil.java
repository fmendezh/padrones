package fmendez.padron;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import lombok.SneakyThrows;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;

/**
 * Elasticsearch client wrapper for convenient tasks.
 */
public class EsClientUtil implements AutoCloseable {

  private RestHighLevelClient restHighLevelClient;

  public EsClientUtil(String[] hosts) {
    HttpHost[] esHosts = Arrays.stream(hosts)
                          .map(HttpHost::create)
                          .toArray(HttpHost[]::new);

    restHighLevelClient = new RestHighLevelClient(RestClient.builder(esHosts));
  }

  /**
   * Reads the entire content of a file into a String.
   */
  private static String fileAsString(InputStream inputStream) {
    try {
      BufferedInputStream bis = new BufferedInputStream(inputStream);
      ByteArrayOutputStream buf = new ByteArrayOutputStream();
      for (int result = bis.read(); result != -1; result = bis.read()) {
        buf.write((byte) result);
      }
      return buf.toString(StandardCharsets.UTF_8.name());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Creates an Elasticsearch index using a settings and mapping file.
   */
  public void createIndex(String indexName, InputStream settingsFile, InputStream mappingFile) throws IOException {
    CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);
    createIndexRequest.settings(fileAsString(settingsFile), XContentType.JSON);
    createIndexRequest.mapping(fileAsString(mappingFile), XContentType.JSON);
    restHighLevelClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
  }

  @Override
  @SneakyThrows
  public void close() {
    restHighLevelClient.close();
  }
}
