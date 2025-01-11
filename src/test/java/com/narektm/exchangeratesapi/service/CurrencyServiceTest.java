package com.narektm.exchangeratesapi.service;

import com.narektm.exchangeratesapi.converter.CurrencyConverter;
import com.narektm.exchangeratesapi.dto.CurrencyDetailsDto;
import com.narektm.exchangeratesapi.dto.CurrencySummaryDto;
import com.narektm.exchangeratesapi.dto.ExchangeRatesResponse;
import com.narektm.exchangeratesapi.exception.ExternalApiException;
import com.narektm.exchangeratesapi.exception.NotFoundException;
import com.narektm.exchangeratesapi.persistence.entity.CurrencyEntity;
import com.narektm.exchangeratesapi.persistence.repository.CurrencyRepository;
import com.narektm.exchangeratesapi.testutils.ReflectionTestUtil;
import com.narektm.exchangeratesapi.web.model.AddCurrencyRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrencyServiceTest {

    private static final String USD = "USD";
    private static final String EUR = "EUR";

    @InjectMocks
    private CurrencyService currencyService;

    @Mock
    private CurrencyRepository currencyRepository;

    @Mock
    private ExternalExchangeRatesService exchangeRatesService;

    @Mock
    private CurrencyConverter currencyConverter;

    @Test
    void getAllCurrencies_shouldReturnAllCurrencySummaries() throws Exception {
        Map<String, CurrencyDetailsDto> mockExchangeRates = Map.of(
                USD, getCurrencyDetailsDto(USD, EUR),
                EUR, getCurrencyDetailsDto(EUR, USD)
        );
        ReflectionTestUtil.putDataToExchangeRates(mockExchangeRates);

        when(currencyConverter.toCurrencySummaryDto(USD))
                .thenReturn(new CurrencySummaryDto(USD));
        when(currencyConverter.toCurrencySummaryDto(EUR))
                .thenReturn(new CurrencySummaryDto(EUR));

        Set<CurrencySummaryDto> result = currencyService.getAllCurrencies();

        assertThat(result).hasSize(2)
                .containsExactlyInAnyOrder(new CurrencySummaryDto(USD),
                        new CurrencySummaryDto(EUR));
        verify(currencyConverter).toCurrencySummaryDto(USD);
        verify(currencyConverter).toCurrencySummaryDto(EUR);
    }

    @Test
    void getCurrency_shouldReturnCurrencyDetails() throws Exception {
        CurrencyDetailsDto mockDetails = getCurrencyDetailsDto(USD, EUR);
        ReflectionTestUtil.putDataToExchangeRates(Map.of(USD, mockDetails));

        CurrencyDetailsDto result = currencyService.getCurrency(USD);

        assertThat(result).isEqualTo(mockDetails);
    }

    @Test
    void getCurrency_shouldThrowNotFoundExceptionWhenCurrencyNotFound() {
        NotFoundException exception = Assertions.assertThrows(NotFoundException.class,
                () -> currencyService.getCurrency(USD));
        assertThat(exception.getMessage()).isEqualTo("Currency with code USD not found");
    }

    @Test
    void addCurrency_shouldAddNewCurrency() throws Exception {
        AddCurrencyRequest request = new AddCurrencyRequest(USD);
        ExchangeRatesResponse response =
                new ExchangeRatesResponse(USD, Map.of(EUR, BigDecimal.ONE));
        CurrencyEntity entity = getCurrencyEntity();
        CurrencyDetailsDto dto = getCurrencyDetailsDto(USD, EUR);

        when(exchangeRatesService.fetchExchangeRates(USD))
                .thenReturn(ResponseEntity.ok(response));
        when(currencyConverter.toCurrencyEntity(response)).thenReturn(entity);
        when(currencyRepository.save(entity)).thenReturn(entity);
        when(currencyConverter.toCurrencyDetailsDto(entity)).thenReturn(dto);

        CurrencyDetailsDto result = currencyService.addCurrency(request);

        assertThat(result).isEqualTo(dto);

        Map<String, CurrencyDetailsDto> exchangeRates = ReflectionTestUtil.getExchangeRates();
        assertThat(exchangeRates).containsKey(USD);
    }

    @Test
    void addCurrency_shouldThrowExceptionWhenCurrencyAlreadyExists() throws Exception {
        AddCurrencyRequest request = new AddCurrencyRequest(USD);
        ReflectionTestUtil.putDataToExchangeRates(Map.of(USD, getCurrencyDetailsDto(USD, EUR)));

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> currencyService.addCurrency(request));
        assertThat(exception.getMessage()).isEqualTo("Currency with code USD already exists");
    }


    @Test
    void updateExchangeRates_shouldUpdateAllCurrencies() {
        CurrencyEntity existingEntity = getCurrencyEntity();

        ExchangeRatesResponse response =
                new ExchangeRatesResponse(USD, Map.of(EUR, BigDecimal.ONE));

        when(currencyRepository.getAllCodes()).thenReturn(Set.of(USD));
        when(exchangeRatesService.fetchExchangeRates(USD)).thenReturn(ResponseEntity.ok(response));
        when(currencyRepository.findByCode(USD)).thenReturn(existingEntity);
        when(currencyRepository.save(any())).thenReturn(existingEntity);
        when(currencyConverter.toCurrencyDetailsDto(existingEntity))
                .thenReturn(new CurrencyDetailsDto(USD,
                        existingEntity.getRates(),
                        existingEntity.getCreatedOn(),
                        existingEntity.getUpdatedOn()));

        currencyService.updateExchangeRates();

        verify(currencyRepository).findByCode(USD);
        verify(currencyRepository).save(any(CurrencyEntity.class));
    }

    @Test
    void updateExchangeRates_shouldHandleExternalApiExceptionGracefully() {
        when(currencyRepository.getAllCodes()).thenReturn(Set.of(USD));
        when(exchangeRatesService.fetchExchangeRates(USD))
                .thenThrow(new ExternalApiException("API Error"));

        currencyService.updateExchangeRates();

        verify(currencyRepository, never()).findByCode(any());
        verify(currencyRepository, never()).save(any());
    }

    @Test
    void initExchangeRatesFromDb_shouldInitializeExchangeRates() throws Exception {
        CurrencyEntity entity = getCurrencyEntity();

        CurrencyDetailsDto dto = new CurrencyDetailsDto(USD, entity.getRates(), entity.getCreatedOn(), entity.getUpdatedOn());
        when(currencyRepository.findAll()).thenReturn(List.of(entity));
        when(currencyConverter.toCurrencyDetailsDto(entity)).thenReturn(dto);

        currencyService.initExchangeRatesFromDb();

        Map<String, CurrencyDetailsDto> exchangeRates = ReflectionTestUtil.getExchangeRates();
        assertThat(exchangeRates).containsKey(USD)
                .containsEntry(USD, dto);
    }

    private static CurrencyDetailsDto getCurrencyDetailsDto(String currencyCode,
                                                            String rateCurrencyCode) {
        return new CurrencyDetailsDto(currencyCode,
                Map.of(rateCurrencyCode, BigDecimal.ONE),
                LocalDateTime.now(),
                LocalDateTime.now());
    }

    private static CurrencyEntity getCurrencyEntity() {
        CurrencyEntity entity = new CurrencyEntity();

        entity.setCode(USD);
        entity.setRates(Map.of(EUR, BigDecimal.ONE));
        entity.setCreatedOn(LocalDateTime.now());
        entity.setUpdatedOn(LocalDateTime.now());

        return entity;
    }

    @AfterEach
    public void clearExchangeRates() throws Exception {
        ReflectionTestUtil.getExchangeRates().clear();
    }
}
