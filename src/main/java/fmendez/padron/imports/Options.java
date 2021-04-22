package fmendez.padron.imports;

import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;

/** Specific pipeline options. */
public interface Options extends PipelineOptions {
  @Description("Electoral roll Input File")
  String getArchivoPadron();

  void setArchivoPadron(String value);

  @Description("Electoral districts Input File")
  String getArchivoDistritosElectorales();

  void setArchivoDistritosElectorales(String value);

  @Description("Elasticsearch server")
  String getEsHosts();

  void setEsHosts(String value);

  @Description("Elasticsearch index")
  String getEsIndex();

  void setEsIndex(String value);

  @Description("Use Elasticsearch schema")
  Boolean isSchemaless();

  void setSchemaless(Boolean value);

  @Description("Elastisearch bulk size request")
  @Default.Integer(1_000)
  Integer getEsBulkSize();

  void setEsBulkSize(Integer value);
}
