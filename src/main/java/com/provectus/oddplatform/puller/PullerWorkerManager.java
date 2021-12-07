package com.provectus.oddplatform.puller;

import com.provectus.oddplatform.puller.dto.DataSourceDto;
import com.provectus.oddplatform.puller.service.AdapterRestService;
import com.provectus.oddplatform.puller.service.PlatformRestService;
import com.provectus.oddplatform.puller.task.PullerTask;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class PullerWorkerManager {
    private final ThreadPoolTaskScheduler scheduler;

    private final BlockingQueue<List<DataSourceDto>> queue;
    private final Map<Long, Pair<DataSourceDto, ScheduledFuture<?>>> jobRepository = new HashMap<>();

    private final AdapterRestService adapterRestService;
    private final PlatformRestService platformRestService;

    @EventListener(ApplicationReadyEvent.class)
    public void manage() throws InterruptedException {
        while (true) {
            processDataSourceBatch(queue.take());
        }
    }

    private void processDataSourceBatch(final List<DataSourceDto> dataSourceBatch) {
        removeInactiveFromState(dataSourceBatch);

        for (final DataSourceDto dataSource : dataSourceBatch) {
            try {
                jobRepository.compute(dataSource.getId(), (id, value) -> scheduleTask(dataSource, value));
            } catch (final Exception e) {
                log.error(
                    "Error while handling data source current state: {}. Error message: {}", dataSourceBatch, e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void removeInactiveFromState(final List<DataSourceDto> batch) {
        final Set<Long> batchIds = batch.stream().map(DataSourceDto::getId).collect(Collectors.toSet());

        jobRepository.entrySet().removeIf(e -> {
            if (!batchIds.contains(e.getKey())) {
                log.info("Data Source {} considered inactive", e.getValue().left().getId());
                e.getValue().right().cancel(false);
                return true;
            }

            return false;
        });
    }

    private Pair<DataSourceDto, ScheduledFuture<?>> scheduleTask(final DataSourceDto dataSource,
                                                                 final Pair<DataSourceDto, ScheduledFuture<?>> value) {
        if (value != null) {
            if (areDataSourceEqual(value.left(), dataSource)) {
                log.info("Data Source {} hasn't been changed", dataSource.getId());
                return value;
            }

            log.info("Data Source {} has been changed, recreating puller task", dataSource.getId());
            value.right().cancel(true);
        }

        final PullerTask task = new PullerTask(dataSource, adapterRestService, platformRestService);

        return new Pair<>(
            dataSource,
            scheduler.scheduleAtFixedRate(task, dataSource.getInterval() * 1000)
        );
    }

    private boolean areDataSourceEqual(final DataSourceDto d1, final DataSourceDto d2) {
        return d1.getInterval() == d2.getInterval() && d1.getEndpoint().equals(d2.getEndpoint());
    }

    private record Pair<L, R>(L left, R right) {
    }
}
