package com.provectus.oddplatform.puller.service.auth;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
@ConditionalOnProperty(value = "platform.auth.type", havingValue = "LOGIN_FORM")
public class AuthManagerLoginForm implements AuthManager {
    private final WebClient webClient;
    private final LoginFormCredentials loginFormCredentials;

    public AuthManagerLoginForm(@Qualifier("platformWebClient") final WebClient webClient,
                                @Value("${platform.auth.login-form-credentials}") final String credentials) {
        this.webClient = webClient;
        this.loginFormCredentials = LoginFormCredentials.parseCredentialString(credentials);
    }

    @Override
    public Mono<HttpHeaders> authenticate() {
        return webClient.post()
            .uri("/login")
            .body(loginFormCredentials.toFormData())
            .exchangeToMono(this::buildAuthHeaders);
    }

    private Mono<HttpHeaders> buildAuthHeaders(final ClientResponse response) {
        if (response.statusCode() != HttpStatus.FOUND) {
            return Mono.error(new AuthenticationException("Response status code differs from 302 FOUND"));
        }

        final List<String> setCookieHeader = response.headers().header("Set-Cookie");

        if (setCookieHeader.isEmpty()) {
            return Mono.error(new AuthenticationException("Set-Cookie header is empty"));
        }

        final Optional<String> sessionId = Arrays.stream(setCookieHeader.get(0).split(";"))
            .filter(h -> h.startsWith("SESSION"))
            .map(h -> h.split("="))
            .filter(a -> a.length == 2 && StringUtils.hasLength(a[1]))
            .map(a -> a[1])
            .findFirst();

        if (sessionId.isEmpty()) {
            return Mono.error(new AuthenticationException("Couldn't retrieve session cookie from header"));
        }

        final HttpHeaders authHeader = new HttpHeaders();
        authHeader.set("Cookie", String.format("SESSION=%s", sessionId.get()));

        return Mono.just(authHeader);
    }

    @Data
    @RequiredArgsConstructor
    private static class LoginFormCredentials {
        private final String username;
        private final String password;

        public BodyInserters.FormInserter<String> toFormData() {
            return BodyInserters.fromFormData("username", username).with("password", password);
        }

        public static LoginFormCredentials parseCredentialString(final String credentialString) {
            final String[] credentials = credentialString.split(":");

            if (credentials.length != 2) {
                throw new IllegalArgumentException(
                    "Invalid format of given login form credentials string. Must be username:password");
            }

            return new LoginFormCredentials(credentials[0].trim(), credentials[1].trim());
        }
    }
}
