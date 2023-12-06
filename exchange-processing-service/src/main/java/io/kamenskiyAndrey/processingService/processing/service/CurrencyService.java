package io.kamenskiyAndrey.processingService.processing.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

//Задача данного сервиса сделать Http вызов в наш сервис котировок по коду валют
@Service
@RequiredArgsConstructor
public class CurrencyService {
    private final RestTemplate restClient;

    @Value("${service.currency.url}")
    private String currencyUrl;

    public BigDecimal loadCurrencyRate(String code){
        return restClient.getForObject(currencyUrl + "/money/quotation/{code}", BigDecimal.class,  code);
    }
}
