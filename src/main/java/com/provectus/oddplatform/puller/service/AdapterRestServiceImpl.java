package com.provectus.oddplatform.puller.service;

import lombok.extern.slf4j.Slf4j;
import org.opendatadiscovery.client.model.DataEntityList;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URL;

@Service
@Slf4j
public class AdapterRestServiceImpl implements AdapterRestService {
    private final WebClient webClient;

    public AdapterRestServiceImpl(@Qualifier("adapterWebClient") final WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Mono<DataEntityList> getEntities(final URL adapterHost) {
        final URI uri;
        try {
            uri = new URL(adapterHost, "/entities").toURI();
        } catch (final Exception e) {
            log.error("Couldn't create adapter's URI {}. Error message: {}", adapterHost, e.getMessage());
            return Mono.error(e);
        }

        return this.webClient.get()
            .uri(uri)
            .exchangeToMono(r -> r.statusCode() == HttpStatus.OK
                ? r.bodyToMono(DataEntityList.class)
                : r.createException().flatMap(Mono::error));
    }
}
