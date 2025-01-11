package com.narektm.exchangeratesapi.converter;

import com.narektm.exchangeratesapi.dto.CurrencyDetailsDto;
import com.narektm.exchangeratesapi.dto.CurrencySummaryDto;
import com.narektm.exchangeratesapi.dto.ExchangeRatesResponse;
import com.narektm.exchangeratesapi.persistence.entity.CurrencyEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CurrencyConverterTest {

    private final CurrencyConverter currencyConverter = new CurrencyConverter();

    @Test
    void toCurrencySummaryDto_shouldReturnCurrencySummaryDto() {
        String currencyCode = "USD";

        CurrencySummaryDto result = currencyConverter.toCurrencySummaryDto(currencyCode);

        assertThat(result.code()).isEqualTo(currencyCode);
    }

    @Test
    void toCurrencyDetailsDto_shouldReturnCurrencyDetailsDto() {
        CurrencyEntity entity = new CurrencyEntity();
        entity.setCode("USD");
        entity.setRates(Map.of("EUR", BigDecimal.ONE));
        entity.setCreatedOn(LocalDateTime.now());
        entity.setUpdatedOn(LocalDateTime.now());

        CurrencyDetailsDto result = currencyConverter.toCurrencyDetailsDto(entity);

        assertThat(result.code()).isEqualTo("USD");
        assertThat(result.rates()).isEqualTo(entity.getRates());
        assertThat(result.createdOn()).isEqualTo(entity.getCreatedOn());
        assertThat(result.updatedOn()).isEqualTo(entity.getUpdatedOn());
    }

    @Test
    void toCurrencyEntity_shouldReturnCurrencyEntity() {
        ExchangeRatesResponse response = new ExchangeRatesResponse(
                "USD",
                Map.of("EUR", BigDecimal.ONE)
        );

        CurrencyEntity result = currencyConverter.toCurrencyEntity(response);

        assertThat(result.getCode()).isEqualTo(response.baseCurrencyCode());
        assertThat(result.getRates()).isEqualTo(response.rates());
        assertThat(result.getCreatedOn()).isNotNull();
        assertThat(result.getUpdatedOn()).isNotNull();
    }
}