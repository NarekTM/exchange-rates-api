package com.narektm.exchangeratesapi.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.narektm.exchangeratesapi.dto.CurrencyDetailsDto;
import com.narektm.exchangeratesapi.dto.CurrencySummaryDto;
import com.narektm.exchangeratesapi.persistence.repository.CurrencyRepository;
import com.narektm.exchangeratesapi.testutils.IntegrationTestUtil;
import com.narektm.exchangeratesapi.testutils.ReflectionTestUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.spring.EnableWireMock;

import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@EnableWireMock
class CurrencyControllerIntegrationTest {

    private static final String GBP = "GBP";
    private static final String BASE = "base";
    private static final String LATEST = "/latest";
    private static final String CURRENCY_CODE = "currencyCode";
    private static final String API_V_1_0_CURRENCIES = "/api/v1.0/currencies";
    private static final String EXPECTED_BODY = "{\"base\": \"GBP\", \"rates\": {\"USD\": 1.25, \"EUR\": 1.20}}";

    @Container
    @ServiceConnection
    @SuppressWarnings("resource")
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("test_db")
            .withUsername("test_user")
            .withPassword("test_password");

    private static WireMockServer wireMockServer;

    @LocalServerPort
    private int port;

    @Autowired
    private CurrencyRepository currencyRepository;

    @DynamicPropertySource
    static void setUpProperties(DynamicPropertyRegistry registry) {
        registry.add("exchange.rates.external-api.base-uri", () -> "http://localhost:" + wireMockServer.port());
    }

    @BeforeAll
    static void startWireMockServer() {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();
    }

    @AfterAll
    static void stopWireMockServer() {
        wireMockServer.stop();
    }

    @BeforeEach
    void setUpWireMock() {
        WireMock.configureFor("localhost", 8081);
    }

    @AfterEach
    void cleanUp() throws Exception {
        currencyRepository.deleteAll();

        ReflectionTestUtil.getExchangeRates().clear();
    }

    @Test
    void testGetAllCurrencies_shouldBeEmpty_Success() {
        var response = IntegrationTestUtil.getAllCurrencies(port, API_V_1_0_CURRENCIES);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<CurrencySummaryDto> body = response.getBody();
        assertThat(body).isNotNull()
                .isEmpty();
    }

    @Test
    void testGetAllCurrencies_shouldNotBeEmpty_Success() {
        wireMockServer.stubFor(get(urlPathEqualTo(LATEST))
                .withQueryParam(BASE, equalTo(GBP))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBody(EXPECTED_BODY)));

        IntegrationTestUtil.post(port, API_V_1_0_CURRENCIES, Map.of(CURRENCY_CODE, GBP));

        var response = IntegrationTestUtil.getAllCurrencies(port, API_V_1_0_CURRENCIES);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<CurrencySummaryDto> body = response.getBody();
        assertThat(body).isNotNull()
                .hasSize(1)
                .anyMatch(currencySummaryDto ->
                        GBP.equals(currencySummaryDto.code()));
    }

    @Test
    void testGetCurrency_Success() {
        wireMockServer.stubFor(get(urlPathEqualTo(LATEST))
                .withQueryParam(BASE, equalTo(GBP))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBody(EXPECTED_BODY)));

        IntegrationTestUtil.post(port, API_V_1_0_CURRENCIES, Map.of(CURRENCY_CODE, GBP));

        var response = IntegrationTestUtil.getCurrency(port, API_V_1_0_CURRENCIES + "/GBP");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        var body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.code()).isEqualTo(GBP);
        assertThat(body.rates()).hasSize(2);
    }

    @Test
    void testAddCurrency_Success() {
        Map<String, Object> requestPayload = Map.of(CURRENCY_CODE, GBP);

        wireMockServer.stubFor(get(urlPathEqualTo(LATEST))
                .withQueryParam(BASE, equalTo(GBP))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBody(EXPECTED_BODY)));

        var response = IntegrationTestUtil.post(port, API_V_1_0_CURRENCIES, requestPayload);

        var savedEntity = currencyRepository.findByCode(GBP);

        assertThat(savedEntity).isNotNull();
        assertThat(savedEntity.getId()).isNotNull();
        assertThat(savedEntity.getCode()).isEqualTo(GBP);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        CurrencyDetailsDto body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.code()).isEqualTo(GBP);
        assertThat(body.rates()).hasSize(2);
    }

    @Test
    void testAddCurrency_ExistingCurrency_Failure() {
        Map<String, Object> requestPayload = Map.of(CURRENCY_CODE, GBP);

        wireMockServer.stubFor(get(urlPathEqualTo(LATEST))
                .withQueryParam(BASE, equalTo(GBP))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBody(EXPECTED_BODY)));

        var successResponse =
                IntegrationTestUtil.post(port, API_V_1_0_CURRENCIES, requestPayload);
        assertThrows(Exception.class,
                () -> IntegrationTestUtil.post(port, API_V_1_0_CURRENCIES, requestPayload));

        assertThat(successResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }
}
