package net.interus.keycloak.tokencode.delegation;

import net.interus.keycloak.tokencode.exception.ValidatingFailure;
import net.interus.keycloak.tokencode.jpa.representations.TokenCodeRepresentation;

/**
 * SMS, Voice, APP
 */
public interface ValidatingDelegator {
  boolean onValidating(TokenCodeRepresentation tokenCode, String code) throws ValidatingFailure;
}
