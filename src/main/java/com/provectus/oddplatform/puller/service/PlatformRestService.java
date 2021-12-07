package com.provectus.oddplatform.puller.service;

import com.provectus.oddplatform.puller.dto.DataSourceDto;
import org.opendatadiscovery.client.model.DataEntityList;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PlatformRestService {
    Flux<DataSourceDto> fetchDataSources();

    Mono<Integer> ingest(final DataEntityList payload);
}
