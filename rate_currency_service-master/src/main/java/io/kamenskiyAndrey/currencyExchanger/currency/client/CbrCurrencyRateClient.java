package io.kamenskiyAndrey.currencyExchanger.currency.client;

import io.kamenskiyAndrey.currencyExchanger.currency.config.CurrencyClientConfig;
import io.kamenskiyAndrey.currencyExchanger.currency.exceptions.CustomConfigurationXMLException;
import io.kamenskiyAndrey.currencyExchanger.currency.exceptions.CustomSendingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
@Slf4j
public class CbrCurrencyRateClient implements CurrencyDateHttpClient {
    private static final String DATE_PATTERN = "dd/MM/yyyy";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);

    private final CurrencyClientConfig clientConfig;

    @Override
    public String requestByDate(LocalDate date) throws CustomSendingException {
        var baseUrl = clientConfig.getUrl(); // первичный адрес с ЦБР
        var client = HttpClient.newHttpClient(); //создали самого клиента
        var url = buildUrlRequest(baseUrl, date);  //формируем url уже с запросами на конкретную дату

        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build(); // запрос клиента
            //отправляем request на внешний ресурс
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        }catch (IOException | InterruptedException | IllegalArgumentException  ex){
            log.error("Ошибка прерывания метода send, нет связи с сервером ЦБР, {}", url, ex);
            throw new CustomSendingException("Метод send не выполнил отправку запроса по адресу", url, ex);
        }
    }

    //Строем URL
    private String buildUrlRequest(String baseUrl, LocalDate date) {
            return UriComponentsBuilder.fromHttpUrl(baseUrl)
                    .queryParam("date_req", DATE_TIME_FORMATTER.format(date))
                    .build().toUriString();
        }
    }

