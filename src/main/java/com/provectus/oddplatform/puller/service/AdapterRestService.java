package com.provectus.oddplatform.puller.service;

import org.opendatadiscovery.client.model.DataEntityList;
import reactor.core.publisher.Mono;

import java.net.URL;

public interface AdapterRestService {
    Mono<DataEntityList> getEntities(final URL adapterHost);
}
