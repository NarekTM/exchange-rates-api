package com.narektm.exchangeratesapi.testutils;

import com.narektm.exchangeratesapi.dto.CurrencyDetailsDto;
import com.narektm.exchangeratesapi.dto.CurrencySummaryDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

public class IntegrationTestUtil {

    private static final RestClient REST_CLIENT = RestClient.builder().build();
    private static final String HTTP_LOCALHOST = "http://localhost:";

    public static ResponseEntity<CurrencyDetailsDto> post(int port, String uri, Map<String, Object> payload) {
        return REST_CLIENT.post()
                .uri(UriComponentsBuilder.fromUriString(HTTP_LOCALHOST)
                        .port(port)
                        .path(uri)
                        .toUriString())
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .toEntity(CurrencyDetailsDto.class);
    }

    public static ResponseEntity<List<CurrencySummaryDto>> getAllCurrencies(int port, String uri) {
        return REST_CLIENT.get()
                .uri(UriComponentsBuilder.fromUriString(HTTP_LOCALHOST)
                        .port(port)
                        .path(uri)
                        .toUriString())
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {
                });
    }

    public static ResponseEntity<CurrencyDetailsDto> getCurrency(int port, String uri) {
        return REST_CLIENT.get()
                .uri(UriComponentsBuilder.fromUriString(HTTP_LOCALHOST)
                        .port(port)
                        .path(uri)
                        .toUriString())
                .retrieve()
                .toEntity(CurrencyDetailsDto.class);
    }
}
