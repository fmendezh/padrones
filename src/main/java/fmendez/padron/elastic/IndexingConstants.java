package fmendez.padron.elastic;

import java.util.HashMap;
import java.util.Map;

import lombok.experimental.UtilityClass;

/** Constants used for indexing into Elastisearch. */
@UtilityClass
public class IndexingConstants {

  /** Default/Recommended indexing settings. */
  public static final Map<String, String> DEFAULT_INDEXING_SETTINGS = new HashMap<>();

  static {
    DEFAULT_INDEXING_SETTINGS.put("index.refresh_interval", "-1");
    DEFAULT_INDEXING_SETTINGS.put("index.number_of_shards", "1");
    DEFAULT_INDEXING_SETTINGS.put("index.number_of_replicas", "0");
    DEFAULT_INDEXING_SETTINGS.put("index.translog.durability", "async");
  }

  /** Default/recommended setting for search/production mode. */
  public static final Map<String, String> DEFAULT_SEARCH_SETTINGS = new HashMap<>();

  static {
    DEFAULT_SEARCH_SETTINGS.put("index.refresh_interval", "1s");
    DEFAULT_SEARCH_SETTINGS.put("index.number_of_replicas", "1");
  }
}
