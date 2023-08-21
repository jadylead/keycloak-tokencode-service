package net.interus.keycloak.tokencode.integrated.firebase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.interus.keycloak.tokencode.impl.TokenCodeServiceImpl;
import net.interus.keycloak.tokencode.integrated.firebase.dto.DynamicLinksShortUrlResultData;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.util.JsonSerialization;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

public class DynamicLinksRestApi implements DynamicLinksApi {
  private static final Logger logger = Logger.getLogger(TokenCodeServiceImpl.class);

  private final DynamicLinksProperties properties;

  public DynamicLinksRestApi() {
    this.properties = DynamicLinksProperties.defaultValueOf();
  }

  public DynamicLinksRestApi(Config.Scope config) {
    this.properties = DynamicLinksProperties.valueOf(config);
  }

  @Override
  public String postShortLink(String longDynamicLink) throws IOException {
    String destUrl = String.format("%s/%s/shortLinks?key=%s", properties.apiHost, properties.apiVersion, properties.apiKey);
    String dataStr = String.format("{\"longDynamicLink\":\"%s\"}", longDynamicLink);
    logger.info(String.format("Post postShortLink url: %s body: %s", destUrl, dataStr));

    String resultDataStr;
    try {
      resultDataStr = callRestService(destUrl, dataStr);
      if (resultDataStr != null) {
        logger.info(String.format("- res data %s", resultDataStr));
        DynamicLinksShortUrlResultData result = DynamicLinksShortUrlResultData.valueOf(resultDataStr);
        if (result != null) {
          return result.getShortLink();
        }
      }

    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }

    return null;
  }

  /**
   * @param url
   * @param dataStr
   * @return
   * @throws URISyntaxException
   * @throws IOException
   */
  public static String callRestService(String url, String dataStr)
      throws URISyntaxException, IOException {

    HttpPost post = new HttpPost(url);
    post.setHeader("Accept", "*/*");
    post.setHeader("Content-type", "application/json");
    URI uri = new URIBuilder(post.getURI()).build();
    post.setURI(uri);

    HttpEntity entity = new StringEntity(dataStr, "UTF-8");
    post.setEntity(entity);
    logger.info(String.format("- set request params %s", dataStr));

    CloseableHttpClient client = null;
    CloseableHttpResponse response = null;

    try {
      client = HttpClientBuilder.create().build();
      response = client.execute(post);
      logger.info(String.format("- response %s", response.getStatusLine()));

      if (response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201) {
        //return NotificationPostResponseData.valueOf(IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8.name()));
        return IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8.name());
      }
    } finally {
      if (client != null) client.close();
      if (response != null) response.close();
    }

    return null;
  }
}
