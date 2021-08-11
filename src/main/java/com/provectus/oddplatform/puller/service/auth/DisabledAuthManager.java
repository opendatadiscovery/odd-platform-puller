package com.provectus.oddplatform.puller.service.auth;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@ConditionalOnProperty(value = "platform.auth.type", havingValue = "DISABLED")
public class DisabledAuthManager implements AuthManager {

    @Override
    public Mono<HttpHeaders> authenticate() {
        return Mono.empty();
    }
}
