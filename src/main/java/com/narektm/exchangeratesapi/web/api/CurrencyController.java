package com.narektm.exchangeratesapi.web.api;

import com.narektm.exchangeratesapi.dto.CurrencyDetailsDto;
import com.narektm.exchangeratesapi.dto.CurrencySummaryDto;
import com.narektm.exchangeratesapi.service.CurrencyService;
import com.narektm.exchangeratesapi.web.model.AddCurrencyRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/api/v1.0/currencies")
public class CurrencyController {

    private final CurrencyService currencyService;

    public CurrencyController(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Set<CurrencySummaryDto> getAllCurrencies() {
        return currencyService.getAllCurrencies();
    }

    @GetMapping("/{currencyCode}")
    @ResponseStatus(HttpStatus.OK)
    public CurrencyDetailsDto getCurrency(@PathVariable String currencyCode) {
        return currencyService.getCurrency(currencyCode);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CurrencyDetailsDto addCurrency(@RequestBody AddCurrencyRequest request) {
        return currencyService.addCurrency(request);
    }
}
