package net.interus.keycloak.tokencode.util;

import net.interus.keycloak.tokencode.integrated.firebase.DynamicLinksApi;
import net.interus.keycloak.tokencode.integrated.firebase.DynamicLinksRestApi;

import java.io.IOException;

public class URLShorter {
  public static String shorten(String url) throws IOException {
    DynamicLinksApi api = new DynamicLinksRestApi();
    return api.postShortLink(url);
  }
}
