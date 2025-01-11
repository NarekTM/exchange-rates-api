package com.narektm.exchangeratesapi.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public record CurrencyDetailsDto(String code,
                                 Map<String, BigDecimal> rates,
                                 LocalDateTime createdOn,
                                 LocalDateTime updatedOn) {
}
