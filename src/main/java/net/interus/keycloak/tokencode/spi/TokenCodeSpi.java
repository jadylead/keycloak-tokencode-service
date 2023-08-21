package net.interus.keycloak.tokencode.spi;

import net.interus.keycloak.tokencode.TokenCodeService;
import net.interus.keycloak.tokencode.TokenCodeServiceFactory;
import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

public class TokenCodeSpi implements Spi {
  @Override
  public boolean isInternal() {
    return false;
  }

  @Override
  public String getName() {
    return "token-code";
  }

  @Override
  public Class<? extends Provider> getProviderClass() {
    return TokenCodeService.class;
  }

  @Override
  @SuppressWarnings("rawtypes")
  public Class<? extends ProviderFactory> getProviderFactoryClass() {
    return TokenCodeServiceFactory.class;
  }
}