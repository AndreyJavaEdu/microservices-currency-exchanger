package io.kamenskiyAndrey.currencyExchanger.currency.client;

import java.time.LocalDate;

public interface CurrencyDateHttpClient {
    String requestByDate(LocalDate date);
}
