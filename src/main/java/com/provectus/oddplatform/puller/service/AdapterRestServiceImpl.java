package com.provectus.oddplatform.puller.service;

import com.provectus.oddplatform.puller.dto.DataSourceDto;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class AdapterRestServiceImpl implements AdapterRestService {
    private final WebClient webClient = WebClient.builder()
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .clientConnector(new ReactorClientHttpConnector(HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000)
            .responseTimeout(Duration.ofMillis(30000))
            .doOnConnected(conn ->
                conn.addHandlerLast(new ReadTimeoutHandler(30000, TimeUnit.MILLISECONDS))
                    .addHandlerLast(new WriteTimeoutHandler(30000, TimeUnit.MILLISECONDS)))))
        .build();


    @Override
    public Mono<String> getRawEntityList(final URL adapterHost) {
        final URI uri;
        try {
            uri = new URL(adapterHost, "/entities").toURI();
        } catch (final Exception e) {
            log.error("Couldn't create adapter's URI {}. Error message: {}", adapterHost, e.getMessage());
            return Mono.error(e);
        }

        return this.webClient.get()
            .uri(uri)
            .exchangeToMono(r -> r.statusCode() == HttpStatus.OK
                ? r.bodyToMono(String.class)
                : r.createException().flatMap(Mono::error));
    }
}
