package net.interus.keycloak.tokencode;

import net.interus.keycloak.tokencode.relay.MediaType;
import net.interus.keycloak.tokencode.delegation.SendingDelegator;
import net.interus.keycloak.tokencode.delegation.ValidatingDelegator;
import net.interus.keycloak.tokencode.exception.SendingFailure;
import net.interus.keycloak.tokencode.exception.ValidatingFailure;
import net.interus.keycloak.tokencode.jpa.representations.TokenCodeRepresentation;
import net.interus.keycloak.tokencode.relay.Message;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.provider.Provider;

public interface TokenCodeService extends Provider {
  boolean sendMessage(String toAddress, MediaType mediaType, Message message) throws SendingFailure;

  int sendCode(String uri, TokenCodeType type, String credentialType, String credentialData) throws SendingFailure;
  int sendCode(String uri, TokenCodeType type, String credentialType, String credentialData, SendingDelegator delegator) throws SendingFailure;
  int sendCode(String uri, String toAddress, TokenCodeType type, String credentialType, String credentialData) throws SendingFailure;
  int sendCode(String uri, String toAddress, TokenCodeType type, MediaType mediaType, String credentialType, String credentialData, Message message) throws SendingFailure;
  int sendCode(String uri, String toAddress, TokenCodeType type, MediaType mediaType, String credentialType, String credentialData, Message message, Integer expiresIn) throws SendingFailure;

  boolean validateCode(String uri, String code, TokenCodeType tokenCodeType, String credentialType, String credentialSecret, UserModel user) throws ValidatingFailure;
  boolean validateCode(String uri, String code, TokenCodeType tokenCodeType, String credentialType, String credentialSecret, UserModel user, ValidatingDelegator delegator) throws ValidatingFailure;

  void setOnUserConfirmedListener(OnUserConfirmedListener listener);
  interface OnUserConfirmedListener {
    void onUserConfirmed(KeycloakSession session, TokenCodeRepresentation tokenCode, UserModel user);
  }
}
