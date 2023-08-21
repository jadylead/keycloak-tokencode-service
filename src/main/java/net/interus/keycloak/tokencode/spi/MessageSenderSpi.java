package net.interus.keycloak.tokencode.spi;

import net.interus.keycloak.tokencode.relay.MessageSender;
import net.interus.keycloak.tokencode.relay.MessageSenderFactory;
import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

public class MessageSenderSpi implements Spi {
  @Override
  public boolean isInternal() {
    return false;
  }

  @Override
  public String getName() {
    return "message-sender";
  }

  @Override
  public Class<? extends Provider> getProviderClass() {
    return MessageSender.class;
  }

  @Override
  @SuppressWarnings("rawtypes")
  public Class<? extends ProviderFactory> getProviderFactoryClass() {
    return MessageSenderFactory.class;
  }
}