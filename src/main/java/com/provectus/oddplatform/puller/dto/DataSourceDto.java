package com.provectus.oddplatform.puller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URL;

@Data
@NoArgsConstructor
public class DataSourceDto {
    private long id;
    private String name;

    @JsonProperty("pulling_interval")
    private long interval;

    @JsonProperty("connection_url")
    private URL endpoint;
}
