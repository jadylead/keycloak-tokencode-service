package net.interus.keycloak.tokencode.integrated.firebase;

import lombok.Builder;
import org.keycloak.Config;

@Builder
public class DynamicLinksProperties {
  public static final String DYNAMIC_LINKS_API_HOST = "FIREBASE_DYNAMIC_LINKS_API_HOST";
  public static final String DYNAMIC_LINKS_API_VERSION = "FIREBASE_DYNAMIC_LINKS_API_VERSION";
  public static final String DYNAMIC_LINKS_API_KEY = "FIREBASE_DYNAMIC_LINKS_API_KEY";

  static String defaultApiHost = System.getenv(DYNAMIC_LINKS_API_HOST);
  static String defaultApiVersion = System.getenv(DYNAMIC_LINKS_API_VERSION);
  static String defaultApiKey = System.getenv(DYNAMIC_LINKS_API_KEY);

  String apiHost;
  String apiVersion;
  String apiKey;

  public static DynamicLinksProperties valueOf(Config.Scope config) {
    return DynamicLinksProperties.builder()
        .apiHost(config.get(DYNAMIC_LINKS_API_HOST, defaultApiHost))
        .apiVersion(config.get(DYNAMIC_LINKS_API_VERSION, defaultApiVersion))
        .apiKey(config.get(DYNAMIC_LINKS_API_KEY, defaultApiKey))
        .build();
  }

  public static DynamicLinksProperties defaultValueOf() {
    return DynamicLinksProperties.builder()
        .apiHost(defaultApiHost)
        .apiVersion(defaultApiVersion)
        .apiKey(defaultApiKey)
        .build();
  }
}
