package fmendez.padron.elastic;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

import fmendez.padron.imports.Importer;
import lombok.SneakyThrows;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.common.settings.Settings;
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

  public RestHighLevelClient getRestHighLevelClient() {
    return restHighLevelClient;
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
  public void createIndex(String indexName, InputStream settingsFile, InputStream mappingFile, Map<String,String> indexingSettings) throws IOException {
    CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);

    Settings.Builder settingsBuilder = Settings.builder();
    indexingSettings.forEach(settingsBuilder::put);
    settingsBuilder.loadFromSource(fileAsString(settingsFile), XContentType.JSON);

    createIndexRequest.settings(settingsBuilder);
    createIndexRequest.mapping(fileAsString(mappingFile), XContentType.JSON);
    restHighLevelClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
  }

  @SneakyThrows
  public void updateSettings(String indexName, Map<String,String> settings) {
    UpdateSettingsRequest updateSettingsRequest = new UpdateSettingsRequest();
    updateSettingsRequest.indices(indexName);
    updateSettingsRequest.settings(settings);
    restHighLevelClient.indices().putSettings(updateSettingsRequest, RequestOptions.DEFAULT);
  }

  /**
   * Creates the electoral roll index.
   */
  @SneakyThrows
  public void createElectoralRollIndex(String esIndex) {
    createIndex(esIndex,
               Importer.class.getClassLoader().getResourceAsStream("padron-es-settings.json"),
               Importer.class.getClassLoader().getResourceAsStream("padron-es-mapping.json"),
               IndexingConstants.DEFAULT_INDEXING_SETTINGS);
  }

  /**
   * Sets the indexing settings after creating the index.
   */
  public void updateSearchSettings(String esIndex) {
    updateSettings(esIndex, IndexingConstants.DEFAULT_SEARCH_SETTINGS);
  }

  /**
   * Sets the indexing settings after creating the index.
   */
  @SneakyThrows
  public BulkResponse bulk(BulkRequest bulkRequest) {
    return restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
  }

  @Override
  @SneakyThrows
  public void close() {
    restHighLevelClient.close();
  }
}
