package io.kamenskiyAndrey.currencyExchanger.currency.exceptions;

public class CustomConfigurationXMLException extends RuntimeException{
    public CustomConfigurationXMLException(String message, Throwable cause) {
        super(message, cause);
    }

    public CustomConfigurationXMLException(String message) {
        super(message);
    }
}
