package net.interus.keycloak.tokencode.jpa.representations;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.interus.keycloak.tokencode.TokenCodeType;
import net.interus.keycloak.tokencode.relay.MediaType;
import org.keycloak.models.utils.KeycloakModelUtils;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenCodeRepresentation {
  private String id;
  private String realmId;
  private String uri;
  private String fromAddress;
  private String toAddress;
  private String code;
  private String mediaType;
  private String type;
  private String credentialType;
  private String credentialData;
  private String secretData;
  private Date createdAt;
  private Date expiresAt;
  private Boolean confirmed;
  private String byWhom;

  public static TokenCodeRepresentation valueOf(String realmId, String uri, String toAddress, TokenCodeType type,
                                                MediaType mediaType, String credentialType, String credentialData) {

    TokenCodeRepresentation tokenCode = new TokenCodeRepresentation();
    tokenCode.id = KeycloakModelUtils.generateId();
    tokenCode.realmId = realmId;
    tokenCode.uri = uri;
    tokenCode.toAddress = toAddress;
    tokenCode.code = (type == TokenCodeType.OTP_SAFE ? generateSafeToken(): generateTokenCode()) ;
    tokenCode.type = type.name();
    tokenCode.mediaType = mediaType.name();
    tokenCode.credentialType = credentialType;
    tokenCode.credentialData = credentialData;
    tokenCode.confirmed = false;
    return tokenCode;
  }

  private static String generateTokenCode() {
    SecureRandom secureRandom = new SecureRandom();
    Integer code = secureRandom.nextInt(999_999);
    return String.format("%06d", code);
  }

  private static String generateSafeToken() {
    SecureRandom random = new SecureRandom();
    byte bytes[] = new byte[20];
    random.nextBytes(bytes);
    Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
    String token = encoder.encodeToString(bytes);
    return token;
  }

  public void setSecretData(String secretData) {
    this.secretData = secretData;
  }
}
