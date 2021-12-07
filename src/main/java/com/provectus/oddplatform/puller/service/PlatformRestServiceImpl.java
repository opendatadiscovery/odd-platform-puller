package com.provectus.oddplatform.puller.service;

import com.provectus.oddplatform.puller.dto.DataSourceDto;
import com.provectus.oddplatform.puller.service.auth.AuthManager;
import lombok.extern.slf4j.Slf4j;
import org.opendatadiscovery.client.model.DataEntityList;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class PlatformRestServiceImpl implements PlatformRestService {
    private final WebClient webClient;
    private final AuthManager authManager;

    public PlatformRestServiceImpl(@Qualifier("platformWebClient") final WebClient webClient,
                                   final AuthManager authManager) {
        this.authManager = authManager;
        this.webClient = webClient;
    }

    @Override
    public Flux<DataSourceDto> fetchDataSources() {
        return authenticate().flatMapMany(this::fetchDataSources);
    }

    @Override
    public Mono<Integer> ingest(final DataEntityList payload) {
        return authenticate().flatMap(authHeader -> ingest(payload, authHeader));
    }

    private Flux<DataSourceDto> fetchDataSources(final HttpHeaders authHeader) {
        return webClient.get()
            .uri("/ingestion/datasources/active")
            .headers(headers -> headers.putAll(authHeader))
            .exchangeToFlux(r -> r.statusCode() == HttpStatus.OK
                ? r.bodyToFlux(DataSourceDto.class)
                : r.createException().flux().flatMap(Flux::error));
    }

    private Mono<Integer> ingest(final DataEntityList payload, final HttpHeaders authHeader) {
        return webClient.post()
            .uri("/ingestion/entities")
            .headers(headers -> headers.putAll(authHeader))
            .bodyValue(payload)
            .exchangeToMono(r -> r.statusCode() == HttpStatus.OK
                ? Mono.just(r.statusCode().value())
                : r.createException().flatMap(Mono::error));
    }

    private Mono<HttpHeaders> authenticate() {
        return authManager.authenticate().switchIfEmpty(Mono.just(new HttpHeaders()));
    }
}