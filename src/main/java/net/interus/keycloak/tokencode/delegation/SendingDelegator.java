package net.interus.keycloak.tokencode.delegation;

import net.interus.keycloak.tokencode.exception.SendingFailure;
import net.interus.keycloak.tokencode.jpa.representations.TokenCodeRepresentation;
import net.interus.keycloak.tokencode.relay.Message;

/**
 * SMS, Voice, APP
 */
public interface SendingDelegator {
  boolean onSending(TokenCodeRepresentation tokenCode, Message message) throws SendingFailure;
}
