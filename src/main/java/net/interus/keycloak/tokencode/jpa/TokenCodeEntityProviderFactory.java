package net.interus.keycloak.tokencode.jpa;

import org.keycloak.Config;
import org.keycloak.connections.jpa.entityprovider.JpaEntityProvider;
import org.keycloak.connections.jpa.entityprovider.JpaEntityProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class TokenCodeEntityProviderFactory implements JpaEntityProviderFactory {
  public static final String ID = "token-code-entity-provider";
  @Override
  public String getId() {
    return ID;
  }

  @Override
  public JpaEntityProvider create(KeycloakSession session) {
    return new TokenCodeEntityProvider();
  }

  @Override
  public void init(Config.Scope config) {
  }

  @Override
  public void postInit(KeycloakSessionFactory factory) {
  }

  @Override
  public void close() {
  }
}
