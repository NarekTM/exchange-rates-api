package com.narektm.exchangeratesapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Map;

public record ExchangeRatesResponse(@JsonProperty(value = "base") String baseCurrencyCode,
                                    @JsonProperty(value = "rates") Map<String, BigDecimal> rates) {
}
