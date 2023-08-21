package net.interus.keycloak.tokencode.impl;

import net.interus.keycloak.tokencode.relay.Link;
import net.interus.keycloak.tokencode.relay.MediaType;
import net.interus.keycloak.tokencode.relay.Message;
import net.interus.keycloak.tokencode.relay.MessageSender;
import net.interus.keycloak.tokencode.*;
import net.interus.keycloak.tokencode.delegation.SendingDelegator;
import net.interus.keycloak.tokencode.delegation.ValidatingDelegator;
import net.interus.keycloak.tokencode.exception.SendingFailure;
import net.interus.keycloak.tokencode.exception.ValidatingFailure;
import net.interus.keycloak.tokencode.jpa.TokenCodeEntity;
import net.interus.keycloak.tokencode.jpa.representations.TokenCodeRepresentation;
import org.apache.http.HttpStatus;
import org.jboss.logging.Logger;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.validation.Validation;
import org.keycloak.util.JsonSerialization;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TemporalType;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class TokenCodeServiceImpl implements TokenCodeService, ValidatingDelegator, SendingDelegator {
  private static final Logger logger = Logger.getLogger(TokenCodeServiceImpl.class);

  protected final KeycloakSession session;
  protected final EntityManager entityManager;
  protected final RealmModel realm;

  private final TokenCodeProperties properties;

  private OnUserConfirmedListener userConfirmedListener;

  public TokenCodeServiceImpl(KeycloakSession session, TokenCodeProperties properties) {
    this.session = session;
    this.entityManager = session.getProvider(JpaConnectionProvider.class).getEntityManager();
    this.realm = session.getContext().getRealm();
    this.properties = properties;
  }

  private TokenCodeRepresentation _ongoingProcess(String uri, TokenCodeType tokenCodeType, String credentialType) {
    logger.info(String.format("ongoingProcess, realm: %s, uri: %s, tokenCodeType: %s, credentialType: %s",
        this.realm.getId(), uri, tokenCodeType.name(), credentialType));

    Instant now = Instant.now();
    try {
      TokenCodeEntity entity = this.entityManager
          .createNamedQuery("ongoingProcess", TokenCodeEntity.class)
          .setParameter("realmId", this.realm.getId())
          .setParameter("uri", uri)
          //.setParameter("type", tokenCodeType.name())
          .setParameter("credentialType", credentialType)
          .setParameter("confirmed", false)
          .setParameter("now", Date.from(now), TemporalType.TIMESTAMP)
          //.setFirstResult(0)
          .setMaxResults(1)
          .getSingleResult();
      if (entity != null) {
        TokenCodeRepresentation tokenCodeRepresentation = new TokenCodeRepresentation();
        tokenCodeRepresentation.setId(entity.getId());
        tokenCodeRepresentation.setRealmId(entity.getRealmId());
        tokenCodeRepresentation.setUri(entity.getUri());
        tokenCodeRepresentation.setCode(entity.getCode());
        tokenCodeRepresentation.setMediaType(entity.getMediaType());
        tokenCodeRepresentation.setType(entity.getType());
        tokenCodeRepresentation.setCredentialType(entity.getCredentialType());
        tokenCodeRepresentation.setCredentialData(entity.getCredentialData());
        tokenCodeRepresentation.setCreatedAt(entity.getCreatedAt());
        tokenCodeRepresentation.setExpiresAt(entity.getExpiresAt());
        tokenCodeRepresentation.setConfirmed(entity.getConfirmed());
        tokenCodeRepresentation.setByWhom(entity.getByWhom());
        return tokenCodeRepresentation;
      }
    } catch (NoResultException ignore) {
    }
    return null;
  }

  private boolean _isAbusing(String uri, TokenCodeType tokenCodeType, String credentialType, int hourMaximum) {
    Date oneHourAgo = new Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1));

    List<TokenCodeEntity> entities = this.entityManager
        .createNamedQuery("processesSince", TokenCodeEntity.class)
        .setParameter("realmId", this.realm.getId())
        .setParameter("uri", uri)
        .setParameter("type", tokenCodeType.name())
        .setParameter("credentialType", credentialType)
        .setParameter("date", oneHourAgo, TemporalType.TIMESTAMP)
        .getResultList();
    return entities.size() > hourMaximum;
  }



  @Override
  public int sendCode(String uri, TokenCodeType type, String credentialType,
                      String credentialData) throws SendingFailure {
    return _sendCode(uri, uri, type, MediaType.SMS, credentialType, credentialData, null, null,null);
  }

  @Override
  public int sendCode(String uri, TokenCodeType type, String credentialType,
                      String credentialData, SendingDelegator delegator) throws SendingFailure {
    return _sendCode(uri, uri, type, MediaType.SMS, credentialType, credentialData, null, null, delegator);
  }

  @Override
  public int sendCode(String uri, String toAddress, TokenCodeType type, String credentialType,
                      String credentialData) throws SendingFailure {
    return _sendCode(uri, toAddress, type, MediaType.SMS, credentialType, credentialData, null, null, null);
  }

  @Override
  public int sendCode(String uri, String toAddress, TokenCodeType type, MediaType mediaType, String credentialType,
                      String credentialData, Message message) throws SendingFailure {
    return _sendCode(uri, toAddress, type, mediaType, credentialType, credentialData, message, null, null);
  }

  @Override
  public int sendCode(String uri, String toAddress, TokenCodeType type, MediaType mediaType, String credentialType,
                      String credentialData, Message message, Integer expiresIn) throws SendingFailure {
    return _sendCode(uri, toAddress, type, mediaType, credentialType, credentialData, message,expiresIn, null);
  }

  private int _sendCode(String uri, String toAddress, TokenCodeType type, MediaType mediaType, String credentialType,
                      String credentialData, Message message, Integer expiresIn, SendingDelegator delegator) throws SendingFailure {
    if (uri == null) {
      throw new SendingFailure(HttpStatus.SC_BAD_REQUEST, "request_fail",
          "no parameter: uri");
    }
    if (toAddress == null) {
      throw new SendingFailure(HttpStatus.SC_BAD_REQUEST, "request_fail",
          "no parameter: toAddress");
    }

    if (_isAbusing(uri, type, credentialType, properties.getHourMaximum())) {
      throw new SendingFailure(HttpStatus.SC_FORBIDDEN, "request_fail",
          "Requested the maximum number(" + properties.getHourMaximum() + ") of messages the last hour");
    }

    TokenCodeRepresentation tokenCode = _ongoingProcess(uri, type, credentialType);
    if (tokenCode == null) {
      // Generate new token code
      tokenCode = TokenCodeRepresentation.valueOf(realm.getId(), uri, toAddress, type, mediaType, credentialType, credentialData);
      logger.info(String.format("Generate new token"));
    } else {
      int transitExpiresIn = (int) (tokenCode.getExpiresAt().getTime() - Instant.now().toEpochMilli()) / 1000;
      if (tokenCode.getMediaType().equalsIgnoreCase(mediaType.getLabel()) || transitExpiresIn < 5) {
        // Revert ongoing token code If media type not changed or time is up
        logger.info(String.format("Revert ongoing token code & Generate new one %s media type for %s", mediaType.getLabel(), uri));
        TokenCodeEntity entity = entityManager.find(TokenCodeEntity.class, tokenCode.getId());
        entity.setConfirmed(true);
        entityManager.persist(entity);

        // and generate new token code
        tokenCode = TokenCodeRepresentation.valueOf(realm.getId(), uri, toAddress, type, mediaType, credentialType, credentialData);
      } else {
        // Reuse ongoing token code
        logger.info(String.format("Reuse ongoing token code If media type changed %s media type for %s", mediaType.getLabel(), uri));

        TokenCodeEntity entity = entityManager.find(TokenCodeEntity.class, tokenCode.getId());
        entity.setToAddress(toAddress);
        entity.setMediaType(mediaType.getLabel());
        entity.setCredentialData(credentialData);
        entityManager.persist(entity);

        tokenCode.setToAddress(toAddress);
        tokenCode.setMediaType(mediaType.getLabel());
        tokenCode.setCredentialData(credentialData);
        expiresIn = transitExpiresIn;
      }
    }

    // Macro with URL Encoding & Shortening
    if (message != null) {
      message.putDictionary("$tokenCode", tokenCode.getCode());
      message.macro();

      final Link link;
      if ((link = message.getLink()) != null) {
        if (message.hasBodyLink()) {
          message.clearDictionary()
              .putDictionary("$link.url", link.fillFallbackAndAppToUrl().shortenUrl().getUrl())
              .macro();
        } else {
          link.encodeFallbackAndApp().fillFallbackAndAppToUrl();
        }
      } else {
        message.clearDictionary()
            .putDictionary("$link.fallback", link.getFallback())
            .putDictionary("$link.app", link.getApp())
            .macro();
        message.shortUrlBody();
      }
    }

    if (delegator == null)
      delegator = this;
    if (!delegator.onSending(tokenCode, message)) {
      logger.warn(String.format("Message sending to %s failed with %s", toAddress, uri));
      throw new SendingFailure(HttpStatus.SC_FORBIDDEN, "request_fail", "Message sending failed");
    }

    if (expiresIn == null)
      expiresIn = properties.getExpiresIn();
    _persistCode(tokenCode, message, expiresIn);

    return expiresIn;
  }

  @Override
  public boolean sendMessage(String toAddress, MediaType mediaType, Message message) throws SendingFailure {
    if (toAddress == null) {
      throw new SendingFailure(HttpStatus.SC_BAD_REQUEST, "request_fail",
          "no parameter: toAddress");
    }

    // Macro with URL Encoding & Shortening
    if (message != null) {
      final Link link;
      if ((link = message.getLink()) != null) {
        link.encodeFallbackAndApp();
        if (link.getUrl() != null) {
          if (message.hasBodyLink()) {
            message.clearDictionary()
                .putDictionary("$link.url", link.fillFallbackAndAppToUrl().shortenUrl().getUrl())
                .macro();
          } else {
            link.encodeFallbackAndApp().fillFallbackAndAppToUrl();
          }
        } else {
          message.clearDictionary()
              .putDictionary("$link.fallback", link.getFallback())
              .putDictionary("$link.app", link.getApp())
              .macro();
          message.shortUrlBody();
        }
      }
    }

    if (!_sendMessage(toAddress, mediaType, message)) {
      logger.warn(String.format("Message sending to %s failed", toAddress));
      throw new SendingFailure(HttpStatus.SC_FORBIDDEN, "request_fail", " Message sending failed");
    }

    return true;
  }

  @Override
  public boolean onSending(TokenCodeRepresentation tokenCode, Message message) throws SendingFailure {
    return _sendMessage(tokenCode.getToAddress(), MediaType.valueOf(tokenCode.getMediaType()), message);
  }

  public boolean _sendMessage(String toAddress, MediaType mediaType, Message message) throws SendingFailure {
    String senderId;
    if (mediaType == MediaType.SMS || mediaType == MediaType.LMS) {
      senderId = session.listProviderIds(MessageSender.class)
          .stream().filter(s -> s.equals(properties.getSmsSenderId()))
          .findFirst().orElseThrow(() -> new SendingFailure(HttpStatus.SC_BAD_REQUEST,
              "not_support_media_type",
              "not found send provider: " + mediaType.getLabel()));
    } else if (mediaType == MediaType.PUSH_NOTIFICATION) {
      senderId = session.listProviderIds(MessageSender.class)
          .stream().filter(s -> s.equals(properties.getPushNotificationSenderId()))
          .findFirst().orElseThrow(() -> new SendingFailure(HttpStatus.SC_BAD_REQUEST,
              "not_support_media_type",
              "not found send provider: " + mediaType.getLabel()));
    } else if (mediaType == MediaType.BIZTALK) {
      senderId = session.listProviderIds(MessageSender.class)
          .stream().filter(s -> s.equals(properties.getBiztalkSenderId()))
          .findFirst().orElseThrow(() -> new SendingFailure(HttpStatus.SC_BAD_REQUEST,
              "not_support_media_type",
              "not found send provider: " + mediaType.getLabel()));
    } else {
      throw new SendingFailure(HttpStatus.SC_BAD_REQUEST,
          "not_support_media_type",
          "not support media type: " + mediaType.getLabel());
    }

    return session.getProvider(MessageSender.class, senderId).process(toAddress, message);
  }

  private void _persistCode(TokenCodeRepresentation tokenCode, Message message, int tokenExpiresIn) {
    logger.info(String.format("PersistCode, tokenCode: %s, tokenExpiresIn: %d",
        tokenCode.toString(), tokenExpiresIn));

    Instant now = Instant.now();
    logger.info(String.format(" createdAt: %s, expiresAt: %s", Date.from(now),
        Date.from(now.plusSeconds(tokenExpiresIn))));

    TokenCodeEntity entity = new TokenCodeEntity();
    entity.setId(tokenCode.getId());
    entity.setRealmId(tokenCode.getRealmId());
    entity.setMediaType(tokenCode.getMediaType());
    entity.setType(tokenCode.getType());
    entity.setUri(tokenCode.getUri());
    entity.setFromAddress(tokenCode.getFromAddress());
    entity.setToAddress(tokenCode.getToAddress());
    entity.setCode(tokenCode.getCode());
    entity.setCredentialType(tokenCode.getCredentialType());
    entity.setCredentialData(tokenCode.getCredentialData());
    if (message != null) entity.setComparativeData(message.toString());
    entity.setCreatedAt(Date.from(now));
    entity.setExpiresAt(Date.from(now.plusSeconds(tokenExpiresIn)));
    entity.setConfirmed(tokenCode.getConfirmed());
    this.entityManager.persist(entity);
  }

  @Override
  public boolean validateCode(String uri, String code, TokenCodeType tokenCodeType, String credentialType,
                              String secretData, UserModel user) throws ValidatingFailure {
    return validateCode(uri, code, tokenCodeType, credentialType, secretData, user, null);
  }

  @Override
  public boolean validateCode(String uri, String code, TokenCodeType tokenCodeType, String credentialType,
                              String secretData, UserModel user, ValidatingDelegator delegator) throws ValidatingFailure {
    return _validateCode(uri, code, tokenCodeType, credentialType, secretData, user, delegator);
  }


  private boolean _validateCode(String uri, String code, TokenCodeType tokenCodeType, String credentialType,
                              String secretData, UserModel user, ValidatingDelegator delegator) throws ValidatingFailure {
    logger.info(String.format("Validate %s , phone: %s, code: %s", tokenCodeType, uri, code));
    TokenCodeRepresentation tokenCode = _ongoingProcess(uri, tokenCodeType, credentialType);
    if (tokenCode == null) return false;

    tokenCode.setSecretData(secretData);
    logger.info(String.format("Check Confirmed %s %s", tokenCode.getId(), tokenCode.getConfirmed()));

    if (delegator == null)
      delegator = this;

    if (delegator.onValidating(tokenCode, code)) {
      TokenCodeEntity entity = this.entityManager.find(TokenCodeEntity.class, tokenCode.getId());
      entity.setConfirmed(true);
      if (user != null) entity.setByWhom(user.getId());
      this.entityManager.persist(entity);
      logger.info(String.format("- persist confirmed"));

      if (user != null && userConfirmedListener != null) {
        userConfirmedListener.onUserConfirmed(session, tokenCode, user);
        logger.info(String.format("- user confirmed"));
      }
      return true;
    }

    return false;
  }

  @Override
  public boolean onValidating(TokenCodeRepresentation tokenCode, String code) {
   if (TokenCodeType.OTP_SAFE.name().equalsIgnoreCase(tokenCode.getType())) {
     logger.info(String.format("Validating compare ( %s | %s ) equals %s temporarily allowed" , tokenCode.getCode(), code, (tokenCode.getCode().equals(code))));
     return true;
   }

    logger.info(String.format("Validating compare ( %s | %s ) equals %s " , tokenCode.getCode(), code, (tokenCode.getCode().equals(code))));
    return tokenCode.getCode().equals(code);
  }

  @Override
  public void close() {
  }

  public void setOnUserConfirmedListener(OnUserConfirmedListener listener) {
    this.userConfirmedListener = listener;
  }
}
