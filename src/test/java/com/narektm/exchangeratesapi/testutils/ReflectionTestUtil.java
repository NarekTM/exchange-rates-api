package com.narektm.exchangeratesapi.testutils;

import com.narektm.exchangeratesapi.dto.CurrencyDetailsDto;
import com.narektm.exchangeratesapi.service.CurrencyService;

import java.lang.reflect.Field;
import java.util.Map;

public class ReflectionTestUtil {

    @SuppressWarnings("unchecked")
    public static void putDataToExchangeRates(Map<String, CurrencyDetailsDto> exchangeRates) throws Exception {
        Field field = CurrencyService.class.getDeclaredField("EXCHANGE_RATES");
        field.setAccessible(true);
        var exchangeRateMap = (Map<String, CurrencyDetailsDto>) field.get(null);
        exchangeRateMap.putAll(exchangeRates);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, CurrencyDetailsDto> getExchangeRates() throws Exception {
        Field field = CurrencyService.class.getDeclaredField("EXCHANGE_RATES");
        field.setAccessible(true);

        return (Map<String, CurrencyDetailsDto>) field.get(null);
    }
}
