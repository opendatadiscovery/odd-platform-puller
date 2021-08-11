package com.provectus.oddplatform.puller;

import com.provectus.oddplatform.puller.dto.DataSourceDto;
import com.provectus.oddplatform.puller.service.PlatformRestService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.BlockingQueue;

@Component
@RequiredArgsConstructor
public class DataSourceFetcher {
    private final BlockingQueue<List<DataSourceDto>> queue;
    private final PlatformRestService platformRestService;

    @Scheduled(fixedRateString = "PT${platform.fetching-delay}")
    public void fetchDataSources() {
        platformRestService.fetchDataSources()
            .collectList()
            .subscribe(queue::add);
    }
}
