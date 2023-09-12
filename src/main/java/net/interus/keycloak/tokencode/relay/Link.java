package net.interus.keycloak.tokencode.relay;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.interus.keycloak.tokencode.integrated.URLShorter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Link {
  private String url;
  private String fallback;
  private String app;

  private Button button;

  public Link replaceAll(CharSequence regex, CharSequence replacement) {
    if (url != null)
      url = url.replace(regex, replacement);
    if (fallback != null)
      fallback = fallback.replace(regex, replacement);
    if (app != null)
      app = app.replace(regex, replacement);

    return this;
  }

  public Link encodeFallbackAndApp() {
    if (fallback != null) {
      try {
        fallback = URLEncoder.encode(fallback, "UTF-8");
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }
    }
    if (app != null) {
      try {
        app = URLEncoder.encode(app, "UTF-8");
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }
    }

    return this;
  }

  public Link fillFallbackAndAppToUrl() {
    if (url != null) {
      if (fallback != null)
        url = url.replace("$fallback", fallback);
      if (app != null)
        url = url.replace("$app", app);
    }

    return this;
  }

  public Link shortenUrl() {
    if (url != null && url.contains("https://avkd.page.link")) {
      String shortened;
      try {
        if ((shortened = URLShorter.shorten(url)) != null)
          url = shortened;
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return this;
  }

  public boolean hasButton() {
    return button != null;
  }
}