package io.kamenskiyAndrey.currencyExchanger.currency.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.kamenskiyAndrey.currencyExchanger.currency.client.CurrencyDateHttpClient;
import io.kamenskiyAndrey.currencyExchanger.currency.exceptions.CustomConfigurationXMLException;
import io.kamenskiyAndrey.currencyExchanger.currency.exceptions.CustomCurrencyCodeException;
import io.kamenskiyAndrey.currencyExchanger.currency.exceptions.CustomParsingStringException;
import io.kamenskiyAndrey.currencyExchanger.currency.exceptions.CustomSendingException;
import io.kamenskiyAndrey.currencyExchanger.currency.schemas.ValCurs;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
   private static final Logger LOGGER = LoggerFactory.getLogger(CurService.class);
    private final CurrencyDateHttpClient client;
    private final Cache<LocalDate, Map<String, BigDecimal>> cache; //поле для кеша

    public CurService(CurrencyDateHttpClient client) {
        this.cache = CacheBuilder.newBuilder().build();
        this.client = client;
    }

    //Метод получения курса валюты из списка кеша в зависимости от типа валюты (код валюты - EUR, USD...).
    public BigDecimal requestByCurrencyCode(String currencyCode) {
        try {
            BigDecimal value = cache.get(LocalDate.now(), this::callAllByCurrentDate).get(currencyCode);
            if (value==null) {
                throw new CustomCurrencyCodeException("Некорректно введен код валюты, код валюты должен состоять" +
                        "из 3 заглавных латинских букв,например USD, EUR!!!");
            }
            LOGGER.info("Котировка валюты value : {}", value);
            return value;

        } catch (ExecutionException e) {
            LOGGER.error("Ошибка при загрузки значения для ключа Кеша, необходимо проверить входные " +
                    "данные currencyCode {} и ", currencyCode, e);
            throw new CustomCurrencyCodeException("Не возможно получить значение из Map Кеша по ключу currencyCode", e);
        }
    }

    //Метод получения Мапы с ключем - код валюты типа String и значение - курс валюты типа BigDecimal
    private Map<String, BigDecimal> callAllByCurrentDate() throws CustomSendingException {

            var xml = client.requestByDate(LocalDate.now()); // получим xml - в нем будут котировки всех валют за текущую дату
            ValCurs valCurs = unmarshall(xml);
            List<ValCurs.Valute> valute = valCurs.getValute(); //получили список значений валют
            Map<String, BigDecimal> differentCurrency = valute.stream().collect(Collectors.toMap(ValCurs.Valute::getCharCode
                    , items -> parseWithLocaleStringValue(items.getValue())));
            return differentCurrency;
    }

    //Метод в котором распарсили String значение курса валюты в BigDecimal
    private BigDecimal parseWithLocaleStringValue(String currency) {
        try {
            double v = NumberFormat.getNumberInstance(Locale.getDefault()).parse(currency).doubleValue(); //преобразуем текстовое представление числа в тип double в соответствии с текущей Локалью
            return BigDecimal.valueOf(v);
        } catch (ParseException e) {
            LOGGER.error("Некорректные входные данные - String currency {}:", currency, e);
            throw new CustomParsingStringException("Параметр String currency не отвечает требованиям парсера", e);
        }
    }

    //возвращает объект типа ValCurs, полученный после разбора XML-строки.
    private ValCurs unmarshall(String xml) {
        try (StringReader reader = new StringReader(xml)) { //читает данные из строки xml
            JAXBContext context = JAXBContext.newInstance(ValCurs.class); //создали контекст для класса, соответствующего структуре XML (ValCurs)
            return (ValCurs) context.createUnmarshaller().unmarshal(reader); //получаем объект типа ValCurs
        } catch (JAXBException e) {
            LOGGER.error("Требуется проверка XML-схемы, проблемы с парсингом xml {}", xml, e);
            throw new CustomConfigurationXMLException("Ошибка при парсинге xml строки в объект", e);
        }
    }
}
