package net.interus.keycloak.tokencode.relay;

import lombok.*;
import net.interus.keycloak.tokencode.util.URLShorter;
import org.keycloak.util.JsonSerialization;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {
  private String principal;
  private String topic;
  private String title;
  private String summary;
  private String body;
  private String image;
  private String template;
  private Link link;
  private Map<String, String> data;

  private Boolean posting;

  private Map<String, String> dictionary;

  public Message clearDictionary() {
    if (dictionary != null)
      dictionary.clear();
    dictionary = null;

    return this;
  }

  public Message putDictionary(String regex, String replacement) {
    if (dictionary == null)
      dictionary = new HashMap<>();

    if (regex != null && replacement != null)
      dictionary.put(regex, replacement);
    return this;
  }

  public Message macro() {
    if (dictionary != null) {
      dictionary.keySet().stream()
          .forEach(regex -> {
            String replacement = dictionary.get(regex);
            if (title != null)
              title = title.replace(regex, replacement);
            if (summary != null)
              summary = summary.replace(regex, replacement);
            if (body != null)
              body = body.replace(regex, replacement);
            if (link != null) {
              link = link.replaceAll(regex, replacement);
            }
          });
    }

    return this;
  }

  public boolean hasBodyLink() {
    return (body != null) ? body.contains("$link.url") : false;
  }

  /**
   * @deprecated
   * @return
   */
  public Message encodeUrlBody() {
    if (body != null) {
      int start;
      String url;
      try {
        while ((start = body.indexOf("https://")) > 0) {
          int end = body.indexOf(" ", start) > 0 ? body.indexOf(" ", start) : body.length();
          url = body.substring(start, end);
          String encoded = URLEncoder.encode(url, "UTF-8");
          body = body.replace(url, encoded);
        }

        while ((start = body.indexOf("http://")) > 0) {
          int spaceEnd = body.indexOf(" ", start), enterEnd = body.indexOf("\n", start);
          int end = spaceEnd > 0 && enterEnd > 0 ? Math.min(spaceEnd, enterEnd) :
              (spaceEnd > 0 ? spaceEnd : (enterEnd > 0 ? enterEnd : body.length()));
          url = body.substring(start, end);
          String encoded = URLEncoder.encode(url, "UTF-8");
          body = body.replace(url, encoded);
        }
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }
    }
    return this;
  }

  /**
   * @deprecated
   * @return
   */
  public Message shortUrlBody() {
    if (body != null) {
      int start;
      String url;
      try {
        while ((start = body.indexOf("https://avkd.page.link")) > 0) {
          int spaceEnd = body.indexOf(" ", start), enterEnd = body.indexOf("\n", start);
          int end = spaceEnd > 0 && enterEnd > 0 ? Math.min(spaceEnd, enterEnd) :
              (spaceEnd > 0 ? spaceEnd : (enterEnd > 0 ? enterEnd : body.length()));
          url = body.substring(start, end);
          String shortened = URLShorter.shorten(url);
          if (shortened == null) break;
          body = body.replace(url, shortened);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return this;
  }
}