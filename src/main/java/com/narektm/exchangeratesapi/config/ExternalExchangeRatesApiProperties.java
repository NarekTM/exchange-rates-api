package com.narektm.exchangeratesapi.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("exchange.rates.external-api")
@Getter
@Setter
public class ExternalExchangeRatesApiProperties {

    private String key;

    private String baseUri;

    private String latestRatesUri;
}
