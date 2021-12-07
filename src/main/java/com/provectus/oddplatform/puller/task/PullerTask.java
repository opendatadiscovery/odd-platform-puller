package com.provectus.oddplatform.puller.task;

import com.provectus.oddplatform.puller.dto.DataSourceDto;
import com.provectus.oddplatform.puller.service.AdapterRestService;
import com.provectus.oddplatform.puller.service.PlatformRestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opendatadiscovery.client.model.DataEntityList;
import org.opendatadiscovery.oddrn.Generator;
import org.opendatadiscovery.oddrn.model.ODDPlatformDataSourcePath;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Slf4j
public class PullerTask implements Runnable {
    private final DataSourceDto dataSourceDto;
    private final AdapterRestService adapterRestService;
    private final PlatformRestService platformRestService;

    private final Generator generator = new Generator();

    @Override
    public void run() {
        log.info("Running puller task for Data Source: {}", dataSourceDto.getId());

        adapterRestService
            .getEntities(dataSourceDto.getEndpoint())
            .flatMap(this::substituteOddrn)
            .flatMap(platformRestService::ingest)
            .doOnError(t -> log.error(t.getMessage()))
            .subscribe(r -> log.info("Puller task has been completed. Data Source: {}", dataSourceDto.getId()));
    }

    private Mono<DataEntityList> substituteOddrn(final DataEntityList dataEntityList) {
        final ODDPlatformDataSourcePath oddrnPath = ODDPlatformDataSourcePath.builder()
            .datasourceId(dataSourceDto.getId())
            .build();

        try {
            final String dataSourceOddrn = generator.generate(oddrnPath, "datasourceId");
            return Mono.just(dataEntityList.dataSourceOddrn(dataSourceOddrn));
        } catch (final Exception e) {
            return Mono.error(
                new RuntimeException(String.format("Couldn't generate oddrn for data source: %s", dataSourceDto)));
        }
    }
}
