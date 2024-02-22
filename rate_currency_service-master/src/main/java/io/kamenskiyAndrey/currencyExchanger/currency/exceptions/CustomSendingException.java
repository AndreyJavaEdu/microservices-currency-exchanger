package io.kamenskiyAndrey.currencyExchanger.currency.exceptions;

public class CustomSendingException extends RuntimeException{
    public CustomSendingException(String message, Throwable cause) {
        super(message, cause);
    }

    public CustomSendingException(String message) {
        super(message);
    }

    public CustomSendingException(String s, String url, Throwable cause) {
    }
}
