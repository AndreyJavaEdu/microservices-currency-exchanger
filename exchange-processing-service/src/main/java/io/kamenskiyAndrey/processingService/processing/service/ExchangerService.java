package io.kamenskiyAndrey.processingService.processing.service;

import io.kamenskiyAndrey.processingService.processing.domainModel.AccountEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class ExchangerService {

    private static final String CURRENCY_RUB = "RUB";
    private final AccountCreateService service;
    private final CurrencyService currencyService;

    /*
    Метод перевода валюты с одно счета на другой, в зависимости от условий:
    если у счета переводчика сумма не в рублях, а у счета получателя в рублях,
    если у счета переводчика и у счета получателя суммы не в рублях.
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public BigDecimal exchangeCurrency(String uuid, Long fromAccount, Long toAccount, BigDecimal ammount) {
        AccountEntity source = service.getAccountById(fromAccount); //получаем объект счета котрый будет отправлять сумму на другой счет
        AccountEntity target = service.getAccountById(toAccount); // объект счета который будет получать деньги с другого счета

        BigDecimal result;
            //Проверка если код валют на обоих счетах в РУБЛЯХ.
        if (CURRENCY_RUB.equals(source.getCurrencyCode()) && CURRENCY_RUB.equals(target.getCurrencyCode())) {
            result = moneyTransferFromOneAccToOther(uuid, source, target, ammount);

            //Проверка если код валюты отправителя НЕ В РУБЛЯХ, а получателя в РУБЛЯХ
        } else if (!CURRENCY_RUB.equals(source.getCurrencyCode()) && CURRENCY_RUB.equals(target.getCurrencyCode())) {
            BigDecimal currencyRate = currencyService.loadCurrencyRate(source.getCurrencyCode()); //получили катировку валюты отправителя
            result = exchangeWithDifferenceOfOneCurrCode(uuid, source, target, currencyRate, ammount);

            //Проверка если код валюты отправителя В РУБЛЯХ, а получателя НЕ В РУБЛЯХ
        } else if (CURRENCY_RUB.equals(source.getCurrencyCode()) && !CURRENCY_RUB.equals(target.getCurrencyCode())) {
            BigDecimal currencyRate = currencyService.loadCurrencyRate(target.getCurrencyCode()); //получили катировку валюты получателя
            BigDecimal moneyConversion = new BigDecimal(BigInteger.ONE).divide(currencyRate, 5, RoundingMode.HALF_DOWN);
            result = exchangeWithDifferenceOfOneCurrCode(uuid, source, target, moneyConversion, ammount);

            //Проверка если код валюты отправителя не в рублях и получателя тоже не в рублях 
        } else if (!CURRENCY_RUB.equals(source.getCurrencyCode()) && !CURRENCY_RUB.equals(target.getCurrencyCode())) {
            BigDecimal curRateSource = currencyService.loadCurrencyRate(source.getCurrencyCode()); //получили катировку валюты отправителя
            BigDecimal curRateTarget = currencyService.loadCurrencyRate(target.getCurrencyCode()); //получили катировку валюты получателя
            result = exchangeWithDifferenceOfAllCurrCode(uuid, curRateSource, curRateTarget, source, target, ammount);
        } else {
            throw new IllegalStateException("Unknown behavior");
        }
        return result;
    }
    /*
    Вспомогательный метод веревода валюты с разными катировками отличными от рублей
     */
    public BigDecimal exchangeWithDifferenceOfAllCurrCode(String uuid, BigDecimal curRateSource, BigDecimal curRateTarget
            , AccountEntity source, AccountEntity target, BigDecimal ammount) {
        service.addMoneyToAccount(uuid, source.getId(), ammount.negate());

        BigDecimal rub = ammount.multiply(curRateSource); //сконвертировали сумму перевода в рубли
        BigDecimal result = rub.divide(curRateTarget, 5, RoundingMode.HALF_DOWN); //сконвертировали сумму перевода в рублях в валюту получателя

        service.addMoneyToAccount(uuid, target.getId(),result);
        return result;
    }
    /*
    Вспомогательный метод перевода валюты с одного счета на другой счет если валюта с кодом RUB
     */
    public BigDecimal moneyTransferFromOneAccToOther(String uuid, AccountEntity source
            , AccountEntity target, BigDecimal amount) {
        service.addMoneyToAccount(uuid, source.getId(), amount.negate());
        service.addMoneyToAccount(uuid, target.getId(), amount);
        return amount;
    }
    /*
    Вспомогательный метод перевода валюты когда валюта в одном из счету в рублях
     */
    public BigDecimal exchangeWithDifferenceOfOneCurrCode(String uuid, AccountEntity source
            , AccountEntity target, BigDecimal currencyRate, BigDecimal amount) {
        service.addMoneyToAccount(uuid, source.getId(), amount.negate());
        BigDecimal moneyConversion = amount.multiply(currencyRate); // конвертация суммы денег перевода в ту валюту кому переводим
        service.addMoneyToAccount(uuid, target.getId(), moneyConversion);
        return moneyConversion;
    }
}
