package com.provectus.oddplatform.puller.config;

import com.provectus.oddplatform.puller.config.properties.OAuthProperties;
import com.provectus.oddplatform.puller.dto.DataSourceDto;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableScheduling
@EnableConfigurationProperties(OAuthProperties.class)
public class PullerConfiguration {
    @Bean
    public WebClient platformWebClient(
        @Value("${platform.host-url}") final String platformHost,
        @Value("${puller.client.max-in-memory-size}") final Integer maxInMemorySize
    ) {
        return WebClient.builder()
            .baseUrl(platformHost)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .exchangeStrategies(ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(maxInMemorySize))
                .build())
            .clientConnector(new ReactorClientHttpConnector(HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000)
                .responseTimeout(Duration.ofMillis(30000))
                .doOnConnected(conn ->
                    conn.addHandlerLast(new ReadTimeoutHandler(30000, TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(30000, TimeUnit.MILLISECONDS)))))
            .build();
    }

    @Bean
    public WebClient adapterWebClient(@Value("${puller.client.max-in-memory-size}") final Integer maxInMemorySize) {
        return WebClient.builder()
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .exchangeStrategies(ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(maxInMemorySize))
                .build())
            .clientConnector(new ReactorClientHttpConnector(HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000)
                .responseTimeout(Duration.ofMillis(30000))
                .doOnConnected(conn ->
                    conn.addHandlerLast(new ReadTimeoutHandler(30000, TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(30000, TimeUnit.MILLISECONDS)))))
            .build();
    }

    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler(
        @Value("${puller.worker-thread-pool-size:5}") final int workerThreadSize
    ) {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(workerThreadSize);
        threadPoolTaskScheduler.setThreadNamePrefix("worker-thread");

        return threadPoolTaskScheduler;
    }

    @Bean
    public BlockingQueue<List<DataSourceDto>> blockingQueue() {
        return new LinkedBlockingDeque<>(2);
    }
}
