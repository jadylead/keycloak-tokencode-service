package net.interus.keycloak.tokencode.integrated.firebase;

import java.io.IOException;

public interface DynamicLinksApi {
  String postShortLink(String longDynamicLink) throws IOException;
}
