package com.narektm.exchangeratesapi.service;

import com.narektm.exchangeratesapi.dto.ExchangeRatesResponse;
import org.springframework.http.ResponseEntity;

public interface ExchangeRatesService {
    ResponseEntity<ExchangeRatesResponse> fetchExchangeRates(String currencyCode);
}
