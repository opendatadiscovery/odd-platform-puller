package com.provectus.oddplatform.puller.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@ConfigurationProperties(prefix = "platform.auth.oauth")
@Data
public class OAuthProperties {
    private URL authDomain;
    private String clientId;
    private String clientSecret;

    public String credentialsBase64() {
        return Base64
            .getEncoder()
            .encodeToString(String.format("%s:%s", clientId, clientSecret).getBytes(StandardCharsets.UTF_8));
    }
}
