package com.provectus.oddplatform.puller.service.auth;

import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

public interface AuthManager {
    Mono<HttpHeaders> authenticate();
}
