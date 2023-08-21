package net.interus.keycloak.tokencode.jpa;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@Table(name = "TOKEN_CODE")
@NamedQueries({
    @NamedQuery(
        name = "ongoingProcess",
        query = "FROM TokenCodeEntity t " +
            "WHERE t.realmId = :realmId " +
            //"AND t.type = :type " +
            "AND t.uri = :uri " +
            "AND t.credentialType = :credentialType " +
            "AND t.expiresAt >= :now " +
            "AND t.confirmed = :confirmed " +
            "ORDER BY t.createdAt DESC "
    ),
    @NamedQuery(
        name = "processesSince",
        query = "FROM TokenCodeEntity t " +
            "WHERE t.realmId = :realmId " +
            "AND t.type = :type " +
            "AND t.uri = :uri " +
            "AND t.credentialType = :credentialType " +
            "AND t.createdAt >= :date "

    ),
})
public class TokenCodeEntity {
  @Id
  @Column(name = "ID")
  private String id;

  @Column(name = "REALM_ID", nullable = false)
  private String realmId;

  @Column(name = "URI", nullable = false)
  private String uri;

  @Column(name = "FROM_ADDRESS")
  private String fromAddress;

  @Column(name = "TO_ADDRESS", nullable = false)
  private String toAddress;

  @Column(name = "MEDIA_TYPE", nullable = false)
  private String mediaType;

  @Column(name = "TYPE", nullable = false)
  private String type;

  @Column(name = "CREDENTIAL_TYPE", nullable = false)
  private String credentialType;

  @Column(name = "CREDENTIAL_DATA")
  private String credentialData;

  @Column(name = "SECRET_DATA")
  private String secretData;

  @Column(name = "COMPARATIVE_DATA")
  private String comparativeData;

  @Column(name = "CODE", nullable = false)
  private String code;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "CREATED_AT", nullable = false)
  private Date createdAt;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "EXPIRES_AT", nullable = false)
  private Date expiresAt;

  @Column(name = "CONFIRMED", nullable = false)
  private Boolean confirmed;

  @Column(name = "BY_WHOM")
  private String byWhom;
}
