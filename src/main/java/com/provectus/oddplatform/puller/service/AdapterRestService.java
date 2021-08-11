package com.provectus.oddplatform.puller.service;

import reactor.core.publisher.Mono;

import java.net.URL;

public interface AdapterRestService {
    Mono<String> getRawEntityList(final URL adapterHost);
}
