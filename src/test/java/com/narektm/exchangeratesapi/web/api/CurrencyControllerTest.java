package com.narektm.exchangeratesapi.web.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.narektm.exchangeratesapi.dto.CurrencyDetailsDto;
import com.narektm.exchangeratesapi.dto.CurrencySummaryDto;
import com.narektm.exchangeratesapi.service.CurrencyService;
import com.narektm.exchangeratesapi.web.model.AddCurrencyRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CurrencyController.class)
class CurrencyControllerTest {

    private static final String BASE_URL = "/api/v1.0/currencies";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CurrencyService currencyService;

    @Test
    void getAllCurrencies_shouldReturnCurrencySummaryDtos() throws Exception {
        Set<CurrencySummaryDto> mockCurrencies =
                Set.of(new CurrencySummaryDto("EUR"), new CurrencySummaryDto("USD"));

        when(currencyService.getAllCurrencies()).thenReturn(mockCurrencies);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isNotEmpty());

        verify(currencyService).getAllCurrencies();
    }

    @Test
    void getCurrency_shouldReturnCurrencyDetailsDto() throws Exception {
        String currencyCode = "USD";
        CurrencyDetailsDto mockDto = getMockDto(currencyCode, "EUR");

        when(currencyService.getCurrency(currencyCode)).thenReturn(mockDto);

        mockMvc.perform(get(BASE_URL + "/" + currencyCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(currencyCode));

        verify(currencyService).getCurrency(currencyCode);
    }

    @Test
    void addCurrency_shouldReturnAddedCurrencyDetailsDto() throws Exception {
        AddCurrencyRequest request = new AddCurrencyRequest("GBP");
        CurrencyDetailsDto mockDto = getMockDto("GBP", "USD");

        when(currencyService.addCurrency(request)).thenReturn(mockDto);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(request.currencyCode()));

        verify(currencyService).addCurrency(request);
    }

    private static CurrencyDetailsDto getMockDto(String baseCurrencyCode, String rateCurrencyCode) {
        return new CurrencyDetailsDto(baseCurrencyCode,
                Map.of(rateCurrencyCode, BigDecimal.ONE),
                LocalDateTime.now(),
                LocalDateTime.now());
    }
}