package com.narektm.exchangeratesapi.service;

import com.narektm.exchangeratesapi.config.ExternalExchangeRatesApiProperties;
import com.narektm.exchangeratesapi.dto.ExchangeRatesResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class ExternalExchangeRatesService implements ExchangeRatesService {

    private final ExternalExchangeRatesApiProperties properties;
    private final RestClient restClient;

    public ExternalExchangeRatesService(ExternalExchangeRatesApiProperties properties,
                                        RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.restClient = restClientBuilder
                .baseUrl(properties.getBaseUri())
                .build();
    }

    @Override
    public ResponseEntity<ExchangeRatesResponse> fetchExchangeRates(String currencyCode) {
        return restClient.get()
                .uri(UriComponentsBuilder.fromUriString(properties.getLatestRatesUri())
                        .queryParam("base", currencyCode)
                        .toUriString())
                .header("apikey", properties.getKey())
                .retrieve()
                .toEntity(ExchangeRatesResponse.class);
    }
}
