package io.kamenskiyAndrey.currencyExchanger.currency.client;

import io.kamenskiyAndrey.currencyExchanger.currency.exceptions.CustomSendingException;

import java.time.LocalDate;

public interface CurrencyDateHttpClient {
    String requestByDate(LocalDate date) throws CustomSendingException;
}
