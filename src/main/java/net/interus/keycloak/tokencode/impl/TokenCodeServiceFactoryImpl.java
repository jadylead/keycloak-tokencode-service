package net.interus.keycloak.tokencode.impl;

import net.interus.keycloak.tokencode.TokenCodeProperties;
import net.interus.keycloak.tokencode.TokenCodeService;
import net.interus.keycloak.tokencode.TokenCodeServiceFactory;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class TokenCodeServiceFactoryImpl implements TokenCodeServiceFactory {
  public static final String ID = "default-token-code-provider";

  protected Config.Scope config;

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public TokenCodeService create(KeycloakSession session) {
    return new TokenCodeServiceImpl(session, TokenCodeProperties.valueOf(config));
  }

  @Override
  public void init(Config.Scope config) {
    this.config = config;
  }

  @Override
  public void postInit(KeycloakSessionFactory keycloakSessionFactory) {
  }

  @Override
  public void close() {
  }
}
