package io.kamenskiyAndrey.currencyExchanger.currency.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.kamenskiyAndrey.currencyExchanger.currency.client.CurrencyDateHttpClient;
import io.kamenskiyAndrey.currencyExchanger.currency.schemas.ValCurs;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import org.springframework.stereotype.Service;

import java.io.StringReader;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class CurService {
    private CurrencyDateHttpClient client;
    private final Cache<LocalDate, Map<String, BigDecimal>> cache; //поле для кеша

    public CurService(CurrencyDateHttpClient client) {
        this.cache = CacheBuilder.newBuilder().build();
        this.client = client;
    }

    //Метод получения списка курса валюты в зависимости от типа валюты (код валюты - EUR, USD...) из Кэша
    public BigDecimal requestByCurrencyCode(String currencyCode) {

        try {
            return cache.get(LocalDate.now(), this::callAllByCurrentDate).get(currencyCode);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    //Метод получения Мапы с ключем - код валюты типа String и значение - курс валюты типа BigDecimal
    private Map<String, BigDecimal> callAllByCurrentDate() {
        var xml = client.requestByDate(LocalDate.now()); // получим xml - в нем будут котировки всех валют за текщую дату
        ValCurs valCurs = unmarshall(xml);
        List<ValCurs.Valute> valute = valCurs.getValute(); //получили список значений валют
        Map<String, BigDecimal> allValuesOfEachCurrency = valute.stream().collect(Collectors.toMap(ValCurs.Valute::getCharCode
                , items -> parseWithLocaleStringValue(items.getValue())));
        return allValuesOfEachCurrency;
    }

    //Метод в котором распарсили String значение курса валюты в BigDecimal
    private BigDecimal parseWithLocaleStringValue(String currency) {
        try {
            double v = NumberFormat.getNumberInstance(Locale.getDefault()).parse(currency).doubleValue(); //преобразуем текстовое представление числа в тип double в соответствии с текущей Локалью
            return BigDecimal.valueOf(v);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    //возвращает объект типа ValCurs, полученный после разбора XML-строки.
    private ValCurs unmarshall(String xml) {
        try (StringReader reader = new StringReader(xml)) { //читает данные из строки xml
            JAXBContext context = JAXBContext.newInstance(ValCurs.class); //создали контекст для класса, соответствующего структуре XML (ValCurs)
            return (ValCurs) context.createUnmarshaller().unmarshal(reader); //получаем объект типа ValCurs
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }
}
