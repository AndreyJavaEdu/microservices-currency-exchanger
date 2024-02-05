# Проект микросервисы - сервисы по обмене валют

Данный проект включает в себя несколько отдельных сервисов, которые взаимодействуют между собой.

**Микросервисы в проекте:**
- [rate_currency_service-master](rate_currency_service-master) - микросервис по получению котировок валют с https://www.cbr.ru/.


- [exchange-processing-service](exchange-processing-service) - микросервис по созданию счета пользователя, 
а также реализация перевода денежных средств с одного счета на другой, отправка событий по операций со счетом в топик Кафки.
Вывод информации по счетам для определенного пользователя.


- [eureka-service](eureka-service) - Сервис регистрации и обнаружения всех микросервисов на сервере Eureka. 
Spring Cloud Eureka обычно используется в среде микросервисов для регистрации, 
обнаружения и управления взаимодействием между службами. Это помогает сделать архитектуру приложения более гибкой, 
масштабируемой и отказоустойчивой.


- [identity-service-new](identity-service-new) - микросервис по обеспичению безопасности всех микросервисов - 
в данном микросервисе реализованы запросы на регистрацию пользователя, сохранение его логина и пароля (в закодированном формате)
в БД и хранение учетных. Также данный сервис реализует получение JWT токена пользователем и проверка токена (валидация).


- [history-service](history-service) - микросервис по получению истории операций со 
счетами конкретного пользователя из топика Кафки и сохранения событий в БД для хранения истории. Микросервис реализован на Kotlin.


- [notification-bot](notification-bot) - Микросервис телеграмм бот, реализующий оповещение
конкретного пользователя об операциях с денежными счетами. Микросервис реализован на Kotlin.


