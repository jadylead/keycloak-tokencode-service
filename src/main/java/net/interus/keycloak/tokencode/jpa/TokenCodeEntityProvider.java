package net.interus.keycloak.tokencode.jpa;

import org.keycloak.connections.jpa.entityprovider.JpaEntityProvider;

import java.util.Collections;
import java.util.List;

public class TokenCodeEntityProvider implements JpaEntityProvider {
  @Override
  public String getFactoryId() {
    return TokenCodeEntityProviderFactory.ID;
  }

  @Override
  public List<Class<?>> getEntities() {
    return Collections.singletonList(TokenCodeEntity.class);
  }

  @Override
  public String getChangelogLocation() {
    return "META-INF/changelog/token-code-changelog-6.0.xml";
  }

  @Override
  public void close() {
  }
}