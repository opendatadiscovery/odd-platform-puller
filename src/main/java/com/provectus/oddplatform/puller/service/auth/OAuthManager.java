package com.provectus.oddplatform.puller.service.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.provectus.oddplatform.puller.config.properties.OAuthProperties;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URL;

@Component
@ConditionalOnProperty(value = "platform.auth.type", havingValue = "OAUTH2")
@RequiredArgsConstructor
@Slf4j
public class OAuthManager implements AuthManager {
    private final WebClient webClient = WebClient.create();
    private final OAuthProperties oAuthProperties;

    @Override
    public Mono<HttpHeaders> authenticate() {
        final String url;
        try {
            // TODO: make URL path as configuration
            url = new URL(oAuthProperties.getAuthDomain(), "/oauth2/token?grant_type=client_credentials").toString();
        } catch (final Exception e) {
            log.error("Couldn't create OAuth's domain URL {}. Error message: {}",
                oAuthProperties.getAuthDomain(), e.getMessage());
            return Mono.error(e);
        }

        return webClient.post()
            .uri(url)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .header("Authorization", String.format("Basic %s", oAuthProperties.credentialsBase64()))
            .exchangeToMono(this::buildAuthHeaders);
    }

    private Mono<HttpHeaders> buildAuthHeaders(final ClientResponse response) {
        if (response.statusCode() != HttpStatus.OK) {
            return Mono.error(new AuthenticationException("Response status code differs from 200 OK"));
        }

        return response.bodyToMono(OAuthResponse.class)
            .map(OAuthResponse::getAccessToken)
            .map(token -> {
                final HttpHeaders authHeader = new HttpHeaders();
                authHeader.set("Authorization", String.format("Bearer %s", token));
                return authHeader;
            });
    }

    @Data
    private static class OAuthResponse {
        @JsonProperty("access_token")
        private String accessToken;

        @JsonProperty("expires_in")
        private int expiresIn;
    }
}
