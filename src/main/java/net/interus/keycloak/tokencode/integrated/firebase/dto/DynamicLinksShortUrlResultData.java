package net.interus.keycloak.tokencode.integrated.firebase.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DynamicLinksShortUrlResultData {
  private String shortLink;

  private String previewLink;

  //private Warning warning;

  public static DynamicLinksShortUrlResultData valueOf(String data) throws JsonProcessingException {
    return new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .readValue(data, DynamicLinksShortUrlResultData.class);
  }
}