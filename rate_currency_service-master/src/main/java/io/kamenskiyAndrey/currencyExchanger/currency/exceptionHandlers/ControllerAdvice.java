package io.kamenskiyAndrey.currencyExchanger.currency.exceptionHandlers;

import io.kamenskiyAndrey.currencyExchanger.currency.dtoException.Response;
import io.kamenskiyAndrey.currencyExchanger.currency.exceptions.CustomConfigurationXMLException;
import io.kamenskiyAndrey.currencyExchanger.currency.exceptions.CustomCurrencyCodeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@org.springframework.web.bind.annotation.ControllerAdvice
public class ControllerAdvice  {
    @ExceptionHandler(CustomCurrencyCodeException.class)
    public ResponseEntity<Response> handleExceptionCurrencyCod(CustomCurrencyCodeException ex){
        Response response = new Response(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
