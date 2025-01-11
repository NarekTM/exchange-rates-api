package com.narektm.exchangeratesapi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.narektm.exchangeratesapi.config.ExternalExchangeRatesApiProperties;
import com.narektm.exchangeratesapi.dto.ExchangeRatesResponse;
import com.narektm.exchangeratesapi.exception.ExternalApiException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.client.MockRestServiceServer;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(ExternalExchangeRatesService.class)
@EnableConfigurationProperties(ExternalExchangeRatesApiProperties.class)
class ExternalExchangeRatesServiceTest {

    private static final String BASE_URI = "https://api.apilayer.com/exchangerates_data/latest?base=";
    private static final String USD = "USD";

    @Autowired
    private MockRestServiceServer server;

    @Autowired
    private ExternalExchangeRatesService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fetchExchangeRates_shouldReturnExchangeRatesResponse_whenValidCurrencyCodeProvided()
            throws ExternalApiException, JsonProcessingException {
        ExchangeRatesResponse expectedResponse =
                new ExchangeRatesResponse(USD, Map.of("EUR", BigDecimal.ONE));

        server.expect(requestTo(BASE_URI + USD))
                .andRespond(withSuccess(objectMapper.writeValueAsString(expectedResponse), MediaType.APPLICATION_JSON));

        ResponseEntity<ExchangeRatesResponse> actualResponse = service.fetchExchangeRates(USD);

        assertThat(actualResponse.getBody()).isEqualTo(expectedResponse);
    }

    @Test
    void fetchExchangeRates_shouldThrowExternalApiException_whenResponseContainsError() {
        String wrongCurrencyCode = "QWERTY";

        server.expect(requestTo(BASE_URI + wrongCurrencyCode))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("Invalid base currency provided: " + wrongCurrencyCode));

        Exception exception = assertThrows(Exception.class,
                () -> service.fetchExchangeRates(wrongCurrencyCode));

        assertThat(exception.getMessage()).contains("Invalid base currency provided");
    }
}