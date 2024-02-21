package io.kamenskiyAndrey.currencyExchanger.currency.exceptions;

import java.math.BigDecimal;

public class CustomCurrencyCodeException extends RuntimeException{
    private BigDecimal value;

    public CustomCurrencyCodeException(String message) {
        super(message);
    }

    public CustomCurrencyCodeException(String message, Throwable cause) {
        super(message, cause);
        this.value = value;
    }

    public CustomCurrencyCodeException(String message, BigDecimal value) {
        super(message);
        this.value = value;
    }
}
