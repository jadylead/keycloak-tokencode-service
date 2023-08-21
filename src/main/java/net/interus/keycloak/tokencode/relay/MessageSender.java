package net.interus.keycloak.tokencode.relay;

import net.interus.keycloak.tokencode.exception.SendingFailure;
import org.keycloak.models.UserModel;
import org.keycloak.provider.Provider;

import java.util.List;

public interface MessageSender extends Provider {
  boolean process(String toAddress, Message message) throws SendingFailure;
  int processes(List<String> toAddress, Message message) throws SendingFailure;
}