- [gateway-service](gateway-service) - сервис Spring Cloud Gateway - это централизованная точка входа для 
управления маршрутизацией и обеспечения безопасности для 
микросервисной архитектуры. Данный сервис представляет собой общий шлюз для обработки запросов
поступающих в микросервисы [rate_currency_service-master](rate_currency_service-master), 
[exchange-processing-service](exchange-processing-service), [history-service](history-service), [identity-service-new](identity-service-new)
из Фронтенд приложения пользователя (в нашем случае демонстрация будет производиться с помощью Postman).
Также данный шлюз содержит фильтр и проверяет, содержит ли запрос пользователя JWT токен.

  
### Общая схема проекта:
![Блок схема проекта микросервисы.png](https://github.com/AndreyJavaEdu/microservices-currency-exchanger/blob/03194480b543b4c3d1d750758b0748c49669cc6e/%D0%A1%D1%85%D0%B5%D0%BC%D1%8B%20%D0%B4%D0%BB%D1%8F%20README/%D0%91%D0%BB%D0%BE%D0%BA%20%D1%81%D1%85%D0%B5%D0%BC%D0%B0%20%D0%BF%D1%80%D0%BE%D0%B5%D0%BA%D1%82%D0%B0%20%D0%BC%D0%B8%D0%BA%D1%80%D0%BE%D1%81%D0%B5%D1%80%D0%B2%D0%B8%D1%81%D1%8B.png)


## Объяснение как работают микросервисы

### 1. Микросервис по получению котировок валют ([rate_currency_service-master](rate_currency_service-master))

![Схема работы микросервиса текущих котировок валют.png](https://github.com/AndreyJavaEdu/microservices-currency-exchanger/blob/4eb6724306484e3bb167447175b80e81e5dbc151/%D0%A1%D1%85%D0%B5%D0%BC%D1%8B%20%D0%B4%D0%BB%D1%8F%20README/%D0%A1%D1%85%D0%B5%D0%BC%D0%B0%20%D1%80%D0%B0%D0%B1%D0%BE%D1%82%D1%8B%20%D0%BC%D0%B8%D0%BA%D1%80%D0%BE%D1%81%D0%B5%D1%80%D0%B2%D0%B8%D1%81%D0%B0%20%D1%82%D0%B5%D0%BA%D1%83%D1%89%D0%B8%D1%85%20%D0%BA%D0%BE%D1%82%D0%B8%D1%80%D0%BE%D0%B2%D0%BE%D0%BA%20%D0%B2%D0%B0%D0%BB%D1%8E%D1%82.png)

Техлогии и библиотеки: spring-boot-starter 3.2.0, Lombok, Spring WEB, зависимость com.google.guava (кеш), 
spring-cloud-starter-netflix-eureka-client.

Данный микросервис получает данные из xml с ЦБР по адресу https://cbr.ru/scripts/XML_daily.asp?date_req=02/03/2002 
для получения котировок на заданный день.
Для этого используется xsd схема xml документа котировок всех валют, 
скаченная с https://cbr.ru/StaticHtml/File/92172/Valuta.xsd.
В [pom.xml](rate_currency_service-master%2Fpom.xml) добавлены зависимости org.glassfish.jaxb, а также
настроен плагин с указанием пути куда будет сгенерирован POJO класс из xsd схемы  
([ValCurs.xsd](rate_currency_service-master%2Fsrc%2Fmain%2Fresources%2Fxsd%2FValCurs.xsd)), 
которую мы скачали с ЦБР. Применяется данный плагин нами лишь один раз для получения
класса [ValCurs.java](rate_currency_service-master%2Fsrc%2Fmain%2Fjava%2Fio%2FkamenskiyAndrey%2FcurrencyExchanger%2Fcurrency%2Fschemas%2FValCurs.java).
Данный POJO класс [ValCurs.java](rate_currency_service-master%2Fsrc%2Fmain%2Fjava%2Fio%2FkamenskiyAndrey%2FcurrencyExchanger%2Fcurrency%2Fschemas%2FValCurs.java),
который сгенерировался в пакете [schemas](rate_currency_service-master%2Fsrc%2Fmain%2Fjava%2Fio%2FkamenskiyAndrey%2FcurrencyExchanger%2Fcurrency%2Fschemas)
полностью соответствует XML схеме котировок валют.

В данном микросервисе имеется пакет [service](rate_currency_service-master%2Fsrc%2Fmain%2Fjava%2Fio%2FkamenskiyAndrey%2FcurrencyExchanger%2Fcurrency%2Fservice)
с классом сервисом [CurService.java](rate_currency_service-master%2Fsrc%2Fmain%2Fjava%2Fio%2FkamenskiyAndrey%2FcurrencyExchanger%2Fcurrency%2Fservice%2FCurService.java).
Данный класс реализует метод requestByCurrencyCode(), который принимает в качестве аргумента
код валюты ввиде 3-х заглавных букв (например RUB, EUR), а на выходе метод
возвращает курс данной валюты в формате BigDecimal.
Для оптимизации используется guava кеш, чтобы постоянно не обращаться к сайту ЦБР для получения всех 
курсов валют (кеш настраивается с помощью конструктора согласно документации):
```java
 private final Cache<LocalDate, Map<String, BigDecimal>> cache; //поле для кеша
    public CurService(CurrencyDateHttpClient client) {
        this.cache = CacheBuilder.newBuilder().build();
        this.client = client;
    }
```
Ключ кеша является дата LocalDate, т.к. каждый курс валюты привязан к дате и каждый день курс валюты меняется.
Значение кеша является Map с ключем - код валюты, и значением - котировка валюты.
В теле метода requestByCurrencyCode() вызывается вспомогательный метод callAllByCurrentDate(),
который с помощью стандартного HttpClient в java достает строку xml с ЦБР и далее производится
анмаршаллинг данной строки в объект Java.
Метод запроса к ЦБР с помощью HttpClient - requestByDate() принимающий дату реализован в классе [CbrCurrencyRateClient.java](rate_currency_service-master%2Fsrc%2Fmain%2Fjava%2Fio%2FkamenskiyAndrey%2FcurrencyExchanger%2Fcurrency%2Fclient%2FCbrCurrencyRateClient.java) который помечен как компонет Спринг:
```java
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
```
Итак в самом сервисе мы инжектим бин  CbrCurrencyRateClient и используем его метод для получения
xml строки с котировками всех валют за определенную дату.
Далее во вспомогательном методе parseWithLocaleStringValue() парсим строку xml и получаем значение уже
в формате BigDecimal курса валюты в зависимости от кода валюты и текущей локали.
```java
 //Метод в котором распарсили String значение курса валюты в BigDecimal
    private BigDecimal parseWithLocaleStringValue(String currency) {
        try {
            double v = NumberFormat.getNumberInstance(Locale.getDefault()).parse(currency).doubleValue(); //преобразуем текстовое представление числа в тип double в соответствии с текущей Локалью
            return BigDecimal.valueOf(v);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
```
Далее этот вспомогательный метод используется в методе
callAllByCurrentDate() для получения значений в Map, у которой ключем будет являтся код валюты, а значение BigDecimal распарсенное из xml строки.
```java
    //Метод получения Мапы с ключем - код валюты типа String и значение - курс валюты типа BigDecimal
    private Map<String, BigDecimal> callAllByCurrentDate() {
        var xml = client.requestByDate(LocalDate.now()); // получим xml - в нем будут котировки всех валют за текщую дату
        ValCurs valCurs = unmarshall(xml);
        List<ValCurs.Valute> valute = valCurs.getValute(); //получили список значений валют
        Map<String, BigDecimal> allValuesOfEachCurrency = valute.stream().collect(Collectors.toMap(ValCurs.Valute::getCharCode
                , items -> parseWithLocaleStringValue(items.getValue())));
        return allValuesOfEachCurrency;
    }
```
Далее чтобы получить котировку валюты с помощью Http запроса мы создали
в пакете [controller](rate_currency_service-master%2Fsrc%2Fmain%2Fjava%2Fio%2FkamenskiyAndrey%2FcurrencyExchanger%2Fcurrency%2Fcontroller)
класс Rest контроллера - [MoneyController.java](rate_currency_service-master%2Fsrc%2Fmain%2Fjava%2Fio%2FkamenskiyAndrey%2FcurrencyExchanger%2Fcurrency%2Fcontroller%2FMoneyController.java)
в котором реализовали метод getCurrencyQuotation() с аннотацией GetMapping и эндпоинтом "/quotation/{code}".
В качестве аргумента метод будет принимать код валюты, который мы будет ему
предоставлять в шаблонной части uri нашего get запроса.
```java
    //Метод получения котировки валюты
    @GetMapping("/quotation/{code}")
    public BigDecimal getCurrencyQuotation(@PathVariable("code") String code){
        return currencyService.requestByCurrencyCode(code);
    }
```
В качестве демонстрации прилагаю скриншот работающего приложения и проверка 
работы запроса через Postman:
![Start currency service.png](https://github.com/AndreyJavaEdu/microservices-currency-exchanger/blob/readme-file/%D0%A1%D1%85%D0%B5%D0%BC%D1%8B%20%D0%B4%D0%BB%D1%8F%20README/Start%20currency%20service.png)

![Postman - Получение котировки валюты.png](https://github.com/AndreyJavaEdu/microservices-currency-exchanger/blob/readme-file/%D0%A1%D1%85%D0%B5%D0%BC%D1%8B%20%D0%B4%D0%BB%D1%8F%20README/Postman%20-%20%D0%9F%D0%BE%D0%BB%D1%83%D1%87%D0%B5%D0%BD%D0%B8%D0%B5%20%D0%BA%D0%BE%D1%82%D0%B8%D1%80%D0%BE%D0%B2%D0%BA%D0%B8%20%D0%B2%D0%B0%D0%BB%D1%8E%D1%82%D1%8B.png)
Все запросы к микросервисам будут производиться через порт
шлюза gate way localhost:8080.



### 2. Микросервис процессинга - [exchange-processing-service](exchange-processing-service)

![Диаграмма работы сервиса процессинга.png](https://github.com/AndreyJavaEdu/microservices-currency-exchanger/blob/readme-file/%D0%A1%D1%85%D0%B5%D0%BC%D1%8B%20%D0%B4%D0%BB%D1%8F%20README/%D0%94%D0%B8%D0%B0%D0%B3%D1%80%D0%B0%D0%BC%D0%BC%D0%B0%20%D1%80%D0%B0%D0%B1%D0%BE%D1%82%D1%8B%20%D1%81%D0%B5%D1%80%D0%B2%D0%B8%D1%81%D0%B0%20%D0%BF%D1%80%D0%BE%D1%86%D0%B5%D1%81%D1%81%D0%B8%D0%BD%D0%B3%D0%B0.png)

Техлогии и библиотеки: spring-boot-starter 3.2.0, Lombok, Spring WEB,
flyway-core, postgresql, spring-kafka, spring-cloud-starter-netflix-eureka-client

Пользователь через Рест интерфейс с помощью Рест контроллера 
отправляет запрос (на создание счета, пополнение счета или перевод денежных средств на
другой счет в какой либо валюте). При этом будет изменяться состояние счета, 
его баланс и в случае с переводом денежных средств между счетами, то это
должно производиться исходя из текущих котировок валют (т.е. мы должны
запрашивать текущую котировку валюты в Микросервисе котировок валют).

В качестве СУБД использован Docker образ Postgres. Имя базы данных - processing,
имя пользователя - postgres и пароль - password.
Структура БД создается с помощью Fly way. Скрипт для создания таблицы
находится в подпакете [migration](exchange-processing-service%2Fsrc%2Fmain%2Fresources%2Fdb%2Fmigration) в файле [V1__Account_table.sql](exchange-processing-service%2Fsrc%2Fmain%2Fresources%2Fdb%2Fmigration%2FV1__Account_table.sql):
```postgresql
create table ACCOUNT
(
    ID bigint not null primary key,
    USER_ID bigint not null,
    CURRENCY_CODE varchar(3) not null,
    BALANCE numeric not null
);
CREATE SEQUENCE ACCOUNT_SEQ;
```
В [application.yml](exchange-processing-service%2Fsrc%2Fmain%2Fresources%2Fapplication.yml)
мы настроили порт сервера данного микросервиса, имя микросервиса, а также сделали настройки jpa,
datasource и flyway:
```
server:
  port: 8090
  
spring:
  application:
    name: exchange-processing-service
  jpa:
    database: POSTGRESQL
    show-sql: true
    hibernate:
      ddl-auto: none

  datasource:
    url: jdbc:postgresql://${cloud.db-host}:5433/processing
    username: postgres
    password: password
    driver-class-name: org.postgresql.Driver

  flyway:
    enabled: true
    locations: classpath:db
    url: jdbc:postgresql://${cloud.db-host}:5433/processing
    user: postgres
    password: password
```
Доменная модель описана с помощью POJO класса [AccountEntity.java](exchange-processing-service%2Fsrc%2Fmain%2Fjava%2Fio%2FkamenskiyAndrey%2FprocessingService%2Fprocessing%2FdomainModel%2FAccountEntity.java)
в пакете [domainModel](exchange-processing-service%2Fsrc%2Fmain%2Fjava%2Fio%2FkamenskiyAndrey%2FprocessingService%2Fprocessing%2FdomainModel).
Доменная модель необходима, чтобы Spring понял с какой моделью и структурой БД ему работать.
Данный класс доменной модели, проаннотировали аннотациями jakarta.persistence для
указания имени таблицы, имени колонок и обозначения генератора последовательности sequence
для генерации нового идентификатора при создании нового счета.

Для поиска в БД и создания новых объектов реализован интерфейс репозитория [AccountRepository.java](exchange-processing-service%2Fsrc%2Fmain%2Fjava%2Fio%2FkamenskiyAndrey%2FprocessingService%2Fprocessing%2Frepository%2FAccountRepository.java)
в пакете [repository](exchange-processing-service%2Fsrc%2Fmain%2Fjava%2Fio%2FkamenskiyAndrey%2FprocessingService%2Fprocessing%2Frepository).

Также разработан сервис создания счета (аккаунта) - в пакете [service](exchange-processing-service%2Fsrc%2Fmain%2Fjava%2Fio%2FkamenskiyAndrey%2FprocessingService%2Fprocessing%2Fservice)
класс [AccountCreateService.java](exchange-processing-service%2Fsrc%2Fmain%2Fjava%2Fio%2FkamenskiyAndrey%2FprocessingService%2Fprocessing%2Fservice%2FAccountCreateService.java).
В данном классе-сервисе реализован метод по созданию нового счета createNewAccount(), в качестве аргумента метод
принимает объект класса DTO [NewAccountDTO.java](exchange-processing-service%2Fsrc%2Fmain%2Fjava%2Fio%2FkamenskiyAndrey%2FprocessingService%2Fprocessing%2Fdto%2FNewAccountDTO.java):
```java
@Data
public class NewAccountDTO {
    @JsonAlias("currency")
    private String currencyCode;

    @JsonAlias("user")
    private Long UserId;

}
```
В методе создания счета происходит создание нового объекта класса доменной модели AccountEntity, 
который замаплен на таблицу БД, и далее поля этого объекта заполняются данными из 
полученного объекта DTO и объект доменной модели с заполненными полями сохраняется в БД,
используя стандартный метод репозитория Spring Data JPA:
```java
    //Метод создания счета
    @Transactional
    public AccountEntity createNewAccount(NewAccountDTO dto) {
        //заполняем Entity объект данными из DTO
        var account = new AccountEntity();
        account.setCurrencyCode(dto.getCurrencyCode());
        account.setUserId(dto.getUserId());
        account.setBalance(new BigDecimal(0));
        //Сохраняем объект в базу
        var entityAccountObjectInBase = repository.save(account);
        return entityAccountObjectInBase;
    }
```
Сам метод возвращает объект нового аккаунта (счета), который будет сохранен в БД.

Также данный класс сервиса [AccountCreateService.java](exchange-processing-service%2Fsrc%2Fmain%2Fjava%2Fio%2FkamenskiyAndrey%2FprocessingService%2Fprocessing%2Fservice%2FAccountCreateService.java) содержит
реализацию ряда методов по работе со счетами:
 - Метод пополнения счета: public AccountEntity addMoneyToAccount(String uid, Long accountId, Operation operation, BigDecimal money);
 - Метод получения счета по идентификатору: public AccountEntity getAccountById(Long accountId);
 - Метод получения списка всех счетов у одного конкретного пользователя по его Id: public List<AccountEntity>getAllAccountsForUser(Long id);
 - Метод генерации события в Кафку: private AccountEvent createEvent(String uid, AccountEntity account, Long fromAccount, Operation operation, BigDecimal amount)

Также разработан класс контроллера [ProcessingAccountController.java](exchange-processing-service%2Fsrc%2Fmain%2Fjava%2Fio%2FkamenskiyAndrey%2FprocessingService%2Fprocessing%2Fcontroller%2FProcessingAccountController.java) в пакете [controller](exchange-processing-service%2Fsrc%2Fmain%2Fjava%2Fio%2FkamenskiyAndrey%2FprocessingService%2Fprocessing%2Fcontroller),
который позволяет по Rest передавать JSON для дальнейшего преобразования его в DTO объекты java, для использования 
методов класса сервиса пользователем из его Веб интерфейса (Postman).
Класс контроллера ProcessingAccountController имеет @RequestMapping("/processing"), а также
содержит следующие методы:
- Метод POST запроса на создание нвого счета: public AccountEntity createAccount(@RequestBody NewAccountDTO account, @RequestHeader String userId);
- Метод PUT запроса на пополнение счета: public AccountEntity putMoney(@PathVariable(value = "id") Long accountId, @RequestBody PutMoneyToAccountDTO data);
- Метод PUT запроса на перевод денежных средств с одного счета на другой: public BigDecimal exchangeCurrency (@PathVariable(value = "uid") String uid, @RequestBody ExchangeMoneyDTO data);
- Метод GET запроса на получения списка всех счетов по идентификатору пользователя - userId: public List<AccountEntity> getAllAccountsForUser(@RequestHeader String userId).

В пакете [service](exchange-processing-service%2Fsrc%2Fmain%2Fjava%2Fio%2FkamenskiyAndrey%2FprocessingService%2Fprocessing%2Fservice)
реализован класс-сервис [CurrencyService.java](exchange-processing-service%2Fsrc%2Fmain%2Fjava%2Fio%2FkamenskiyAndrey%2FprocessingService%2Fprocessing%2Fservice%2FCurrencyService.java) 
получения котировок валют из Микросервиса котировок валют [rate_currency_service-master](rate_currency_service-master).
В самом классе инжектится бин RestTemplate. В классе реализован метод принимающий код валюты (RUB, EUR и т.д.) и
с помощью метода getForObject() вызванного на бине RestTemplate производится получение котировки валюты в зависимости 
от кода валюты:
```java
 public BigDecimal loadCurrencyRate(String code){
        return restClient.getForObject("http://CURRENCY-RATE-SERVICE/money/quotation/{code}", BigDecimal.class,  code);
    }
```
Предварительно в конфигурационном классе [CloudConfig.java](exchange-processing-service%2Fsrc%2Fmain%2Fjava%2Fio%2FkamenskiyAndrey%2FprocessingService%2Fprocessing%2Fconfig%2FCloudConfig.java) 
мы сконфигурировали и получили бин RestTemplate для использования его при инъекции в класс-сервис
CurrencyService.

Сама реализация перевода средств с одного счета на другой вынесена в отдельный класс-сервис
[ExchangerService.java](exchange-processing-service%2Fsrc%2Fmain%2Fjava%2Fio%2FkamenskiyAndrey%2FprocessingService%2Fprocessing%2Fservice%2FExchangerService.java).
Данный класс инжектит бины [CurrencyService.java](exchange-processing-service%2Fsrc%2Fmain%2Fjava%2Fio%2FkamenskiyAndrey%2FprocessingService%2Fprocessing%2Fservice%2FCurrencyService.java) и
[AccountCreateService.java](exchange-processing-service%2Fsrc%2Fmain%2Fjava%2Fio%2FkamenskiyAndrey%2FprocessingService%2Fprocessing%2Fservice%2FAccountCreateService.java).
В данном классе-сервисе реализован основной метод перевода валюты с одного счета на другой public BigDecimal exchangeCurrency(String uuid, Long fromAccount, Long toAccount, BigDecimal ammount),
в котором прописаны возможные условия, которые могут возникнуть при обмене валют. Т.е.
рассмотрены такие условия:
- Проверка если код валюты на обоих счетах в РУБЛЯХ, то вызовется вспомогательный метод moneyTransferFromOneAccToOther();
- Проверка если код валюты отправителя НЕ В РУБЛЯХ, а получателя в РУБЛЯХ, то вызывается вспомогательный метод exchangeWithDifferenceOfOneCurrCode();
- Проверка если код валюты отправителя В РУБЛЯХ, а получателя НЕ В РУБЛЯХ, то вызывается вспомогательный метод также exchangeWithDifferenceOfOneCurrCode();
- Проверка если код валюты отправителя не в рублях и получателя тоже не в рублях, то вызывается вспомогательный метод exchangeWithDifferenceOfAllCurrCode().
Во спомогательных методах задействована логика уменьшения суммы с одного счета и увеличения на другом счете,
используя метод addMoneyToAccount() бина класса-сервиса [AccountCreateService.java](exchange-processing-service%2Fsrc%2Fmain%2Fjava%2Fio%2FkamenskiyAndrey%2FprocessingService%2Fprocessing%2Fservice%2FAccountCreateService.java).
Основной метод данного сервиса получает в параметре идентификаторы id счета отправителя и счета
получателя:
```java
@Transactional(isolation = Isolation.REPEATABLE_READ)
    public BigDecimal exchangeCurrency(String uuid, Long fromAccount, Long toAccount, BigDecimal ammount) {
    AccountEntity source = service.getAccountById(fromAccount); //получаем объект счета котрый будет отправлять сумму на другой счет
    AccountEntity target = service.getAccountById(toAccount); // объект счета который будет получать деньги с другого счета
}
```
Параметр String uuid нужен для обеспечения идемпотентности данной операции.
Итак в классе-контроллере [ProcessingAccountController.java](exchange-processing-service%2Fsrc%2Fmain%2Fjava%2Fio%2FkamenskiyAndrey%2FprocessingService%2Fprocessing%2Fcontroller%2FProcessingAccountController.java)
метод exchangeCurrency() представляет Рест интерфейс для пользователя для перевода денежных средств используя адрес
"http://localhost:8080/processing/exchange/{uid}":
```java
  //Метод PUT запроса на перевод денежных средств с одного счета на другой
    @PutMapping(path = "/exchange/{uid}")
    public BigDecimal exchangeCurrency (@PathVariable(value = "uid") String uid, @RequestBody ExchangeMoneyDTO data){
        return exchangerService.exchangeCurrency(uid, data.getFromAccountId(), data.getToAccountId(), data.getAmount());
    }
```
Запрос пользователем будет передан через общий порт шлюза gateway 8080. В запросе пользователь должен передать данные в виде 
JSON типа:
```json
{
    "uid": "379e5cb3-247f-4385-81ff-6a67d0ecfc9b234",
    "from": 3,
    "to": 5,
    "money": 500
}
```
JSON преобразуется в объект [ExchangeMoneyDTO.java](exchange-processing-service%2Fsrc%2Fmain%2Fjava%2Fio%2FkamenskiyAndrey%2FprocessingService%2Fprocessing%2Fdto%2FExchangeMoneyDTO.java).


<details><summary>Рассмотрим демонстрацию работы Микросервиса процессинга. Для этого необходимо запустить контейнер postgres,
сервис Eureka, микросервис шлюз Spring Cloud Gateway, контейнер Apache Kafka, микросервис регистрации и аутентификации identity-service, 
сам микросервис процессинга и микросервис котировок валют:</summary>

1. Регистрация пользователя с именем Misha с помощью микросервиса аутентификации и регистрации:

![1.Регистрация нового пользователя.png](https://github.com/AndreyJavaEdu/microservices-currency-exchanger/blob/readme-file/%D0%A1%D1%85%D0%B5%D0%BC%D1%8B%20%D0%B4%D0%BB%D1%8F%20README/demonstration_of_processing_service/1.%D0%A0%D0%B5%D0%B3%D0%B8%D1%81%D1%82%D1%80%D0%B0%D1%86%D0%B8%D1%8F%20%D0%BD%D0%BE%D0%B2%D0%BE%D0%B3%D0%BE%20%D0%BF%D0%BE%D0%BB%D1%8C%D0%B7%D0%BE%D0%B2%D0%B0%D1%82%D0%B5%D0%BB%D1%8F.png);
 
2. Видим, что пользователь Misha сохранился в БД сервиса аутентификации 
и регистрации после его регистрации и его id = 14:

![2.Пользователь Миша в БД сервиса identity после регистрации.png](https://github.com/AndreyJavaEdu/microservices-currency-exchanger/blob/readme-file/%D0%A1%D1%85%D0%B5%D0%BC%D1%8B%20%D0%B4%D0%BB%D1%8F%20README/demonstration_of_processing_service/2.%D0%9F%D0%BE%D0%BB%D1%8C%D0%B7%D0%BE%D0%B2%D0%B0%D1%82%D0%B5%D0%BB%D1%8C%20%D0%9C%D0%B8%D1%88%D0%B0%20%D0%B2%20%D0%91%D0%94%20%D1%81%D0%B5%D1%80%D0%B2%D0%B8%D1%81%D0%B0%20identity%20%D0%BF%D0%BE%D1%81%D0%BB%D0%B5%20%D1%80%D0%B5%D0%B3%D0%B8%D1%81%D1%82%D1%80%D0%B0%D1%86%D0%B8%D0%B8.png);

3. Получаем JWT токен для пользователя Misha из микросервиса регистрации и аутентификации
   (в payload JWT токена содержится информация об id пользователя, которая будет добавлена к запросу
с помощью фильтра в Gateway микросервисе):

![3.Получаем токен для Миши из identity сервиса.png](https://github.com/AndreyJavaEdu/microservices-currency-exchanger/blob/readme-file/%D0%A1%D1%85%D0%B5%D0%BC%D1%8B%20%D0%B4%D0%BB%D1%8F%20README/demonstration_of_processing_service/3.%D0%9F%D0%BE%D0%BB%D1%83%D1%87%D0%B0%D0%B5%D0%BC%20%D1%82%D0%BE%D0%BA%D0%B5%D0%BD%20%D0%B4%D0%BB%D1%8F%20%D0%9C%D0%B8%D1%88%D0%B8%20%D0%B8%D0%B7%20identity%20%D1%81%D0%B5%D1%80%D0%B2%D0%B8%D1%81%D0%B0.png);

4. Теперь пользователь отправляет запрос в микросервис процессинга на создание нового счета, при этом
добавив Хедер Authorization, тполученный ранее токен как Bearer:

![4.Создание Счета 1 для Миши.png](https://github.com/AndreyJavaEdu/microservices-currency-exchanger/blob/readme-file/%D0%A1%D1%85%D0%B5%D0%BC%D1%8B%20%D0%B4%D0%BB%D1%8F%20README/demonstration_of_processing_service/4.%D0%A1%D0%BE%D0%B7%D0%B4%D0%B0%D0%BD%D0%B8%D0%B5%20%D0%A1%D1%87%D0%B5%D1%82%D0%B0%201%20%D0%B4%D0%BB%D1%8F%20%D0%9C%D0%B8%D1%88%D0%B8.png);

5. Теперь пользователь Misha отправляет запрос на пополнение счета на 15000 рублей в микросервис процессинга:

![5.Пополнили счет 1 Миши на 15000.png](https://github.com/AndreyJavaEdu/microservices-currency-exchanger/blob/readme-file/%D0%A1%D1%85%D0%B5%D0%BC%D1%8B%20%D0%B4%D0%BB%D1%8F%20README/demonstration_of_processing_service/5.%D0%9F%D0%BE%D0%BF%D0%BE%D0%BB%D0%BD%D0%B8%D0%BB%D0%B8%20%D1%81%D1%87%D0%B5%D1%82%201%20%D0%9C%D0%B8%D1%88%D0%B8%20%D0%BD%D0%B0%2015000.png);

6. Демонстрация, что первый счет пользователя с именем Misha сохранился в БД и на нем сумма 15000 руб.:

![6.Этот счет 1 с id=19  сохранился в БД.png](https://github.com/AndreyJavaEdu/microservices-currency-exchanger/blob/readme-file/%D0%A1%D1%85%D0%B5%D0%BC%D1%8B%20%D0%B4%D0%BB%D1%8F%20README/demonstration_of_processing_service/6.%D0%AD%D1%82%D0%BE%D1%82%20%D1%81%D1%87%D0%B5%D1%82%201%20%D1%81%20id%3D19%20%20%D1%81%D0%BE%D1%85%D1%80%D0%B0%D0%BD%D0%B8%D0%BB%D1%81%D1%8F%20%D0%B2%20%D0%91%D0%94.png);

7. Далее пользователь Misha создает еще один счет, без пополнения, отправив запрос в микросервис процессинга:

![7.Создан Счет 2 для пользователя Миша.png](https://github.com/AndreyJavaEdu/microservices-currency-exchanger/blob/readme-file/%D0%A1%D1%85%D0%B5%D0%BC%D1%8B%20%D0%B4%D0%BB%D1%8F%20README/demonstration_of_processing_service/7.%D0%A1%D0%BE%D0%B7%D0%B4%D0%B0%D0%BD%20%D0%A1%D1%87%D0%B5%D1%82%202%20%D0%B4%D0%BB%D1%8F%20%D0%BF%D0%BE%D0%BB%D1%8C%D0%B7%D0%BE%D0%B2%D0%B0%D1%82%D0%B5%D0%BB%D1%8F%20%D0%9C%D0%B8%D1%88%D0%B0.png);

8. Демонстрация, второй счет сохранился в БД с нулевым балансом, id = 20:

![8.Новый счет 2 сохранился в БД с нулевым балансом.png](https://github.com/AndreyJavaEdu/microservices-currency-exchanger/blob/readme-file/%D0%A1%D1%85%D0%B5%D0%BC%D1%8B%20%D0%B4%D0%BB%D1%8F%20README/demonstration_of_processing_service/8.%D0%9D%D0%BE%D0%B2%D1%8B%D0%B9%20%D1%81%D1%87%D0%B5%D1%82%202%20%D1%81%D0%BE%D1%85%D1%80%D0%B0%D0%BD%D0%B8%D0%BB%D1%81%D1%8F%20%D0%B2%20%D0%91%D0%94%20%D1%81%20%D0%BD%D1%83%D0%BB%D0%B5%D0%B2%D1%8B%D0%BC%20%D0%B1%D0%B0%D0%BB%D0%B0%D0%BD%D1%81%D0%BE%D0%BC.png);

9. Далее пользователь делает запрос в микросервис 
процессинга на перевод 5000 рублей со счета с id=19 на счет с id=20:

![9.Перевели с счета 1 на счет 2 Миши 5000 р..png](https://github.com/AndreyJavaEdu/microservices-currency-exchanger/blob/readme-file/%D0%A1%D1%85%D0%B5%D0%BC%D1%8B%20%D0%B4%D0%BB%D1%8F%20README/demonstration_of_processing_service/9.%D0%9F%D0%B5%D1%80%D0%B5%D0%B2%D0%B5%D0%BB%D0%B8%20%D1%81%20%D1%81%D1%87%D0%B5%D1%82%D0%B0%201%20%D0%BD%D0%B0%20%D1%81%D1%87%D0%B5%D1%82%202%20%D0%9C%D0%B8%D1%88%D0%B8%205000%20%D1%80..png);

10. Демонстрация, то что перевод средств был произведен, значение баланса на счесте с id=19 уменьшилось
на 5000 руб., при этом значение баланса на счете с id=20 увеличилось, причем произошла конвертация валюты из RUB
в EUR (логика написана в классе-сервисе [ExchangerService.java](exchange-processing-service%2Fsrc%2Fmain%2Fjava%2Fio%2FkamenskiyAndrey%2FprocessingService%2Fprocessing%2Fservice%2FExchangerService.java)):

![10.Перевод денежных средств отобразился в БД.png](https://github.com/AndreyJavaEdu/microservices-currency-exchanger/blob/readme-file/%D0%A1%D1%85%D0%B5%D0%BC%D1%8B%20%D0%B4%D0%BB%D1%8F%20README/demonstration_of_processing_service/10.%D0%9F%D0%B5%D1%80%D0%B5%D0%B2%D0%BE%D0%B4%20%D0%B4%D0%B5%D0%BD%D0%B5%D0%B6%D0%BD%D1%8B%D1%85%20%D1%81%D1%80%D0%B5%D0%B4%D1%81%D1%82%D0%B2%20%D0%BE%D1%82%D0%BE%D0%B1%D1%80%D0%B0%D0%B7%D0%B8%D0%BB%D1%81%D1%8F%20%D0%B2%20%D0%91%D0%94.png);

11. Далее пользователем был сделан запрос в микросервис процессинга на получение списка его всех счетов:

![11.Сделан запрос на получение списка счетов у Миши.png](https://github.com/AndreyJavaEdu/microservices-currency-exchanger/blob/readme-file/%D0%A1%D1%85%D0%B5%D0%BC%D1%8B%20%D0%B4%D0%BB%D1%8F%20README/demonstration_of_processing_service/11.%D0%A1%D0%B4%D0%B5%D0%BB%D0%B0%D0%BD%20%D0%B7%D0%B0%D0%BF%D1%80%D0%BE%D1%81%20%D0%BD%D0%B0%20%D0%BF%D0%BE%D0%BB%D1%83%D1%87%D0%B5%D0%BD%D0%B8%D0%B5%20%D1%81%D0%BF%D0%B8%D1%81%D0%BA%D0%B0%20%D1%81%D1%87%D0%B5%D1%82%D0%BE%D0%B2%20%D1%83%20%D0%9C%D0%B8%D1%88%D0%B8.png).

</details>

### 3. Сервис регистрации и обнаружения микросервисов - Spring Cloud Eureka ([eureka-service](eureka-service))
Когда у нас большое количество микросервисов, то при их развертывании нам
необходимо ими управлять и контролировать их состояние.

Техлогии и библиотеки: spring-boot-starter 3.2.0, spring-cloud-starter-netflix-eureka-server.

Преимущества использования Spring Cloud Eureka:
- регистрация микросервисов;
- возможность получения ip-адреса сервиса;
- балансировка нагрузки между модулями (т.е. можно именно по имени микосервиса,
когда у нас много его инсталяций, маршрутизировать нагрузку - это важно для работы балансировщика нагрузки).

Схема работы сервиса обнаружения:

![Диаграмма Spring Cloud Eureka.png](https://github.com/AndreyJavaEdu/microservices-currency-exchanger/blob/readme-file/%D0%A1%D1%85%D0%B5%D0%BC%D1%8B%20%D0%B4%D0%BB%D1%8F%20README/Spring%20cloud%20Eureka/%D0%94%D0%B8%D0%B0%D0%B3%D1%80%D0%B0%D0%BC%D0%BC%D0%B0%20Spring%20Cloud%20Eureka.png)

Принцип действия данного сервера Eureka: все микросервисы отправляют
в Service Registry свои имя, хост, порт и ip-адрес. Потребитель же этой информации,
например Маршрутизатор уже по имени сервиса забирает его информацию и использует и передает по назначению.

В самом приложении класс запуска [EurekaServiceApplication.java](eureka-service%2Fsrc%2Fmain%2Fjava%2Fio%2FkamenskiyAndrey%2FcurrencyExchanger%2Fdiscovery%2FEurekaServiceApplication.java) 
аннотирован аннотацией @EnableEurekaServer, которая показывает, что данный сервис является
сервером Eureka:
```java
@SpringBootApplication
@EnableEurekaServer
public class EurekaServiceApplication {

   public static void main(String[] args) {
      SpringApplication.run(EurekaServiceApplication.class, args);
   }
}
```
Настройка в [application.yml](eureka-service%2Fsrc%2Fmain%2Fresources%2Fapplication.yml):
```yaml
server:
  port: 8761

spring:
  application:
    name: eureka-service

eureka:
  client:
    register-with-eureka: false
    fetch-registry: false

  instance:
    prefer-ip-address: true
```
Т.к. это сервер, то мы назначили ему порт 8761 (порт по умолчанию).
Задали имя приложения - eureka-service.
Также в настройке мы отключили регистрацию данного приложения на сервере, т.к. это сам Сервер (register-with-eureka: false).
И отключили настройку получение информации о регистрации данного приложения, как клиента, т.к. это сам сервер (fetch-registry: false).

После запуска приложения и переходе по адресу http://localhost:8761 
мы увидим, что сервис Eureka запустился, но зарегистрированные приложения отсутствуют:

![Start Eureka Server.png](https://github.com/AndreyJavaEdu/microservices-currency-exchanger/blob/readme-file/%D0%A1%D1%85%D0%B5%D0%BC%D1%8B%20%D0%B4%D0%BB%D1%8F%20README/Spring%20cloud%20Eureka/Start%20Eureka%20Server.png)

![Start Eureka 2.png](https://github.com/AndreyJavaEdu/microservices-currency-exchanger/blob/readme-file/%D0%A1%D1%85%D0%B5%D0%BC%D1%8B%20%D0%B4%D0%BB%D1%8F%20README/Spring%20cloud%20Eureka/Start%20Eureka%202.png)

Чтобы наши микросервисы регистрировались на сервере Eureka, В каждом приложении была добавилена в pom.xml 
зависимость:
```xml
<dependencies>
   <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
   </dependency>
</dependencies>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>${spring-cloud.version}</version>
            <type>pom</type>
             <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<repositories>
    <repository>
        <id>spring-milestones</id>
        <name>Spring Milestones</name>
        <url>https://repo.spring.io/milestone</url>
        <snapshots>
        <enabled>false</enabled>
        </snapshots>
    </repository>
</repositories>

```
Чтобы Spring понял, что мы хотим зарегистрировать наши микросервисы в Eureka сервисе
была добавлена аннотация над классом старта каждого микросервиса - @EnableDiscoveryClient (посмотрим на примере [exchange-processing-service](exchange-processing-service)):
```java
@SpringBootApplication
@EnableDiscoveryClient
public class ExchangeProcessingApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExchangeProcessingApplication.class, args);
	}
}
```
В каждом микросервисе (клиенте Eureka) мы указали куда необходимо отправлять данные о регистрации 
текущего сервиса,
путем добавления настройки в конфигурационный файл [application.yml](exchange-processing-service%2Fsrc%2Fmain%2Fresources%2Fapplication.yml) - 
это мы указали в настроке defaultZone: http://${cloud.eureka-host}:8761/eureka/:
```yaml
server:
   port: 8090 
spring:
   application:
      name: exchange-processing-service
cloud:
   db-host: localhost
   eureka-host: localhost
   kafka-host: localhost
eureka:
  client:
    fetch-registry: true
    register-with-eureka: true
    service-url:
      defaultZone: http://${cloud.eureka-host}:8761/eureka/
  instance:
    hostname: localhost
    prefer-ip-address: true
    instance-id: ${spring.application.name}:${server.port}
```
Когда параметр "fetch-registry" установлен в значение "true", 
клиент Eureka будет периодически обновлять список доступных экземпляров служб, 
связанных с ним. Это позволяет клиентам Eureka динамически обнаруживать и подключаться 
к другим службам, участвующим в реестре Eureka.
Параметр конфигурации "register-with-eureka" в контексте Eureka определяет, должен ли клиент
регистрироваться в реестре Eureka. Установка этого параметра в значение "true" указывает 
клиенту Eureka на необходимость зарегистрировать самого себя в реестре, чтобы другие службы 
могли обнаруживать его.



### 4. Сервис Spring Cloud Gateway - [gateway-service](gateway-service)

У нас есть единая точка доступа для всех микросервисов от клиента - в нашем случае это Rest интерфейс, 
вызываемый web приложением. А сервисов у нас очень много и сервисный шлюз gateway действует, как посредник
между web-клиентом (Postman) и вызываемыми службами. Клиент обращается только к одному url,
который принадлежит сервесному шлюзу gateway, а уже этот шлюз анализирует путь указанный клиентом
(например, смотря что указано в пути - currency или processing) и определяет какую службу ему вызвать.

Для реализации шлюза используется проект Spring Cloud Gateway.

Схема работы сервиса обнаружения:
![Диаграмма без названия.png](https://github.com/AndreyJavaEdu/microservices-currency-exchanger/blob/readme-file/%D0%A1%D1%85%D0%B5%D0%BC%D1%8B%20%D0%B4%D0%BB%D1%8F%20README/Gateway/%D0%94%D0%B8%D0%B0%D0%B3%D1%80%D0%B0%D0%BC%D0%BC%D0%B0%20%D0%B1%D0%B5%D0%B7%20%D0%BD%D0%B0%D0%B7%D0%B2%D0%B0%D0%BD%D0%B8%D1%8F.png)

Техлогии и библиотеки: spring-boot-starter 3.2.0, spring-cloud-starter-gateway, spring-cloud-starter-netflix-eureka-client,
lombok, зависимости для работы с jjwt (jjwt-api, jjwt-impl, jjwt-jackson).

<details><summary>В конфигурационном файле application.yml мы указали порт по которому шлюз принимает запросы,
а также настроить роутинг - правила по которым бы будем маршрутизировать наши запросы на
конкретные микросервисы:</summary>

```yaml
# Общий Порт шлюза gateway
server:
  port: 8080

# Имя сервиса Spring cloud gateway
spring:
   application:
      name: api-gateway-service

   # Роутинг сервисов
   cloud:
      gateway:
         discovery:
            locator:
               enabled: true
               lower-case-service-id: true
         routes:
            - id: currency-rate-service
              uri: ${cloud.currency-service-url}
              predicates:
                 - Path=/money/**
              filters:
                 - AuthenticationFilter

            - id: exchange-processing-service
              uri: ${cloud.processing-service-url}
              predicates:
                 - Path=/processing/**
              filters:
                 - AuthenticationFilter

            - id: identity-service
              uri: ${cloud.identity-service-url}
              predicates:
                 - Path=/auth/**

            - id: account-history-service
              uri: ${cloud.account-history-service-url}
              predicates:
                 - Path=/history/**
              filters:
                 - AuthenticationFilter

eureka:
   client:
      fetch-registry: true
      register-with-eureka: true
      service-url:
         defaultZone: http://${cloud.eureka-host}:8761/eureka/
   instance:
      hostname: localhost
      prefer-ip-address: true
      instance-id: ${spring.application.name}:${server.port}
# Видимость логов при обращении к сервисам
logging:
   level:
      org.springframework.cloud.gateway: DEBUG
```
С помощью параметра конфигурации predicates мы указываем часть url, который содержит ключевое слово, например predicates
и это будет означать что шлюз перенаправит запрос по url указанному в параметре uri.
</details>

Демонстрация работы маршрутизатора:

Вместо конкретных url и портов отдельных сервисов мы указываем один порт маршрутизатора 
spring gateway и происходит маршрутизация и перенаправление на конкретный микросервис.

![Демонстрация маршрутизации в сервис котировок 1.png](https://github.com/AndreyJavaEdu/microservices-currency-exchanger/blob/readme-file/%D0%A1%D1%85%D0%B5%D0%BC%D1%8B%20%D0%B4%D0%BB%D1%8F%20README/Gateway/%D0%94%D0%B5%D0%BC%D0%BE%D0%BD%D1%81%D1%82%D1%80%D0%B0%D1%86%D0%B8%D1%8F%20%D1%80%D0%B0%D0%B1%D0%BE%D1%82%D1%8B%20%D1%88%D0%BB%D1%8E%D0%B7%D0%B0/%D0%94%D0%B5%D0%BC%D0%BE%D0%BD%D1%81%D1%82%D1%80%D0%B0%D1%86%D0%B8%D1%8F%20%D0%BC%D0%B0%D1%80%D1%88%D1%80%D1%83%D1%82%D0%B8%D0%B7%D0%B0%D1%86%D0%B8%D0%B8%20%D0%B2%20%D1%81%D0%B5%D1%80%D0%B2%D0%B8%D1%81%20%D0%BA%D0%BE%D1%82%D0%B8%D1%80%D0%BE%D0%B2%D0%BE%D0%BA%201.png)

![Демонстрация маршрутизация в сервис аутентификации 2.png](https://github.com/AndreyJavaEdu/microservices-currency-exchanger/blob/readme-file/%D0%A1%D1%85%D0%B5%D0%BC%D1%8B%20%D0%B4%D0%BB%D1%8F%20README/Gateway/%D0%94%D0%B5%D0%BC%D0%BE%D0%BD%D1%81%D1%82%D1%80%D0%B0%D1%86%D0%B8%D1%8F%20%D1%80%D0%B0%D0%B1%D0%BE%D1%82%D1%8B%20%D1%88%D0%BB%D1%8E%D0%B7%D0%B0/%D0%94%D0%B5%D0%BC%D0%BE%D0%BD%D1%81%D1%82%D1%80%D0%B0%D1%86%D0%B8%D1%8F%20%D0%BC%D0%B0%D1%80%D1%88%D1%80%D1%83%D1%82%D0%B8%D0%B7%D0%B0%D1%86%D0%B8%D1%8F%20%D0%B2%20%D1%81%D0%B5%D1%80%D0%B2%D0%B8%D1%81%20%D0%B0%D1%83%D1%82%D0%B5%D0%BD%D1%82%D0%B8%D1%84%D0%B8%D0%BA%D0%B0%D1%86%D0%B8%D0%B8%202.png)

![Демонстрация маршрутизации в сервис процессинга 3.png](https://github.com/AndreyJavaEdu/microservices-currency-exchanger/blob/readme-file/%D0%A1%D1%85%D0%B5%D0%BC%D1%8B%20%D0%B4%D0%BB%D1%8F%20README/Gateway/%D0%94%D0%B5%D0%BC%D0%BE%D0%BD%D1%81%D1%82%D1%80%D0%B0%D1%86%D0%B8%D1%8F%20%D1%80%D0%B0%D0%B1%D0%BE%D1%82%D1%8B%20%D1%88%D0%BB%D1%8E%D0%B7%D0%B0/%D0%94%D0%B5%D0%BC%D0%BE%D0%BD%D1%81%D1%82%D1%80%D0%B0%D1%86%D0%B8%D1%8F%20%D0%BC%D0%B0%D1%80%D1%88%D1%80%D1%83%D1%82%D0%B8%D0%B7%D0%B0%D1%86%D0%B8%D0%B8%20%D0%B2%20%D1%81%D0%B5%D1%80%D0%B2%D0%B8%D1%81%20%D0%BF%D1%80%D0%BE%D1%86%D0%B5%D1%81%D1%81%D0%B8%D0%BD%D0%B3%D0%B0%203.png)


В самом сервисе Gateway мы реализовали кастомный класс фильтра запросов [AuthenticationFilter.java](gateway-service%2Fsrc%2Fmain%2Fjava%2Fio%2FkamenskiyAndrey%2FcurrencyExchanger%2Fgateway%2Ffilter%2FAuthenticationFilter.java),
который наследуется от абстрактного класса [AuthenticationFilter.java](gateway-service%2Fsrc%2Fmain%2Fjava%2Fio%2FkamenskiyAndrey%2FcurrencyExchanger%2Fgateway%2Ffilter%2FAuthenticationFilter.java).
В данном классе реализован метод фильтра apply(), в котором мы проверяем в условии if содержит 
ли запрос клиента  url на регистрацию, получение токена, а также обращение к сервису eureka.
Данные url запросы не имеют авторизацию и поэтому это проверяется
во вспомогательном 
методе isSecured класса [RouteValidator.java](gateway-service%2Fsrc%2Fmain%2Fjava%2Fio%2FkamenskiyAndrey%2FcurrencyExchanger%2Fgateway%2Ffilter%2FRouteValidator.java).
Далее в фильтре проверяется содержит ли запрос пользователя Хэдер c меткой AUTHORIZATION,
и если содержит, то мы получаем этот Хэдер и отрезаем от него часть строки Bearer с пробелом,
т.е. возвращаем строку которая начинается с 7 индекса Хэдера. Полученная строка и есть токен, который сгенерирован в сервисе
регистрации и аутентификации [identity-service-new](identity-service-new) и далее пытаемся произвести валидацию данного токена.
Если валидация токена успешна, то изменяем запрос посредством метода mutate и добавляем в Хэдер запроса
уже UserId извлеченный из токена с помощью вспомогательного класса [JWTUtil.java](gateway-service%2Fsrc%2Fmain%2Fjava%2Fio%2FkamenskiyAndrey%2FcurrencyExchanger%2Fgateway%2Futil%2FJWTUtil.java),
в котором реализован метод public Integer extractUserId(String token) получения значения UserId.
Тем самым посредством реализованного кастомного фильтра Spring Cloud Gateway производится
проверка каждого запроса пользователя на валидность и к запросу добавляется Хэдер
со значением UserId, который используется далее в параметрах методов контроллеров микросервисов.



### 5. Микросервис регистрации и аутентификации - [identity-service-new](identity-service-new)

Техлогии и библиотеки: spring-boot-starter 3.0.4, spring-boot-starter-security, 
spring-cloud-starter-netflix-eureka-client,
lombok, зависимости для работы с jjwt (jjwt-api, jjwt-impl, jjwt-jackson), spring-boot-starter-web,
postgresql, flyway-core.

![Схема работы isentity сервиса.png](https://github.com/AndreyJavaEdu/microservices-currency-exchanger/blob/readme-file/%D0%A1%D1%85%D0%B5%D0%BC%D1%8B%20%D0%B4%D0%BB%D1%8F%20README/Identity/%D0%A1%D1%85%D0%B5%D0%BC%D0%B0%20%D1%80%D0%B0%D0%B1%D0%BE%D1%82%D1%8B%20isentity%20%D1%81%D0%B5%D1%80%D0%B2%D0%B8%D1%81%D0%B0.png)

Данный микросервис настроен, как клиент для регистрации на сервире Eureka:
```yaml
eureka:
  client:
    service-url:
      defaultZone: http://${cloud.eureka-host}:8761/eureka
```
Также настроена конфигурация для подключения к БД и миграция с помощью flyway:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://${cloud.db-host}:5433/security
    username: postgres
    password: password
    driver-class-name: org.postgresql.Driver

  flyway:
    enabled: true
    locations: classpath:db
    user: postgres
    password: password
    url: jdbc:postgresql://${cloud.db-host}:5433/security
```
В пакете [entity](identity-service-new%2Fsrc%2Fmain%2Fjava%2Fcom%2Fkamenskiyandrey%2Fidentityservice%2Fentity)
реализован класс сущности [UserCredential.java](identity-service-new%2Fsrc%2Fmain%2Fjava%2Fcom%2Fkamenskiyandrey%2Fidentityservice%2Fentity%2FUserCredential.java)
в котором определены поля такие как идентификатор пользователя (id), имя пользователя (name), почта пользователя (email),
пароль пользователя (password). Каждое поле замаплено на таблицу БД. Также поле id аннотировано аннотацией @Id, которая
показывает, что в таблице БД столбец связанный с данным полем является Primary key. У поля id определена стратегия
генерации - @GeneratedValue(strategy = GenerationType.IDENTITY) - полагается на автоматическое увеличение значения столбца по правилам, прописанных в БД.
Также аннотировали каждое поле данного класса аннотацией @Column и прописали имена столбцев таблицы в БД, которые соответствуют полям класса.

В пакете [repository](identity-service-new%2Fsrc%2Fmain%2Fjava%2Fcom%2Fkamenskiyandrey%2Fidentityservice%2Frepository)
создан интерфейс репозитория [UserCredentialRepository.java](identity-service-new%2Fsrc%2Fmain%2Fjava%2Fcom%2Fkamenskiyandrey%2Fidentityservice%2Frepository%2FUserCredentialRepository.java),
который унаследовали от JpaRepository<UserCredential, Integer>.

В пакете [service](identity-service-new%2Fsrc%2Fmain%2Fjava%2Fcom%2Fkamenskiyandrey%2Fidentityservice%2Fservice)
мы создали класс сервиса [AuthService.java](identity-service-new%2Fsrc%2Fmain%2Fjava%2Fcom%2Fkamenskiyandrey%2Fidentityservice%2Fservice%2FAuthService.java),
пометив его аннотацией @Service. В данном классе заинжектили бин репозитория. В данном классе сервиса реализован
метод регистрации пользователя (сохранения пользователя в БД с именем security):
```java
   @Transactional
    public String saveUser(AddNewUserDTO dto){
        var credential = new UserCredential();
        credential.setName(dto.getUserName());
        credential.setEmail(dto.getEmail());
        credential.setPassword(passwordEncoder.encode(dto.getPassword())); //Извлекли пароль, закодировали и поместили в объект UserCredential
        repository.save(credential); //хранить пароль нужно закодированным, поэтому создадим специальный кодировщик
        return "user added to the system";
    }
```
В данном методе при записи пароля пользователя в БД, сначало пароль извлекается из dto, далее кодируется
с помощью метода encode() вызванного на бине кодировщика паролей и помещается в объект UserCredential.
Также данный сервис инжектит бин JWTService, с помощью которого реализуются еще два метода:
- Метод который генерирует токен для пользователя по его имени (generateToken()).
```java
  public String generateToken(String userName){
        return jwtService.generateToken(userName);
    }
```
- Метод проверки, валидации токена (validateToken()) - используем метод ранее созданного 
класса, который мы заинжектили, как бин jwtService.
```java
public void validateToken(String token){
        jwtService.validateToken(token);
    }
```

Чтобы работал кодировщик паролей и в БД записывался пароль 
уже закодированном формате, мы определили
в классе конфигурации [AuthConfig.java](identity-service-new%2Fsrc%2Fmain%2Fjava%2Fcom%2Fkamenskiyandrey%2Fidentityservice%2Fconfig%2FAuthConfig.java)
в пакете [config](identity-service-new%2Fsrc%2Fmain%2Fjava%2Fcom%2Fkamenskiyandrey%2Fidentityservice%2Fconfig)
бин PasswordEncoder:
```java
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
```
В самом классе конфигурации [AuthConfig.java](identity-service-new%2Fsrc%2Fmain%2Fjava%2Fcom%2Fkamenskiyandrey%2Fidentityservice%2Fconfig%2FAuthConfig.java)
мы указали какой url мы можем обойти (не использовать аутентификацию), а какой url мы должны обязательно
подвергать аутентификации.

Для того чтобы сгенерировать JWT токен и проверить его был реализован класс
[JWTService.java](identity-service-new%2Fsrc%2Fmain%2Fjava%2Fcom%2Fkamenskiyandrey%2Fidentityservice%2Fservice%2FJWTService.java).
В данном вспомогательном классе сервисе используется библиотека JWT (jjwt-api, jjwt-impl, jjwt-jackson). Также
мы определили секретный 32-бит ключ, сгенерировав его заранее. Значение данного секрета вынесено в application.yml.
В данном классе реализованы следующие методы:
- public void validateToken(final String token) - метод валидации токена.
- public String generateToken(String userName) - метод генерации токена, который вызывает вспомогательный метод
с помощью которого формируется сам токен;
- private String createToken(Map<String, Object> claims, String userName) - метод создания токена и заполнения
его payload (заполняем полезную нагрузку JWT, установливаем время создания токена в payload JWT, установили время после которого токен нельзя использовать,
присваивается подпись и создается сам токен JWT);
- private Key getSignKey() -  принимает закрытый ключ (SECRET) в формате BASE64, декодирует его, 
создает ключ HMAC с использованием декодированных байтов и возвращает полученный ключ. Этот ключ используется для создания 
подписи JWT токена с алгоритмом HS256, который мы указали при создании токена в предыдущем методе.

Все три метода, которые мы реализовали в классе сервисе [AuthService.java](identity-service-new%2Fsrc%2Fmain%2Fjava%2Fcom%2Fkamenskiyandrey%2Fidentityservice%2Fservice%2FAuthService.java)
мы должны вызвать в классе Рест-контроллере, который мы также реализовали - [AuthController.java](identity-service-new%2Fsrc%2Fmain%2Fjava%2Fcom%2Fkamenskiyandrey%2Fidentityservice%2Fcontroller%2FAuthController.java).
Данный контроллер аннотирован, как @RequestMapping("/auth"), тем самым мы назначили
корневой url для всех запросов в данный микросервис.
В данном Рест-контроллере мы заинжектили бины AuthService и AuthenticationManager.
Бин AuthenticationManager мы сконфигурировали в классе конфигурации.
Данный Рест-контроллер содержит следующие методы:
- POST - public String addNewUser(@RequestBody AddNewUserDTO user) - Метод регистрации, добавления нового пользователя приложения.
Данный метод аннотирован @PostMapping("/registration"), и пользователь при регистрации 
должен ввести данные, которые должны быть предоставлены в виде JSON, например:
```json
{
    "name": "Pasha",
    "email": "pavel@yandex.ru",
    "password": "1122"
}
```
Этот JSON преобразуется в объект DTO, в объект класса [AddNewUserDTO.java](identity-service-new%2Fsrc%2Fmain%2Fjava%2Fcom%2Fkamenskiyandrey%2Fidentityservice%2Fdto%2FAddNewUserDTO.java), который мы также создали
в пакете [dto](identity-service-new%2Fsrc%2Fmain%2Fjava%2Fcom%2Fkamenskiyandrey%2Fidentityservice%2Fdto).
- POST - public String getToken(@RequestBody GetTokenCredititialsDTO data) - это метод 
получения токена зарегистрированным пользователем. 
- GET - public String validateToken(@RequestParam(name = "token") String token) - метод валидации токена.
Этот метод аннотирован аннотацией @GetMapping("/validate"). Данный метод принимает
токен из параметра запроса, т.е. параметр самого метода связан с параметром запроса аннотацией @RequestParam.

Поскольку мы внедрили систему безопасности Spring Security по умолчанию, данная
система позволяет передавать имя пользователя и пароль для доступа к конкретному эндпоинту
который в Рест-контроллере. Spring Security будет производить аутентификацию для каждой
конкретного эндпоинта по умолчанию и при вводе имя пользователя и пароля будет происходить ошибка.
И никто не сможет правильно получить доступ к эндпоинтам в Рест-контроллере. Чтобы этого не происходило мы произвели обход проверки аутентификации эндпоинтов в Рест-контроллере.
Для этого класс конфигурации [AuthConfig.java](identity-service-new%2Fsrc%2Fmain%2Fjava%2Fcom%2Fkamenskiyandrey%2Fidentityservice%2Fconfig%2FAuthConfig.java)
помечен аннотацией @EnableWebSecurity. И в данном классе конфигурации мы определили
цепочку фильтров безопасности:
```java
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.csrf().disable() //отключили защиту от CSRF атак, т.к. используем токены JWT
                .authorizeHttpRequests() //Конфигурируем цепочкой следующих методов политику авторизации (сюда входит настройка ролей)
                //тут указали правила авторизации, для определенных эндпоитов разрешили для всех пользователей без авторизации и аутентификации
                .requestMatchers("/auth/registration", "/auth/token", "/auth/validate").permitAll()
                .and()
                .build();
    }
```
С помощью метода permitAll() мы  разрешения 
доступ к эндпоинтам определенным в Рест-контроллере [AuthController.java](identity-service-new%2Fsrc%2Fmain%2Fjava%2Fcom%2Fkamenskiyandrey%2Fidentityservice%2Fcontroller%2FAuthController.java) 
без необходимости аутентификации. 

Данный микросервис мы также зарегистрировали в качестве клиента Eureka:
```yaml
spring:
   application:
      name: Authentificational-service

eureka:
   client:
      service-url:
         defaultZone: http://${cloud.eureka-host}:8761/eureka

server:
   port: 9797
```












