package com.narektm.exchangeratesapi.service;

import com.narektm.exchangeratesapi.converter.CurrencyConverter;
import com.narektm.exchangeratesapi.dto.CurrencyDetailsDto;
import com.narektm.exchangeratesapi.dto.CurrencySummaryDto;
import com.narektm.exchangeratesapi.dto.ExchangeRatesResponse;
import com.narektm.exchangeratesapi.exception.ExternalApiException;
import com.narektm.exchangeratesapi.exception.NotFoundException;
import com.narektm.exchangeratesapi.persistence.entity.CurrencyEntity;
import com.narektm.exchangeratesapi.persistence.repository.CurrencyRepository;
import com.narektm.exchangeratesapi.web.model.AddCurrencyRequest;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CurrencyService {

    private static final Map<String, CurrencyDetailsDto> EXCHANGE_RATES = new ConcurrentHashMap<>();
    private static final int THREAD_POOL_SIZE = 5;

    private final CurrencyRepository currencyRepository;
    private final ExchangeRatesService exchangeRatesService;
    private final CurrencyConverter currencyConverter;

    public CurrencyService(CurrencyRepository currencyRepository,
                           ExternalExchangeRatesService exchangeRatesService,
                           CurrencyConverter currencyConverter) {
        this.currencyRepository = currencyRepository;
        this.exchangeRatesService = exchangeRatesService;
        this.currencyConverter = currencyConverter;
    }

    public Set<CurrencySummaryDto> getAllCurrencies() {
        return EXCHANGE_RATES.keySet().stream()
                .map(currencyConverter::toCurrencySummaryDto)
                .collect(Collectors.toSet());
    }

    public CurrencyDetailsDto getCurrency(String currencyCode) {
        return Optional.ofNullable(EXCHANGE_RATES.get(currencyCode))
                .orElseThrow(() -> new NotFoundException("Currency with code %s not found".formatted(currencyCode)));
    }

    public CurrencyDetailsDto addCurrency(AddCurrencyRequest request) {
        String currencyCode = request.currencyCode();
        if (EXCHANGE_RATES.containsKey(currencyCode)) {
            throw new IllegalArgumentException("Currency with code %s already exists".formatted(currencyCode));
        }

        log.info("Adding a new currency with code {}...", currencyCode);
        ExchangeRatesResponse response = fetchExchangeRates(currencyCode);
        CurrencyEntity currencyEntity = currencyConverter.toCurrencyEntity(response);
        CurrencyDetailsDto currencyDetailsDto = saveCurrency(currencyEntity);
        log.info("Currency with code {} added successfully", currencyCode);

        return currencyDetailsDto;
    }

    private CurrencyDetailsDto saveCurrency(CurrencyEntity currencyEntity) {
        CurrencyEntity savedCurrencyEntity = currencyRepository.save(currencyEntity);
        CurrencyDetailsDto currencyDetailsDto = currencyConverter.toCurrencyDetailsDto(savedCurrencyEntity);
        EXCHANGE_RATES.put(currencyEntity.getCode(), currencyDetailsDto);

        return currencyDetailsDto;
    }

    @Scheduled(cron = "0 10 0 * * *", zone = "UTC") // every day at 00:10:00 UTC
    public void updateExchangeRates() {
        Set<String> allCodes = currencyRepository.getAllCodes();
        log.info("Updating exchange rates...");
        fetchExchangeRatesInParallel(allCodes)
                .forEach(response -> {
                    CurrencyEntity currencyEntity = currencyRepository.findByCode(response.baseCurrencyCode());
                    currencyEntity.setRates(response.rates());
                    currencyEntity.setUpdatedOn(LocalDateTime.now(ZoneId.of("UTC")));
                    saveCurrency(currencyEntity);
                });
        log.info("Updated exchange rates");
    }

    private ExchangeRatesResponse fetchExchangeRates(String currencyCode) {
        return Optional.ofNullable(exchangeRatesService.fetchExchangeRates(currencyCode).getBody())
                .orElseThrow(() -> new ExternalApiException(
                        "Something went wrong while fetching data for currency with code %s"
                                .formatted(currencyCode)));
    }

    private List<ExchangeRatesResponse> fetchExchangeRatesInParallel(Set<String> currencyCodes) {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        new ThreadPoolExecutor(5, 19, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        try {
            List<CompletableFuture<ExchangeRatesResponse>> futures = currencyCodes.stream()
                    .map(currencyCode -> CompletableFuture.supplyAsync(() -> {
                        // to prevent exceptions in individual tasks from affecting the execution of other tasks
                        try {
                            return fetchExchangeRates(currencyCode);
                        } catch (Exception e) {
                            log.warn("Failed to fetch exchange rates for currency with code {}, exception message: {}",
                                    currencyCode, e.getMessage());
                            return null;
                        }
                    }, executorService))
                    .toList();

            return futures.stream()
                    .map(CompletableFuture::join)
                    .filter(Objects::nonNull)
                    .toList();
        } finally {
            gracefullyShutDownExecutorService(executorService);
        }
    }

    private static void gracefullyShutDownExecutorService(ExecutorService executorService) {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("Interrupted!", e);
            executorService.shutdownNow();
        }
    }

    @PostConstruct
    public void initExchangeRatesFromDb() {
        log.info("Initializing EXCHANGE_RATES from database...");
        currencyRepository.findAll().forEach(currencyEntity ->
                EXCHANGE_RATES.put(currencyEntity.getCode(),
                        currencyConverter.toCurrencyDetailsDto(currencyEntity)));
        log.info("Initialization complete. {} currencies loaded.", EXCHANGE_RATES.size());
    }
}
