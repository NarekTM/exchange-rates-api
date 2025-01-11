package com.narektm.exchangeratesapi.converter;

import com.narektm.exchangeratesapi.dto.CurrencyDetailsDto;
import com.narektm.exchangeratesapi.dto.CurrencySummaryDto;
import com.narektm.exchangeratesapi.dto.ExchangeRatesResponse;
import com.narektm.exchangeratesapi.persistence.entity.CurrencyEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class CurrencyConverter {

    public CurrencySummaryDto toCurrencySummaryDto(String currencyCode) {
        return new CurrencySummaryDto(currencyCode);
    }

    public CurrencyDetailsDto toCurrencyDetailsDto(CurrencyEntity currency) {
        return new CurrencyDetailsDto(currency.getCode(),
                currency.getRates(),
                currency.getCreatedOn(),
                currency.getUpdatedOn());
    }

    public CurrencyEntity toCurrencyEntity(ExchangeRatesResponse exchangeRatesResponse) {
        CurrencyEntity currencyEntity = new CurrencyEntity();

        currencyEntity.setCode(exchangeRatesResponse.baseCurrencyCode());
        currencyEntity.setRates(exchangeRatesResponse.rates());
        LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
        currencyEntity.setCreatedOn(now);
        currencyEntity.setUpdatedOn(now);

        return currencyEntity;
    }
}
