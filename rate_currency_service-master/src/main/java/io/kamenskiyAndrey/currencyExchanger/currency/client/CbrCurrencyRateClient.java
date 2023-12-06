package io.kamenskiyAndrey.currencyExchanger.currency.client;

import io.kamenskiyAndrey.currencyExchanger.currency.config.CurrencyClientConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class CbrCurrencyRateClient implements CurrencyDateHttpClient {
    private static final String DATE_PATTERN = "dd/MM/yyyy";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);

    private final CurrencyClientConfig clientConfig;

    @Override
    public String requestByDate(LocalDate date) {
        var baseUrl = clientConfig.getUrl(); // первичный адрес с ЦБР
        var client = HttpClient.newHttpClient(); //создали самого киента
        var url = buildUrlRequest(baseUrl, date);  //формируем url уже с запросами на конкретную дату

        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build(); // запрос клиента
            //отправляем request на внешний ресурс
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        }catch (Exception e){
            throw new RuntimeException(e);
        }

    }

    //Строем URL
    private String buildUrlRequest(String baseUrl, LocalDate date) {
        return UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("date_req", DATE_TIME_FORMATTER.format(date))
                .build().toUriString();
    }
}
