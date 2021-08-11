package com.provectus.oddplatform.puller.service;

import com.provectus.oddplatform.puller.dto.DataSourceDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PlatformRestService {
    Flux<DataSourceDto> fetchDataSources();

    Mono<Integer> ingest(final String rawJson);
}
