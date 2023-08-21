package net.interus.keycloak.tokencode;

import lombok.Builder;
import lombok.Data;
import org.keycloak.Config;

@Builder
@Data
public class TokenCodeProperties {
  public static final String TOKEN_SMS_SENDER_ID = "TOKEN_SMS_SENDER_ID";
  public static final String TOKEN_PUSH_NOTIFICATION_SENDER_ID = "TOKEN_PUSH_NOTIFICATION_SENDER_ID";
  public static final String TOKEN_BIZTALK_SENDER_ID = "TOKEN_BIZTALK_SENDER_ID";
  public static final String EXPIRES_IN = "TOKEN_EXPIRES_IN";
  public static final String HOUR_MAXIMUM = "TOKEN_HOUR_MAXIMUM";

  static String defaultSmsSenderId = System.getenv(TOKEN_SMS_SENDER_ID);
  static String defaultPushNotificationSenderId = System.getenv(TOKEN_PUSH_NOTIFICATION_SENDER_ID);
  static String defaultBiztalkSenderId = System.getenv(TOKEN_BIZTALK_SENDER_ID);
  static Integer defaultExpiresIn = parseIntegerSafety(System.getenv(EXPIRES_IN));
  static Integer defaultHourMaximum = parseIntegerSafety(System.getenv(HOUR_MAXIMUM));

  String smsSenderId;
  String pushNotificationSenderId;
  String biztalkSenderId;
  Integer expiresIn;
  Integer hourMaximum;

  public static TokenCodeProperties valueOf(Config.Scope config) {
    return TokenCodeProperties.builder()
        .smsSenderId(config.get(TOKEN_SMS_SENDER_ID, defaultSmsSenderId))
        .pushNotificationSenderId(config.get(TOKEN_PUSH_NOTIFICATION_SENDER_ID, defaultPushNotificationSenderId))
        .biztalkSenderId(config.get(TOKEN_BIZTALK_SENDER_ID, defaultBiztalkSenderId))
        .expiresIn(config.getInt(EXPIRES_IN, defaultExpiresIn))
        .hourMaximum(config.getInt(HOUR_MAXIMUM, defaultHourMaximum))
        .build();
  }

  public static Integer parseIntegerSafety(String text) {
    if (text == null)
      return null;

    try {
      return Integer.parseInt(text);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  public static Boolean parseBooleanSafety(String text) {
    if (text == null)
      return null;

    try {
      return Boolean.parseBoolean(text);
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
