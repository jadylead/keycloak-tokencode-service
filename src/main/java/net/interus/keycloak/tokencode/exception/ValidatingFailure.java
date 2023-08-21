package net.interus.keycloak.tokencode.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ValidatingFailure extends Exception {
  private Integer statusCode = -1;
  private String errorCode = "";
  private String errorMessage = "";
}
