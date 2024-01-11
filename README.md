# Проект микросервисы - сервисы по обмене валют

Данный проект включает в себя несколько отдельных сервисов, которые взаимодействуют между собой.

**Микросервисы в проекте:**
- [rate_currency_service-master](rate_currency_service-master) - микросервис по получению котировок валют с https://www.cbr.ru/.


- [exchange-processing-service](exchange-processing-service) - микросервис по созданию счета пользователя, 
а также реализация перевода денежных средств с одного счета на другой, отправка событий по операций со счетом в топик Кафки.
Вывод информации по счетам для определенного пользователя.


- [eureka-service](eureka-service) - Сервис регистрации всех микросервисов в на сервере Eureka. 
Spring Cloud Eureka обычно используется в среде микросервисов для регистрации, 
обнаружения и управления взаимодействием между службами. Это помогает сделать архитектуру приложения более гибкой, 
масштабируемой и отказоустойчивой.


- [identity-service-new](identity-service-new) - микросервис по обеспичению безопасности всех микросервисов - 
в данном микросервисе реализованы запросы на регистрацию пользователя, сохранение его логина и пароля (в закодированном формате)
в БД и хранение учетных. Также данный сервис реализует получение JWT токена пользователем и проверка токена (валидация).


- [history-service](history-service) - микросервис по сохранению истории операций со 
счетом конкретного пользователя в топик Кафки и из него в БД для хранения истории. Микросервис реализован на Kotlin.


- [notification-bot](notification-bot) - Микросервис телеграмм бот, реализующий оповещение
конкретного пользователя об операциях с денежными счетами.


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
1. Микросервис по получению котировок валют ([rate_currency_service-master](rate_currency_service-master))

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



2. Микросервис процессинга - [exchange-processing-service](exchange-processing-service)

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





