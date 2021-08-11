package com.provectus.oddplatform.puller.task;

import com.provectus.oddplatform.puller.dto.DataSourceDto;
import com.provectus.oddplatform.puller.service.AdapterRestService;
import com.provectus.oddplatform.puller.service.PlatformRestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class PullerTask implements Runnable {
    private final DataSourceDto dataSourceDto;
    private final AdapterRestService adapterRestService;
    private final PlatformRestService platformRestService;

    @Override
    public void run() {
        log.info("Running puller task for Data Source: {}", dataSourceDto.getOddrn());

        adapterRestService
            .getRawEntityList(dataSourceDto.getEndpoint())
            .flatMap(platformRestService::ingest)
            .doOnError(t -> log.error(t.getMessage()))
            .subscribe(r -> log.info("Puller task has been completed. Data Source: {}", dataSourceDto.getOddrn()));
    }
}
