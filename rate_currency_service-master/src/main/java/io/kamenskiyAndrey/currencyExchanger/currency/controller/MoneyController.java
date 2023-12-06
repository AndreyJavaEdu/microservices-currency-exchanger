package io.kamenskiyAndrey.currencyExchanger.currency.controller;

import io.kamenskiyAndrey.currencyExchanger.currency.service.CurService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping(path = "/money") //задали начальный путь
@RequiredArgsConstructor
public class MoneyController {

    private final CurService currencyService;

    //Метод получения котировки валюты
    @GetMapping("/quotation/{code}")
    public BigDecimal getCurrencyQuotation(@PathVariable("code") String code){
        return currencyService.requestByCurrencyCode(code);
    }
}
