package io.kamenskiyAndrey.currencyExchanger.currency.exceptions;

public class CustomSendingException extends Exception{
    public CustomSendingException(String message, Throwable cause) {
        super(message, cause);
    }

    public CustomSendingException(String message) {
        super(message);
    }

    public CustomSendingException(String s, String url, Exception ex) {
    }
}
