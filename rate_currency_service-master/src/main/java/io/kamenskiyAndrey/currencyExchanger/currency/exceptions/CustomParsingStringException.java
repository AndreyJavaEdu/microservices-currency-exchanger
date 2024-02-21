package io.kamenskiyAndrey.currencyExchanger.currency.exceptions;

public class CustomParsingStringException extends RuntimeException{
    public CustomParsingStringException(String message, Throwable cause) {
        super(message, cause);
    }
}
