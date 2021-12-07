package com.provectus.oddplatform.puller.task;

import com.provectus.oddplatform.puller.dto.DataSourceDto;
import com.provectus.oddplatform.puller.service.AdapterRestService;
import com.provectus.oddplatform.puller.service.PlatformRestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class PullerTask implements Runnable {
    private final DataSourceDto dataSourceDto;
    private final AdapterRestService adapterRestService;
    private final PlatformRestService platformRestService;
    private final String syntheticOddrn;

    @Override
    public void run() {
        log.info("Running puller task for Data Source: {}", dataSourceDto.getId());

        adapterRestService
            .getEntities(dataSourceDto.getEndpoint())
            .map(payload -> payload.dataSourceOddrn(syntheticOddrn))
            .flatMap(platformRestService::ingest)
            .doOnError(t -> log.error(t.getMessage()))
            .subscribe(r -> log.info("Puller task has been completed. Data Source: {}", dataSourceDto.getId()));
    }
}
