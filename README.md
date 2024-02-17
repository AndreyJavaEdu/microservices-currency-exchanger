# Проект микросервисы - сервисы по обмене валют

Данный проект включает в себя несколько отдельных сервисов, которые взаимодействуют между собой.

**Микросервисы в проекте:**
- [rate_currency_service-master](rate_currency_service-master) - микросервис по 
получению котировок валют с https://www.cbr.ru/ - [Объяснение сервиса currency](#Currency).


- [exchange-processing-service](exchange-processing-service) - микросервис по созданию счета пользователя, 
а также реализация перевода денежных средств с одного счета на другой, отправка событий по операций со счетом в топик Кафки.
Вывод информации по счетам для определенного пользователя - [Объяснение сервиса processing](#Processing)


- [eureka-service](eureka-service) - Сервис регистрации и обнаружения всех микросервисов на сервере Eureka. 
Spring Cloud Eureka обычно используется в среде микросервисов для регистрации, 
обнаружения и управления взаимодействием между службами. Это помогает сделать архитектуру приложения более гибкой, 
масштабируемой и отказоустойчивой - [Объяснение сервиса eureka](#Eureka).


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

## Развертывание всех микросервисов локально с использованием Docker и docker-compose.
Для каждого микросервиса написан свой Dockerfile, в котором определены переменные окружения,
а также основа образа и действие при запуске контейнера (командой ENTRYPOINT):
- rate_currency_service-master: [Dockerfile](rate_currency_service-master%2FDockerfile)
- exchange-processing-service: [Dockerfile](exchange-processing-service%2FDockerfile)
- eureka-service: [Dockerfile](eureka-service%2FDockerfile)
- identity-service-new: [Dockerfile](identity-service-new%2FDockerfile)
- history-service: [Dockerfile](history-service%2FDockerfile)
- notification-bot: [Dockerfile](notification-bot%2FDockerfile)
- gateway-service: [Dockerfile](gateway-service%2FDockerfile)

Для настройки запуска контейнеров из образов микросервисов реализован [docker-compose.yml](docker-compose.yml):
```dockerfile
version: '3.5'

services:
  zookeeper:
    image: wurstmeister/zookeeper
    container_name: zookeeper
    ports:
      - "2181:2181"

  kafka:
    image: wurstmeister/kafka
    container_name: kafka
    hostname: kafka
    ports:
      - "9092:9092"
    environment:
      KAFKA_ADVERTISED_HOST_NAME: "172.17.0.1"
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_CREATE_TOPICS: "account-events:1:1"
    depends_on:
      - zookeeper

  postgres_exchange:
    image: postgres:12.17-alpine3.19
    container_name: postgres_exchange
    restart: unless-stopped
    ports:
      - "5433:5432"
    environment:
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: password
      PGDATA: /var/lib/postgresql/data/pgdata
    volumes:
      - ./opt/docker_postgres/dbdata:/var/lib/postgresql/data
      - ./opt/docker_postgres/postgres_data/init-database.sh:/docker-entrypoint-initdb.d/init-database.sh
      - ./opt/docker_postgres/postgres_data/init-database2.sh:/docker-entrypoint-initdb.d/init-database2.sh
      - ./opt/docker_postgres/postgres_data/init-database3.sh:/docker-entrypoint-initdb.d/init-database3.sh

  eureka-server:
    build: ./eureka-service
    container_name: eureka-service
    hostname: eureka-service
    ports:
      - "8761:8761"

  currency-service:
    build: ./rate_currency_service-master
    container_name: currency-rate-service-1
    hostname: currency-rate-service-1
    ports:
      - "8084:8084"
    environment:
      EUREKA_HOST: "172.17.0.1"

  processing-service:
    build: ./exchange-processing-service
    container_name: processing-service
    hostname: processing-service
    ports:
      - 8090:8090
    environment:
      DB_HOST: "172.17.0.1"
      KAFKA_HOST: "172.17.0.1"
      EUREKA_HOST: "172.17.0.1"
    depends_on:
      - postgres_exchange
      - zookeeper
      - kafka

  gateway-service:
    build: ./gateway-service
    container_name: gateway-service
    hostname: gateway-service
    ports:
      - 8080:8080
    environment:
      EUREKA_HOST: "172.17.0.1"
      PROCESSING_URL: "http://172.17.0.1:8090"
      CURRENCY_URL: "http://172.17.0.1:8084"
      AUTH_URL: "http://172.17.0.1:9797"
      HISTORY_URL: "http://172.17.0.1:8015"
    depends_on:
     - eureka-server

  identity-service:
    build: ./identity-service-new
    container_name: identity-service
    hostname: identity-service
    ports:
      - 9797:9797
    environment:
      EUREKA_HOST: "172.17.0.1"
      DB_HOST: "172.17.0.1"
    restart: unless-stopped
    depends_on:
      - eureka-server

  history-service:
    build: ./history-service
    container_name: history-service
    hostname: history-service
    ports:
      - 8015:8015
    environment:
      DB_HOST: "172.17.0.1"
      KAFKA_HOST: "172.17.0.1"
      EUREKA_HOST: "172.17.0.1"
    depends_on:
      - eureka-server
      - kafka
      - processing-service

  notification-bot:
    build: ./notification-bot
    container_name: notification-bot
    hostname: notification-bot
    ports:
      - 8077:8077
    volumes:
      - .\notification-bot\conf\telegram.token:/conf/telegram.token
    environment:
      KAFKA_HOST: "172.17.0.1"
      TELEGRAM_BOT_TOKEN: .\conf\telegram.token
      IDENTITY_SERVICE: "http://172.17.0.1:9797/auth/token"
    depends_on:
      - kafka
      - processing-service
```
В данном файле мы переопределяем переменные окружения всех микросервисов, которые были изначально
определены в Dockerfile-ах, задав им значения непосредственно через ip-адрес сети bridge,
как 172.17.0.1. Т.о. контейнеры поднятые с помощью docker-compose могут взаимодействовать
между собой по данной сети, а также через прокинутые порты.

В docker-compose файле мы также описали запуск контейнера для Kafka и Zookeeper. Для создания
обараза использовали готовый образ с docker-hub.

В качестве базы данных использовали готовый образ с docker-hub - postgres:12.17-alpine3.19.
Особенностью является то, что мы связали директорию для хранения баз данных локально с 
директорией в контейнере docker. При перезапуске контейнера, все данные будут взяты с локальной
машины и восстановлены в самом контейнере. Еще одной особенностью является то, что
при развертывании нового контейнера из образа БД происходит автоматическое создание
трех баз данных (processing, account_history, security) необходимых для работы соответствующих
микросервисов. Данная функция реализована за счет созданных скриптов, которые находятся в директории
[postgres_data](opt%2Fdocker_postgres%2Fpostgres_data), данные скрипты копируются
в директорию в самом контейнере postgres (/docker-entrypoint-initdb.d/) и запускаются в нем.

Опция `depends_on` в файле `docker-compose.yml`
в Docker Compose используется для задания зависимостей между микросервисами, что гарантирует
запуск микросервисов, которые указаны в depends_on перед запуском данного сервиса.

Образы билдятся с помощью опции `build:`. В данной опции мы указываем директорию, 
в которой находится Dokerfile микросервиса, и Docker Compose производит сборку образа микросервиса
и далее запуск контейнера.


Перед запуском docker-compose необходимо сбилдить все микросервисы с помощью сборщиков maven
и gradle, после этого запустить docker-compose.yml из Intellij IDEA или перейти в терминале в директорию, где
находится файл docker-compose.yml и выполнить команду:
```shell
docker-compose up -d
```
Флаг -d запустит docker-compose в фоновом режиме и можно будет 
продолжить работу в терминале.

## Объяснение как работают микросервисы

### <a name="Currency">1. Микросервис по получению котировок валют ([rate_currency_service-master](rate_currency_service-master))</a>

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



### <a name="Processing">2. Микросервис процессинга - [exchange-processing-service](exchange-processing-service)</a> 

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

### <a name="Eureka">3. Сервис регистрации и обнаружения микросервисов - Spring Cloud Eureka ([eureka-service](eureka-service))</a>
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
В данном вспомогательном классе-сервисе используется библиотека JWT (jjwt-api, jjwt-impl, jjwt-jackson). Также
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
получения токена зарегистрированным пользователем. В данном методе могут получать 
токен только те пользователи, которые уже зарегистрировались и их данные присутствуют в БД.
Т.е. в данном методе мы реализовали процесс аутентификации пользователя (сравнение имя пользователя и пароля,
введенных Юзером с теми которые уже имеются в БД). Для этого используется бин AuthenticationManager,
который заранее получен как компонент в классе конфигурации [AuthConfig.java](identity-service-new%2Fsrc%2Fmain%2Fjava%2Fcom%2Fkamenskiyandrey%2Fidentityservice%2Fconfig%2FAuthConfig.java):
```java
    //Определяем бин AuthenticationManager с помощью метода getAuthenticationManager класса AuthenticationConfiguration
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
```
Сам метод выглядит так:
```java
    @PostMapping("/token")
    public String getToken(@RequestBody GetTokenCredititialsDTO data) {
        Authentication authenticate = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(data.getUserName(), data.getPassword())); //Получаем объект типа Authentication характеризующий аунтефицирован пользователь или нет
        if (authenticate.isAuthenticated()) { //Если пользователь аутентифицирован, то выполним генерацию токена
            return service.generateToken(data.getUserName());
        }else {
            throw new RuntimeException("Invalid access");
        }
    }
```
Чтобы производилась аутентификация в методе getToken() мы определить специальный бин сведений о пользователе
UserDetailsService в классе конфигурации:
```java
 @Bean
    public UserDetailsService userDetailsService() {
        return new CustomUserDetailService(repository);
    }
```
В данном бине используем созданный нами кастомный класс CustomUserDetailService, который будет
имплементировать интерфейс userDetailsService и подключиться к БД и передаст информацию о пользователе 
в AuthenticationProvider, а он в свою очередь подключиться к AuthenticationManager-у. 
Сам кастомный класс [CustomUserDetailService.java](identity-service-new%2Fsrc%2Fmain%2Fjava%2Fcom%2Fkamenskiyandrey%2Fidentityservice%2Fconfig%2FCustomUserDetailService.java),
который имплементирует интерфейс UserDetailsService содержит только один метод loadUserByUsername(String username)
по загрузки пользователя по его имени:
```java
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
       Optional<UserCredential> credential = repository.findByName(username); //Создали метод который ищет пользователя по имени в БД используя репозиторий
        //Необходимо преобразовать объект типа UserCredential к объекту типа UserDetails и далее вернуть его в данном методе.
        //Преобразование в объект CustomUserDetails делаем с помощью вызова конструктора в классе CustomUserDetails
        //и проверяем существует ли такой объект с помощью orElseThrow.
        return credential.map(CustomUserDetails::new).orElseThrow(() -> new UsernameNotFoundException("User not found with name" + username));
    }
```
В этом методе с помощью бина репозитория UserCredentialRepository мы вызываем метод findByName()
и получаем объект пользователя по типу Optional, т.к. может возникнуть ситуация, когда пользователь не найден.
В методе реализовано преобразование из типа Optional в тип UserDetails. Для этого мы реализовали кастомный класс [CustomUserDetails.java](identity-service-new%2Fsrc%2Fmain%2Fjava%2Fcom%2Fkamenskiyandrey%2Fidentityservice%2Fconfig%2FCustomUserDetails.java),
который наследуется от UserDetails. В этом классе определили поля имя пользователя и пароль, а также определили конструктор,
с помощью которого инициализация полей будет производиться из гетторов объекта UserCredential,
тем самым и все поля учетных данных объекта UserDetails будут сопоставлены с полями UserDetails.
Далее в методе loadUserByUsername() на объекте c типом Optional мы вызываем метод map в параметр которого передаем
лямбда выражение сопоставления объектов и меняем объект обернутый в Optional с типом UserCredential на объект
с типом CustomUserDetails (который по сути является UserDetails объектом).

В классе конфигурации [AuthConfig.java](identity-service-new%2Fsrc%2Fmain%2Fjava%2Fcom%2Fkamenskiyandrey%2Fidentityservice%2Fconfig%2FAuthConfig.java) также определен бин AuthenticationProvider:
```java
    //Определили бин провайдера AuthenticationProvider, который реализован на основе класса DaoAuthenticationProvider
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService());
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthenticationProvider;
    }
```


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
С помощью метода permitAll() мы разрешили 
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
![Демонстрация регистрации Identity в Eureka.png](https://github.com/AndreyJavaEdu/microservices-currency-exchanger/blob/readme-file/%D0%A1%D1%85%D0%B5%D0%BC%D1%8B%20%D0%B4%D0%BB%D1%8F%20README/Identity/%D0%94%D0%B5%D0%BC%D0%BE%D0%BD%D1%81%D1%82%D1%80%D0%B0%D1%86%D0%B8%D1%8F%20%D1%80%D0%B5%D0%B3%D0%B8%D1%81%D1%82%D1%80%D0%B0%D1%86%D0%B8%D0%B8%20Identity%20%D0%B2%20Eureka.png)

<details><summary>Рассмотрим демонстрацию работы микросервиса регистрации и аутентификации пользователя. Для этого необходимо запустить контейнер postgres БД security,
сервис Eureka, микросервис шлюз Spring Cloud Gateway, микросервис регистрации и аутентификации identity-service:</summary>

1. Регистрация пользователя с именем Oleg и паролем 123456:

![1. Registration new user.png](https://github.com/AndreyJavaEdu/microservices-currency-exchanger/blob/readme-file/%D0%A1%D1%85%D0%B5%D0%BC%D1%8B%20%D0%B4%D0%BB%D1%8F%20README/Identity/Demonstration%20of%20work/1.%20Registration%20new%20user.png)

2. Получение токена для пользователя Oleg:

![2. Generation of token for user.png](https://github.com/AndreyJavaEdu/microservices-currency-exchanger/blob/readme-file/%D0%A1%D1%85%D0%B5%D0%BC%D1%8B%20%D0%B4%D0%BB%D1%8F%20README/Identity/Demonstration%20of%20work/2.%20Generation%20of%20token%20for%20user.png)

3. Валидация полученного токена:

![3. Валидация полученного токена.png](https://github.com/AndreyJavaEdu/microservices-currency-exchanger/blob/readme-file/%D0%A1%D1%85%D0%B5%D0%BC%D1%8B%20%D0%B4%D0%BB%D1%8F%20README/Identity/Demonstration%20of%20work/3.%20%D0%92%D0%B0%D0%BB%D0%B8%D0%B4%D0%B0%D1%86%D0%B8%D1%8F%20%D0%BF%D0%BE%D0%BB%D1%83%D1%87%D0%B5%D0%BD%D0%BD%D0%BE%D0%B3%D0%BE%20%D1%82%D0%BE%D0%BA%D0%B5%D0%BD%D0%B0.png)
</details>


### 6. микросервис по получению истории операций со счетами конкретного пользователя - [history-service](history-service)

Техлогии и библиотеки: spring-boot-starter 3.2.0, spring-boot-starter-data-jpa,
spring-cloud-starter-netflix-eureka-client,
lombok, spring-boot-starter-web, jackson-module-kotlin, spring-kafka,
postgresql, flyway-core.

У нас есть сервис процессинга, который совершает какие то операции со счетом. События всех этих операций
отправляется в виде сообщений в Kafka в виде JSON. И сервисы потребители ([history-service](history-service) и [notification-bot](notification-bot))
будут из Kafka-очереди вычитывать сообщения и сохранять в БД и Web-клиент сможет их получать по Рест-интерфейсу.
Т.е. модуль процессинга является поставщиком сообщений, а модуль истории является потребителем сообщений
из Kafka, будет их забирать и отправлять в БД.

![Схема работы.png](https://github.com/AndreyJavaEdu/microservices-currency-exchanger/blob/readme-file/%D0%A1%D1%85%D0%B5%D0%BC%D1%8B%20%D0%B4%D0%BB%D1%8F%20README/History%20service/%D0%A1%D1%85%D0%B5%D0%BC%D0%B0%20%D1%80%D0%B0%D0%B1%D0%BE%D1%82%D1%8B.png)

Чтобы проинсталировать Кафку на машину используем докер образы - [docker-compose.yml](docker-compose.yml):
```yaml
version: '3.1'

services:
  zookeeper:
    image: wurstmeister/zookeeper
    container_name: zookeeper
    ports:
      - "2181:2181"

  kafka:
    image: wurstmeister/kafka
    container_name: kafka
    ports:
      - "9092:9092"
    environment:
      KAFKA_ADVERTISED_HOST_NAME: localhost
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_CREATE_TOPICS: "account-events:1:1"
```
При запуске docker-compose файла создастся контейнеры с zookeeper и Кафкой с топиком account-events, имеющим
1 partition и 1 replication factor. В качестве пет проекта этого будет достаточно для демонстрации, как работать с брокером Кафка.

#### Описание Producer-а, которым является микросервис процессинга [exchange-processing-service](exchange-processing-service)
В микросервисе процессинга реализовали отправку сообщений в Кафку. Для этого в микросервисе
процессинга в [pom.xml](exchange-processing-service%2Fpom.xml)pom.xml файле добавлена зависимость, чтобы Spring понял как работать с Кафкой в качестве Producer:
```xml
<dependency>
   <groupId>org.springframework.kafka</groupId>
   <artifactId>spring-kafka</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka-test</artifactId>
    <scope>test</scope>
</dependency>
```
Также в модуле процессинга сделали основные настройки Продюссера в [application.yml](exchange-processing-service%2Fsrc%2Fmain%2Fresources%2Fapplication.yml):
```yaml
spring:
  application:
    name: exchange-processing-service
#Настроили порт кафки
  kafka:
    producer:
      bootstrap-servers: ${cloud.kafka-host}:9092
      key-serializer: org.apache.kafka.common.serialization.LongSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
      request:
        timeout:
          ms: 1000
```
Мы указали host по которому микросервис процессинга будет обращаться в Кафку (`bootstrap-servers: ${cloud.kafka-host}:9092`).
Также мы добавили настройку сериализатора 
(т.е. объект который мы будем помещать в топик необходимо будет сериализовать и потом десериализовать).
В сервисе процессинга будет происходить только сериализация объекта (причем как ключа, так и значения)
и отправка в топик.
Основные классы для сериализации в библиотеки Кафки уже есть, поэтому добавили соответствующие настройки -
key-serializer и value-serializer. В нашем случае сериализатор для ключа - в результате сериализации
будет число. И сериализатор для значения - обычная строка.

Чтобы сгенерировать сообщение для события операции со счетом мы реализовали модель события - [AccountEvent.java](exchange-processing-service%2Fsrc%2Fmain%2Fjava%2Fio%2FkamenskiyAndrey%2FprocessingService%2Fprocessing%2Fmodel%2FAccountEvent.java).

<details>

  <summary>AccountEvent</summary>

```java
@Data
@Builder
public class AccountEvent {
    @NonNull
    private String uuid; //уникальный номер операции

    private long userId, accountId;

    private Long fromAccount; //откуда списаны деньги

    @NonNull
    private String currencyCode; //код валюты

    @NonNull
    private Operation operation; // Операции - пополнение счета и перевод (тип операции)

    @NonNull
    private BigDecimal amount; //Сумма перевода

    @NonNull
    private Date created; //Дата создания
}
```

</details>

Также создали enum класс [Operation.java](exchange-processing-service%2Fsrc%2Fmain%2Fjava%2Fio%2FkamenskiyAndrey%2FprocessingService%2Fprocessing%2Fmodel%2FOperation.java) в котором обозначили две операции - пополнение счета и перевод:
```java
public enum Operation {
    PUT, EXCHANGE
}
```

Далее мы реализовали класс-сервис для отправки сообщения в Кафку - [AccountEventSendingService.java](exchange-processing-service%2Fsrc%2Fmain%2Fjava%2Fio%2FkamenskiyAndrey%2FprocessingService%2Fprocessing%2Fservice%2FAccountEventSendingService.java).
В данном классе будет единственный метод - sendEvent():
```java
    public void sendEvent(AccountEvent event) {
        //получили id счета
        var accountId = event.getAccountId();
        //формируем сообщение в Кафку, из объекта переводим в строку типа Json
        String message;
        try {
            message = mapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        //Отправим событие в ввиде сериализованного сообщения втопик - это асинхронная операция
        var future = kafkaTemplate.send(ACCOUNT_EVENTS, accountId, message);
        //Убеждыемся, что сообщение дошло до топика и туда сохранилось
        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
```
В параметры метод принимает объект реализованного класса [AccountEvent.java](exchange-processing-service%2Fsrc%2Fmain%2Fjava%2Fio%2FkamenskiyAndrey%2FprocessingService%2Fprocessing%2Fmodel%2FAccountEvent.java).
В самом методе мы сразу получаем ключ - accountId, а также сообщение message в виде строки, которое преобразуется из объекта в строку
с помощью метода объекта mapper - writeValueAsString().
Для отправки сообщения будем использовать объект класса KafkaTemplate:
```java
 private final KafkaTemplate<Long, String> kafkaTemplate;

    @Autowired
    public AccountEventSendingService(KafkaTemplate<Long, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
```
На объекте KafkaTemplate мы вызываем метод send() в параметрах которого указываем название топика, 
ключ и само сообщение.
Чтобы убедиться, что сообщение дошло до топика и туда сохранилось мы используем метод get() на объекте future
и если данный метод срабатывает без ошибки, то мы можем быть уверены, что сообщение дошло до топика.

Наиболее удобным и правильным способом вызвать метод sendEvent() будет после комита транзакции,
где мы совершаем операцию на счете. Т.е. необходимо реализовать слушателя транзакции и после его успешного завершения
мы можем отправить данные в топик.

Для этого реализовали слушателя транзакции - [AccountOperationEventListener.java](exchange-processing-service%2Fsrc%2Fmain%2Fjava%2Fio%2FkamenskiyAndrey%2FprocessingService%2Fprocessing%2Fservice%2FAccountOperationEventListener.java).
В данном классе реализован метод по обработке события по завершению транзакции - handleEvent():
```java
    @TransactionalEventListener
    public void handleEvent(AccountEvent event) {
        sendingService.sendEvent(event);
    }
```
Внутри данный метод вызывает метод отправки сообщения в топик Кафки.
Данный метод помечен аннотацией @TransactionalEventListener, которая говорит что данный 
метод сработает после коммита транзакции.

Чтобы отправить событие мы реализовали метод по генерации события в классе-сервисе [AccountCreateService.java](exchange-processing-service%2Fsrc%2Fmain%2Fjava%2Fio%2FkamenskiyAndrey%2FprocessingService%2Fprocessing%2Fservice%2FAccountCreateService.java):
```java
    //Метод генерации события
    private AccountEvent createEvent(String uid, AccountEntity account, Long fromAccount, Operation operation, BigDecimal amount){
        var currentDate = new Date();
        return AccountEvent.builder()
                .uuid(uid)
                .accountId(account.getId())
                .currencyCode(account.getCurrencyCode())
                .userId(account.getUserId())
                .fromAccount(fromAccount)
                .operation(operation)
                .amount(amount)
                .created(currentDate)
                .build();
    }
```
За счет билдера мы заполняем необходимые атрибуты объекта AccountEvent.
Далее в методе addMoneyToAccount() мы генерируем и публикуем событие вызвав метод createEvent() в параметре метода publishEvent()
на бине ApplicationEventPublisher. Событие публикуется в контекст Spring. После того, как транзакция завершается и фиксируется
коммитом срабатывает метод handleEvent(), который является слушателем события. Этот метод принимает в параметр это событие и 
передает его методу sendEvent(), который отправляет событие в виде сообщения в Кафку.
<details><summary>Демонстрация отправки события в 
топик Кафки микросервисом [exchange-processing-service](exchange-processing-service):</summary>

1. Сначало авторизуемся и получим токен для пользователя Misha:

![1. Аутентификация пользователя Миша и получения токена.png](https://github.com/AndreyJavaEdu/microservices-currency-exchanger/blob/readme-file/%D0%A1%D1%85%D0%B5%D0%BC%D1%8B%20%D0%B4%D0%BB%D1%8F%20README/%D0%9E%D1%82%D0%BF%D1%80%D0%B0%D0%B2%D0%BA%D0%B0%20%D1%81%D0%BE%D0%B1%D1%8B%D1%82%D0%B8%D1%8F%20%D0%B2%20%D1%82%D0%BE%D0%BF%D0%B8%D0%BA%20%D0%9A%D0%B0%D1%84%D0%BA%D0%B8/1.%20%D0%90%D1%83%D1%82%D0%B5%D0%BD%D1%82%D0%B8%D1%84%D0%B8%D0%BA%D0%B0%D1%86%D0%B8%D1%8F%20%D0%BF%D0%BE%D0%BB%D1%8C%D0%B7%D0%BE%D0%B2%D0%B0%D1%82%D0%B5%D0%BB%D1%8F%20%D0%9C%D0%B8%D1%88%D0%B0%20%D0%B8%20%D0%BF%D0%BE%D0%BB%D1%83%D1%87%D0%B5%D0%BD%D0%B8%D1%8F%20%D1%82%D0%BE%D0%BA%D0%B5%D0%BD%D0%B0.png)

2. Получим список счетов для пользователя Misha:

![2. Получение списка счетов.png](https://github.com/AndreyJavaEdu/microservices-currency-exchanger/blob/readme-file/%D0%A1%D1%85%D0%B5%D0%BC%D1%8B%20%D0%B4%D0%BB%D1%8F%20README/%D0%9E%D1%82%D0%BF%D1%80%D0%B0%D0%B2%D0%BA%D0%B0%20%D1%81%D0%BE%D0%B1%D1%8B%D1%82%D0%B8%D1%8F%20%D0%B2%20%D1%82%D0%BE%D0%BF%D0%B8%D0%BA%20%D0%9A%D0%B0%D1%84%D0%BA%D0%B8/2.%20%D0%9F%D0%BE%D0%BB%D1%83%D1%87%D0%B5%D0%BD%D0%B8%D0%B5%20%D1%81%D0%BF%D0%B8%D1%81%D0%BA%D0%B0%20%D1%81%D1%87%D0%B5%D1%82%D0%BE%D0%B2.png)

3. В Web-клиенте (Postman) создадим какую нибудь операцию со счетом с идентификатором 1 
(увеличили сумму на счете на 1000):

![3. Увеличили счет 1 на 1000.png](https://github.com/AndreyJavaEdu/microservices-currency-exchanger/blob/readme-file/%D0%A1%D1%85%D0%B5%D0%BC%D1%8B%20%D0%B4%D0%BB%D1%8F%20README/%D0%9E%D1%82%D0%BF%D1%80%D0%B0%D0%B2%D0%BA%D0%B0%20%D1%81%D0%BE%D0%B1%D1%8B%D1%82%D0%B8%D1%8F%20%D0%B2%20%D1%82%D0%BE%D0%BF%D0%B8%D0%BA%20%D0%9A%D0%B0%D1%84%D0%BA%D0%B8/3.%20%D0%A3%D0%B2%D0%B5%D0%BB%D0%B8%D1%87%D0%B8%D0%BB%D0%B8%20%D1%81%D1%87%D0%B5%D1%82%201%20%D0%BD%D0%B0%201000.png)

4. С помощью командной строки и слудующей команды удостоверимся, что сыбытие об увеличении
суммы на счете записалось в топик Кафки:
```shell
docker exec -ti kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic account-events --from-beginning
```
![4. Демонстрация, что событие записалось в топик.png](https://github.com/AndreyJavaEdu/microservices-currency-exchanger/blob/readme-file/%D0%A1%D1%85%D0%B5%D0%BC%D1%8B%20%D0%B4%D0%BB%D1%8F%20README/%D0%9E%D1%82%D0%BF%D1%80%D0%B0%D0%B2%D0%BA%D0%B0%20%D1%81%D0%BE%D0%B1%D1%8B%D1%82%D0%B8%D1%8F%20%D0%B2%20%D1%82%D0%BE%D0%BF%D0%B8%D0%BA%20%D0%9A%D0%B0%D1%84%D0%BA%D0%B8/4.%20%D0%94%D0%B5%D0%BC%D0%BE%D0%BD%D1%81%D1%82%D1%80%D0%B0%D1%86%D0%B8%D1%8F%2C%20%D1%87%D1%82%D0%BE%20%D1%81%D0%BE%D0%B1%D1%8B%D1%82%D0%B8%D0%B5%20%D0%B7%D0%B0%D0%BF%D0%B8%D1%81%D0%B0%D0%BB%D0%BE%D1%81%D1%8C%20%D0%B2%20%D1%82%D0%BE%D0%BF%D0%B8%D0%BA.png)
</details>

#### Описание Consumer-а, которым является микросервис истории [history-service](history-service)
Итак мы убедились что публикация в топик Кафки работает, теперь рассмотрим работу микросервиса истории [history-service](history-service),
который выступает как потребитель этих событий из топика Кафки.
Сам микросервис реализован на языке Kotlin.
Создана БД для хранения истории с именем - account-history. В пакете [migration](history-service%2Fsrc%2Fmain%2Fresources%2Fdb%2Fmigration) для миграций БД созданы два скрипта
для двух таблиц. Первый скрипт - это операции [V2__History_events.sql](history-service%2Fsrc%2Fmain%2Fresources%2Fdb%2Fmigration%2FV2__History_events.sql),
а второй скрипт - это история счетов для второй таблицы [V2__History_events.sql](history-service%2Fsrc%2Fmain%2Fresources%2Fdb%2Fmigration%2FV2__History_events.sql).

Создание таблицы OPERATION:
```postgresql
create table OPERATION
(
    ID smallint not null primary key,
    OPERATION_CODE varchar(8) not null
);

INSERT INTO OPERATION(ID, OPERATION_CODE) VALUES (1, 'PUT'), (2, 'EXCHANGE');
```
Создание таблицы ACCOUNT_EVENT:
```postgresql
create table ACCOUNT_EVENT
(
    uuid varchar(120) not null,
    user_id bigint not null,
    account_id bigint not null,
    from_account bigint,
    currency_code varchar(3) not null,
    operation_code smallint not null,
    amount bigint not null,
    date_creation_event timestamp not null,
    primary key (uuid, account_id)
);
```
Проект сконфигурирован в [application.yml](history-service%2Fsrc%2Fmain%2Fresources%2Fapplication.yml) -
добавлена конфигурация для БД:
```yaml
spring:
  application:
    name: history-service

  datasource:
    url: jdbc:postgresql://${cloud.db-host}:5433/account-history
    username: postgres
    password: password
    driver-class-name: org.postgresql.Driver
```
Конфигурация для миграции БД с помощью Flyway:
```yaml
  flyway:
    enabled: true
    locations: classpath:db
    url: jdbc:postgresql://${cloud.db-host}:5433/account-history
    user: postgres
    password: password
  jpa:
    show-sql: true
    hibernate:
      ddl-auto: none
```
Для настройки Consumer-а Кафки мы указали настройку десериалайзера, а также группу потребителей,
т.к. Офсетты комитятся не в рамках конкретного потребителя, а врамках группы потребителей.
```yaml
  kafka:
    consumer:
      bootstrap-servers: ${cloud.kafka-host}:9092
      key-deserializer: org.apache.kafka.common.serialization.LongDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      group-id: ${spring.application.name}-group
#      для считывания начального off set для группы потребителей
      auto-offset-reset: earliest
```
Также настроили оффсет настройкой 'auto-offset-reset: earliest' , чтобы при запуске данного микросервиса 
мы считывали не последний, а начальный оффсет для данной группы.

Реализована модель события в классе [AccountEvent.kt](history-service%2Fsrc%2Fmain%2Fkotlin%2Fio%2FkamenskiyAndrey%2Fhistory%2Fmodel%2FAccountEvent.kt)
в пакете [model](history-service%2Fsrc%2Fmain%2Fkotlin%2Fio%2FkamenskiyAndrey%2Fhistory%2Fmodel). 
<details><summary>Данная модель будет использоваться как Entity для сохранения в БД:</summary>

```Kotlin
@Entity
@IdClass(EventKey::class)
@Table(name = "ACCOUNT_EVENT")
data class AccountEvent(
        @Id
        @Column(name = "uuid", nullable = false)
        val uuid: String,

        @Id
        @Column(name = "account_id", nullable = false)
        val accountId: Long,

        @Column(name = "user_id", nullable = false)
        val userId: Long,

        @Column(name = "from_account", nullable = true)
        val fromAccount: Long?,

        @Column(name = "currency_code", nullable = false)
        val currencyCode: String,

        @Column(name = "operation_code", nullable = false)
        val operation: Operation,

        @Column(name = "amount", nullable = false)
        val amount: BigDecimal,

        @Column(name = "date_creation_event", nullable = false)
        val created: Date,
)

//Класс составного ключа
@Embeddable // пометили данный класс как встраиваемый в класс AccountEvent
class EventKey(
        val uuid: String,
        val accountId: Long
): Serializable
```
</details>
Идентификатор в данном случае реализован как составной и состоит из двух полей - uuid и accountId. Для этого реализован встраиваемый класс
и помеченный аннотацией @Embeddable, содержащий поля составного Primary Key.

Реализован репозиторий [AccountEventRepository.kt](history-service%2Fsrc%2Fmain%2Fkotlin%2Fio%2FkamenskiyAndrey%2Fhistory%2Frepository%2FAccountEventRepository.kt) в пакете [repository](history-service%2Fsrc%2Fmain%2Fkotlin%2Fio%2FkamenskiyAndrey%2Fhistory%2Frepository)
для того чтобы мы могли сохранять наши события с операциями со счетом в БД. 

Далее реализован сервис-класс [AccountEventKafkaListener.kt](history-service%2Fsrc%2Fmain%2Fkotlin%2Fio%2FkamenskiyAndrey%2Fhistory%2FAccountEventKafkaListener.kt), который выполняет роль Consumer-а из топика Кафки.
Данный класс инжектит бин репозитория [AccountEventRepository.kt](history-service%2Fsrc%2Fmain%2Fkotlin%2Fio%2FkamenskiyAndrey%2Fhistory%2Frepository%2FAccountEventRepository.kt).
```Kotlin
@Service
class AccountEventKafkaListener(private val repository: AccountEventRepository) {
   private val mapper = jacksonObjectMapper() //тут получаем мэппер для десериализации данных из топика Кафки


   //Метод обработки события полученного из Kafka
   @KafkaListener(topics = ["account-events"]) //указали что данным методом мы читаем из топика кафки
   fun consumerEvent(record: ConsumerRecord<Long, String>) {
      //получаем ключ и значение записи
      val key = record.key()
      val value = record.value()
      //десериализовываем  объект entity - AccountEvent
      val event: AccountEvent = try {
         mapper.readValue(value)
      }catch (e: Exception){
         throw e
      }
      //сохраняем в БД
      repository.save(event)
   }
}
```
В данном классе используется объект jacksonObjectMapper, который применяется для десериализации сообщения
из Кафки в объект Kotlin по типу AccountEvent для дальнейшего его сопоставления с таблицей БД и сохранения в БД.
В классе также реализована функция consumerEvent, которая помечена аннотацией
@KafkaListener(topics = ["account-events"]), в параметре аннотации указан топик, откуда будет происходить чтение сообщения
Кафки. Сам этот метод в параметре принимает объект record по типу ConsumerRecord<Long, String>.
record - это запись или сообщение в Кфаке. Из записи record мы получаем ключ и значение. Далее значение
мы десериализовываем с помощью объекта mapper-а, который создан в классе. После десериализации мы получаем объект event
и далее с помощью стандартного мтеода save() JPA, вызванного на бине репозитория, сохраняем этот объект в БД.

Далее реализован Рест-интерфейс, а именно класс [AccountEventService.kt](history-service%2Fsrc%2Fmain%2Fkotlin%2Fio%2FkamenskiyAndrey%2Fhistory%2Fservice%2FAccountEventService.kt),
а также Рест-контроллер [AccountHistoryController.kt](history-service%2Fsrc%2Fmain%2Fkotlin%2Fio%2FkamenskiyAndrey%2Fhistory%2Fcontroller%2FAccountHistoryController.kt) для
В контроллере реализован метод по получению списка всех событий по операция со счетом:
- GET - findAllOperationsInAccountHistory() - в данном методе в параметр поступает идентификатор пользователя
из Хэдера запроса через Gateway. Пользователь вводит лишь идентификатор своего счета. Далее
по идентификатору пользователя и идентификатору счета (составной Primary Key) происходит выборка из таблицы
всех сохраненный событий по операциям со счетом пользователя.

Демонстрация получения списка событий по счету пользователя через Postman:

![Получение списка операций со счетом пользователя.png](https://github.com/AndreyJavaEdu/microservices-currency-exchanger/blob/readme-file/%D0%A1%D1%85%D0%B5%D0%BC%D1%8B%20%D0%B4%D0%BB%D1%8F%20README/History%20service/%D0%9F%D0%BE%D0%BB%D1%83%D1%87%D0%B5%D0%BD%D0%B8%D1%8F%20%D1%81%D0%BF%D0%B8%D1%81%D0%BA%D0%B0%20%D0%B2%D1%81%D0%B5%D1%85%20%D1%81%D0%BE%D0%B1%D1%8B%D1%82%D0%B8%D0%B9%20%D0%B4%D0%BB%D1%8F%20%D1%81%D1%87%D0%B5%D1%82%D0%B0/%D0%9F%D0%BE%D0%BB%D1%83%D1%87%D0%B5%D0%BD%D0%B8%D0%B5%20%D1%81%D0%BF%D0%B8%D1%81%D0%BA%D0%B0%20%D0%BE%D0%BF%D0%B5%D1%80%D0%B0%D1%86%D0%B8%D0%B9%20%D1%81%D0%BE%20%D1%81%D1%87%D0%B5%D1%82%D0%BE%D0%BC%20%D0%BF%D0%BE%D0%BB%D1%8C%D0%B7%D0%BE%D0%B2%D0%B0%D1%82%D0%B5%D0%BB%D1%8F.png)



### 7. Микросервис нотификации - телеграмм бот, реализующий оповещение пользователя - [notification-bot](notification-bot)

Данный микросервис реализован на языке Kotlin и Spring boot.

Техлогии и библиотеки: spring-boot-starter 3.2.2, telegrambots-spring-boot-starter:6.8.0,
spring-boot-starter-web, jackson-module-kotlin, spring-kafka, 
зависимости для работы с jjwt (jjwt-api, jjwt-impl, jjwt-jackson).

Данный микросервис работает, с одной стороны как потребитель (Consumer) Кафка топику, а
с другой стороны взаимодействуем с телеграмм API.
Т.е. пользователь устанавливает своего телеграмм бота и авторизовавшись получает уведомление
об операциях по своим счетам, которые хранятся в топике Кафка.
Телеграмм бот работает с пользователем, как обычный чат, через текстовые команды.
Пользователь отправляет телеграмм боту текстовую команду на подписку, вводит свой логин и пароль
и после этого может получать новые события из топика Кафки.

Для начало была произведена регистрация Телеграмм бота в приложении Telegram
с помощью бота BotFather и получен секретный ключ (токен) для данного
бота также от BotFather.
BotFather - это главный телеграмм бот с помощью которого мы можем создавать собственные телеграмм боты. 
Для регистрации бота необходимо отправить команду /newbot в BotFather, после чего он попросит ввести
имя бота и ввести username для нашего бота. После этого мы получаем сообщение от BotFather,
то что наш телеграмм бот зарегистрирован и в сообщении нам также предоставляется секретный access token.
Данный токен сохранен в пакете [conf](notification-bot%2Fconf) в файле. Секретный токен никому нельзя показывать.

После чего был разработан данный модуль-микросервис с помощью Spring Boot и 
стартера telegrambots-spring-boot-starter для взаимодействия с Telegram API.
Данный модуль взаимодействует с Telegram API через синхронные HTTP вызовы - через операцию getUpdates. И через
setUpdates отправляет сообщения пользователю. Т.е. технически реализация модуля построена на очередях сообщений
на сервере Telegram. Каждое сообщение от клиента имеет свой оффсет. Телеграм-бот запрашивает команды getUpdates
именно по оффсету, но если оффсет не задан, либо нулевой, то Телеграмм-бот получает все новые сообщения.

![Схема работы Телеграм бота.png](https://github.com/AndreyJavaEdu/microservices-currency-exchanger/blob/readme-file/%D0%A1%D1%85%D0%B5%D0%BC%D1%8B%20%D0%B4%D0%BB%D1%8F%20README/Notification/%D0%A1%D1%85%D0%B5%D0%BC%D0%B0%20%D1%80%D0%B0%D0%B1%D0%BE%D1%82%D1%8B%20%D0%A2%D0%B5%D0%BB%D0%B5%D0%B3%D1%80%D0%B0%D0%BC%20%D0%B1%D0%BE%D1%82%D0%B0.png)

Сознан класс-агента [TelegramSubscriptionServiceAgent.kt](notification-bot%2Fsrc%2Fmain%2Fkotlin%2Fio%2FkamenskiyAndrey%2FcurrencyExchanger%2Fnotification%2Fservice%2FTelegramSubscriptionServiceAgent.kt) для работы с API Telegram в пакете [service](notification-bot%2Fsrc%2Fmain%2Fkotlin%2Fio%2FkamenskiyAndrey%2FcurrencyExchanger%2Fnotification%2Fservice).
Данный класс является наследником от класса TelegramLongPollingBot из стартера telegrambots-spring-boot-starter.
Имплементировали его два метода - getBotUsername() и onUpdateReceived(), а также переопределили конструктор, 
в который передается один параметр - этот access token (как раз тот токен, который мы получили от FatherBot).
Именно с помощью метода onUpdateReceived() мы будем получать сообщения из Telegram.


Перед реализацией метода onUpdateReceived() необходимо сконфигурировать телеграм бот, определить его имя и его токен.
После добавления аннотации @Component над классом [TelegramSubscriptionServiceAgent.kt](notification-bot%2Fsrc%2Fmain%2Fkotlin%2Fio%2FkamenskiyAndrey%2FcurrencyExchanger%2Fnotification%2Fservice%2FTelegramSubscriptionServiceAgent.kt)
то видим, что Spring Boot не знает как заполнить параметр в конструкторе - токен. И этот токен мы будем 
получать из файла посредством конфигурации в [application.yml](notification-bot%2Fsrc%2Fmain%2Fresources%2Fapplication.yml)
и добавления специального конфигурационного класса для телеграмм бота - [TelegramBotConfig.kt](notification-bot%2Fsrc%2Fmain%2Fkotlin%2Fio%2FkamenskiyAndrey%2FcurrencyExchanger%2Fnotification%2Fconfig%2FTelegramBotConfig.kt).

Реализовали класс [BotSettings.kt](notification-bot%2Fsrc%2Fmain%2Fkotlin%2Fio%2FkamenskiyAndrey%2FcurrencyExchanger%2Fnotification%2Fconfig%2FBotSettings.kt) для настроек бота
в пакете [config](notification-bot%2Fsrc%2Fmain%2Fkotlin%2Fio%2FkamenskiyAndrey%2FcurrencyExchanger%2Fnotification%2Fconfig).
Этот класс является data class. В этом классе два параметра - имя бота (nameOfBot) и его токен (token):
```Kotlin
data class BotSettings(val nameOfBot: String, val token: String)
```
Добавили в конфигурационный файл [application.yml](notification-bot%2Fsrc%2Fmain%2Fresources%2Fapplication.yml)
настройку для чтения токена из файла, а также имя самого бота, который мы ранее зарегистрировали
с помощью BotFather:
```yaml
telegram:
    botName: java-bot-notification
    token: ${TELEGRAM_BOT_TOKEN}
```
Путь к файлу с токеном мы прочитаем из переменной окружения.

Реализовали класс конфигурации [TelegramBotConfig.kt](notification-bot%2Fsrc%2Fmain%2Fkotlin%2Fio%2FkamenskiyAndrey%2FcurrencyExchanger%2Fnotification%2Fconfig%2FTelegramBotConfig.kt), в котором реализовали чтение токена из файла в виде строки.
Данный класс помечен аннотацией @Configuration. В данном классе реализована функция чтения файла конфигурации и 
получения имени и токена бота:
```Kotlin
@Configuration
class TelegramBotConfig() {
    @Autowired
    private lateinit var appContext: ApplicationContext //инжектим данный бин, чтобы мы могли прочитать файлы из внешнего ресурса

    //также подключим наши настройки которые мы указали в application.yml с помощью аннотации Value мы их достанем из файла application
    @Value("\${telegram.botName}")
    private val botName: String = ""

    @Value("\${telegram.token}")
    private val token: String = ""

    /*Функция чтения файла конфигурации и получение имени и токена.
    Путь к файлу с токеном прописан в переменной окружения и используется при запуске сервиса.
    Сам файл с токеном телеграм бота находится в папке conf в проекте.
     */
    @Bean
    fun botSettings(): BotSettings {
        //реализуем чтение контента из файла с токеном
        val resource = appContext.getResource("file:$token")
        val content = if (resource.exists()) resource.file.readText()
        else throw RuntimeException("File $token not found !!!")
        return BotSettings(botName, content) //заполняем бин именем бота и его токеном
    }
}
```
В данном классе мы достали настройки из application.yml, с помощью аннотации @Value.
Здесь реализован метод botSettings(), с помощью которого происходит чтение контента из файла с токеном.
Чтение это происходит с использованием метода getResource() вызванного на Спринговом бине ApplicationContext, который
мы заинжектили. Если файл с токеном не найден то будет выбрашено исключение с сообщением, что файл мы не нашли.
После чтения контента из файла в методе происходит заполнение бина BotSettings такими настройками, как
имя бота botName и токен - content, который прочитан из файла с помощью метода объекта бина ApplicationContext.

Итак мы заинжектили уже заполненный бин BotSettings в классе телеграмм агента - [TelegramSubscriptionServiceAgent.kt](notification-bot%2Fsrc%2Fmain%2Fkotlin%2Fio%2FkamenskiyAndrey%2FcurrencyExchanger%2Fnotification%2Fservice%2FTelegramSubscriptionServiceAgent.kt).

Сам механиз подписки реализован в виде сервис-класса [SubscriptionService.kt](notification-bot%2Fsrc%2Fmain%2Fkotlin%2Fio%2FkamenskiyAndrey%2FcurrencyExchanger%2Fnotification%2Fservice%2FSubscriptionService.kt).
По сути это просто класс в котором в ХэшМэпе хранятся данные о подписке. В классе объявлено поле subscriptions
по типу ConcurrentHashMap в Дженериках, которого ключ - это идентификатор пользователя и значение - это идентификатор чата (chatId).
В данном классе реализован метод subscribeUser(userId: Long, chatId: Long), который принимает в параметр userId и chatId.
Отправить сообщение боту можно только в контексте какого то конкретного идентификатора чата (chatId).
```Kotlin
fun subscribeUser(userId: Long, chatId: Long) = subscriptions.put(userId, chatId)
```
Данный метод добавляет пару ключ и значение в объявленный subscriptions, как ConcurrentHashMap, тем самым происходит подписка пользователя.

Также в данном классе реализована функция отписки пользователя:
```Kotlin
fun unSubscribeUser(chatId: Long) = subscriptions.entries.removeIf { (_, value) -> value==chatId }
```
Отписаться пользователь может лишь по chatId, и необязательно ему знать идентификатор пользователя, главное знать идентификатор чата бота.
Т.е. происходит поиск соответствующего объекта (entry) внутри мапы subscriptions и и далее этот объект состоящий из ключа и значения удаляется,
тем самым производится отписка пользователя от чата телеграм бота.

А также в данном сервис-классе реализован метод получения подписки для конкретного пользователя:
```Kotlin
 //Метод получения подписки для конкретного пользователя. (мы будем отдавать chat id для конкретного пользователя)
    fun getSubscription(userId: Long): Long? = subscriptions[userId]
```
Метод возвращает chatId по UserId из мапы, но chatId может быть и не найден, поэтому тип может быть nullable.

Далее чтобы класс подписки пользователя работал, мы реализовали также коммандеры этих подписок.
Мы реализовали это через процессор комманд.

Также мы реализовали класс-сервис [ServiceToGetUserId.kt](notification-bot%2Fsrc%2Fmain%2Fkotlin%2Fio%2FkamenskiyAndrey%2FcurrencyExchanger%2Fnotification%2Fservice%2FServiceToGetUserId.kt)
с методом по получению токена getToken(), а также кастомный утилитарный класс помеченный как компонент [UtilJwtGettingPayload.kt](notification-bot%2Fsrc%2Fmain%2Fkotlin%2Fio%2FkamenskiyAndrey%2FcurrencyExchanger%2Fnotification%2Futil%2FUtilJwtGettingPayload.kt)
с методом extractUserId() для извлечения из токена идентификатора пользователя userId. Для 
получения токена используется механизм RestTemplate для запроса в микросервис аутентификации и регистрации - identity-service.

Компндеры подписки и отписки пользователя реализованы в классах в пакете [command](notification-bot%2Fsrc%2Fmain%2Fkotlin%2Fio%2FkamenskiyAndrey%2FcurrencyExchanger%2Fnotification%2Fservice%2Fcommand).
Классы командеры имеют похожую структуру, поэтому реализован интерфейс [BotCommandProcessor.kt](notification-bot%2Fsrc%2Fmain%2Fkotlin%2Fio%2FkamenskiyAndrey%2FcurrencyExchanger%2Fnotification%2Fservice%2Fcommand%2FBotCommandProcessor.kt),
в котором определен один метод processMessage():
```Kotlin
interface BotCommandProcessor {
    fun processMessage(message: String, chatId: Long): String
}
```
На вход данного метода поступает сообщение типа String и chatId типа Long. Метод будет
возвращать какую то строку, чтобы оповестить пользователя об успешности или неуспешности операции.

Реализация метода данного интерфейса выполнена в созданных двух классах [SubscribeCommand.kt](notification-bot%2Fsrc%2Fmain%2Fkotlin%2Fio%2FkamenskiyAndrey%2FcurrencyExchanger%2Fnotification%2Fservice%2Fcommand%2FSubscribeCommand.kt) и [UnsubscribeCommand.kt](notification-bot%2Fsrc%2Fmain%2Fkotlin%2Fio%2FkamenskiyAndrey%2FcurrencyExchanger%2Fnotification%2Fservice%2Fcommand%2FUnsubscribeCommand.kt) - 
по созданию подписки и по отмене подписки пользователь.

<details><summary>Разберем работу метода processMessage() в классе SubscribeCommand для осуществления подписки пользователя: </summary>

```Kotlin
 override fun processMessage(message: String, chatId: Long): String {
        val (user, password) = message.split(":")
            .let { array -> array[0] to array[1] } // из введеного сообщения мы получаем логин и пароль пользователя

        val credentials = GetTokenCredentialsDTO(name = user, password = password)
        logger.info("Получение объекта ДТО - {}", credentials)

        val token = authService.getToken(credentials)
        logger.info("Получыенный токен - {}", token)
        if (token != null) {
            val userId = utilJwt.extractUserId(token)
            logger.info("Полученный userId - {}", userId)
            subscriptionService.subscribeUser(userId.toLong(), chatId)
            return "Подписка оформлена"
        } else {
            throw Exception("Токен не обнаружен, возможно не аутентифицирован пользователь")
        }
    }
```
Для работы метода заинжектили бины [SubscriptionService.kt](notification-bot%2Fsrc%2Fmain%2Fkotlin%2Fio%2FkamenskiyAndrey%2FcurrencyExchanger%2Fnotification%2Fservice%2FSubscriptionService.kt),
[ServiceToGetToken.kt](notification-bot%2Fsrc%2Fmain%2Fkotlin%2Fio%2FkamenskiyAndrey%2FcurrencyExchanger%2Fnotification%2Fservice%2FServiceToGetToken.kt),
а также [UtilJwtGettingPayload.kt](notification-bot%2Fsrc%2Fmain%2Fkotlin%2Fio%2FkamenskiyAndrey%2FcurrencyExchanger%2Fnotification%2Futil%2FUtilJwtGettingPayload.kt).
Из введеного сообщения в телеграм бот пользователем, мы должны получить два параметра -
это логин пользователя и его пароль. Далее созданный объект класса DTO [GetTokenCredentialsDTO.kt](notification-bot%2Fsrc%2Fmain%2Fkotlin%2Fio%2FkamenskiyAndrey%2FcurrencyExchanger%2Fnotification%2Fmodel%2FGetTokenCredentialsDTO.kt)
заполняется логином и паролем. Далее на бине по типу ServiceToGetToken вызывается его метод
getToken() и в параметр метода поступает ранее заполненный объект DTO с именем credentials.
Т.о. мы получаем сам Токен из сервиса аутентификации. После этого проверяем если полученный
токен не null, то извлекаем из его payload userId и далее на бине SubscriptionService
мы вызываем метод subscribeUser() для выполнения подписки пользователя, который принимает в параметр userId
и chatId.
</details>

<details><summary>Теперь разберем работу метода processMessage() в классе UnsubscribeCommand для отписки пользователя:</summary>

```Kotlin
    override fun processMessage(message: String, chatId: Long): String {
   subscriptionService.unSubscribeUser(chatId)
   return "Подписка отменена"
}
```
Для работы метода в данном классе заинжектили бин сервиса подписок [SubscriptionService.kt](notification-bot%2Fsrc%2Fmain%2Fkotlin%2Fio%2FkamenskiyAndrey%2FcurrencyExchanger%2Fnotification%2Fservice%2FSubscriptionService.kt).
С помощью данного бина вызываем метод unSubscribeUser() для отмены подписки, который в параметр принимает chatId. И возвращаем сообщение, что
подписка отменена для информирования пользователя.
</details>

Реализован класс агента [TelegramSubscriptionServiceAgent.kt](notification-bot%2Fsrc%2Fmain%2Fkotlin%2Fio%2FkamenskiyAndrey%2FcurrencyExchanger%2Fnotification%2Fservice%2FTelegramSubscriptionServiceAgent.kt) и его методы.
Токен заполняется в конструкторе из бина BotSettings. Имя бота мы получаем в функции getBotUsername() также из бина
BotSettings.
<details><summary>Далее реализован метод отправки сообщений sendNotification():</summary>

```Kotlin
    fun sendNotification(chatId: Long, responseText: String) {
        val responseMessage =
            SendMessage(chatId.toString(), responseText) //создали объект сообщения для отправки в чат бота
        execute(responseMessage) // данным методом отправляем сообщение
    }
```
Метод принимает chatId и текст ответа. Внутри метода создается объект responseMessage, который является объектом
класса SendMessage. В конструктор которого передается chatId в виде строки и текст ответа.
После этого применяется метод execute в параметр которого передается данный созданный объект responseMessage - 
этим методом отправляется сообщение ботом пользователю.
</details>

<details><summary>Раберем работу метода onUpdateReceived() класса TelegramSubscriptionServiceAgent:</summary>

```Kotlin
    override fun onUpdateReceived(update: Update) {
        if (update.hasMessage()) { //проверяем есть ли сообщение вообще, и если есть то попытаемся далее в теле if понять что это за команда
            val message = update.message //получаем текстовое сообщение
            val chatId = message.chatId //из сообщения получаем chatId

            val responseText: String =
                if (message.hasText()) { //проверяем есть ли текст в сообщении, если текст найдем то пытаемся понять что это за команда
                    val messageText = message.text // получили текст сообщения

                    //определяем что это за команда отдельным методом
                    resolveCommand(messageText).let { command ->
                        if (command != null && command in commands)
                            commands[command]?.processMessage(extractMessage(messageText, command), chatId) //тут отделяем название команды от текста
                                ?: "Комманда $command не зарегистрирована"
                        else "Команда из $messageText не распознана"
                    }
                } else {
                    "Введите текстовую команду"
                }
            sendNotification(chatId, responseText)
        }
    }
```
В методе сначала проверяем есть ли сообщение вызвав метод hasText() и если есть то пытаемся понять что это за команда.
Для этого получаем само сообщение, вызвав метод getMessage() - message по Kotlin. Достаем из этого сообщения chatId.
Далее проверяем есть ли вообще текст в сообщении. Если текста нет, то говорим пользователю, чтобы он его ввел.
Если текст найден, то пытаемся определить, что это за команда с помощью вспомогательного метода resolveCommand, который
принимает в параметр команду в виде строки текста. Далее если нами найденная команда не null, и данная команда есть в списке команд,
то отделяем сообщение от команды с помощью вспомогательного метода extractMessage(). И далее полученный текст сообщения
помещается в метод processMessage() соответствующего класса коммандера. Метод processMessage() вызывается на объекте, который является
значением списка команд (список команд определен вначале класса TelegramSubscriptionServiceAgent как Map, ключ которой является название бина сервиса команды, а значение
сам класс сервиса команды). Наши сервисы-классы команд мы пометили именами в параметре аннотации @Service. Спринг заинжектит
ассоциативный массив ключ которого будет имя команды, а значение - собственно сам клас-сервис команды, реализующий интерфейс
BotCommandProcessor. Если все таки команда или текст сообщения не определяется, то мы об этом оповещаем пользователя сообщением.
С помощью реализованного вспомогательного метода sendNotification() мы отправляем команду в чат бота.
</details>

<details><summary>Рассмотрим вспомогательный метод extractMessage(), который отделяет сообщение от команды:</summary>

```Kotlin
   private fun extractMessage(messageText: String, command: String): String =
   messageText.substring(command.length + 1).trim()
```
Данный метод берет текст сообщения пользователя который состоит из команды и логина и пароля, обрезает этот текст на длину
команды плюс 1 и удаляет пробелы. Т.о. мы получаем текст сообщения.

</details>

<details><summary>Теперь рассмотрим вспомогательный метод resolveCommand(), который определяет какую команду ввел пользователь:</summary>

```Kotlin
    private fun resolveCommand(text: String): String? =
        //Проверяем если команда начинается со / то берем текст до первого пробела
        if (text.startsWith("/")) text.substringBefore(' ').substring(1) else null
```
Внутри метода введенный текст пользователем сначала проверяется, то что он начинается со знака "/".
Все команды в телеграмме начинаются со слэша. И если это так, то обрезаем текст до первого пробела
и если не получается обрезать то вернем null.
</details>


Реализован слушатель Кафки, который считывает из топика записи событий и отправляет их соответсвующему пользователю в телеграмм боте,
если пользователь имеет подписку на данный телеграмм бот.

<details><summary>Для этого реализован класс-сервис AccountEventKafkaListener:</summary>

```Kotlin
@Service
class AccountEventKafkaListener(
    val subscription: SubscriptionService,
    val agent: TelegramSubscriptionServiceAgent
) { //описываем зависимости для кафки слушателя
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    private val mapper = jacksonObjectMapper() //тут получаем мэппер для десериализации данных из топика Кафки


    //Метод обработки события полученного из Kafka
    @KafkaListener(topics = ["account-events"]) //указали что данным методом мы читаем из топика кафки
    fun consumerEvent(record: ConsumerRecord<Long, String>) {
        //получаем ключ и значение записи
        val key = record.key()
        val value = record.value()

        logger.info("consume message $value for account: $key")

        //десериализовываем  объект entity - AccountEvent из Json в объект java
        val event: AccountEvent = try { //получаем само событие
            mapper.readValue(value)
        } catch (e: Exception) {
            logger.error(e.message, e)
            throw e
        }
            /*
            Тут мы проверяем есть ли у пользователя с определенным userId подписка на чат,
             если есть то мы получаем его chatId. Далее по данному chatId
             в зависимости от операции мы форматируем сообщение и отправляем это сообщение в телеграмм агент
             */
        val chatId = subscription.getSubscription(event.userId)
        if (chatId != null) {
            val message = when (event.operation) {
                Operation.PUT -> formatPutEvt(event)
                Operation.EXCHANGE -> formatExchangeEvt(event)
            }
            agent.sendNotification(chatId, message) //отправляем сообщение в телеграмм
        }
        logger.info("consumed event: $event")
    }


    //Вспомогательная функция форматирования и получения сообщения, когда операция PUT
    private fun formatPutEvt(event: AccountEvent): String =
        "Счет № ${event.accountId}. Дата: ${event.created}.\n" +
                "Операция ${event.operation} на сумму ${event.amount} ${event.currencyCode}"


    //Вспомогательная функция форматирования и получения сообщения, когда операция EXCHANGE
    private fun formatExchangeEvt(event: AccountEvent): String =
        "Счет № ${event.accountId}. Дата: ${event.created}.\n" +
                "Операция ${event.operation} на сумму ${event.amount} ${event.currencyCode} \n" +
                "со счета ${event.fromAccount}"
}
```
В данном классе мы десериализовываем событие в Java класс. Для этого используется класс модель [AccountEvent.kt](notification-bot%2Fsrc%2Fmain%2Fkotlin%2Fio%2FkamenskiyAndrey%2FcurrencyExchanger%2Fnotification%2Fmodel%2FAccountEvent.kt),
описывающий JSON который лежит в топике.
В классе AccountEventKafkaListener инжектятся бины SubscriptionService и TelegramSubscriptionServiceAgent.
В классе импортирован парсер 'private val mapper = jacksonObjectMapper()'.
Итак сначала мы десериализовываем JSON в объект Java, далее подставляем из объекта его UserId в метод getSubscription(),
который вызывается на бине сервиса подписок SubscriptionService, в результате получаем chatId. Метод возвращает chatId пользователя.
Если этот chatId не null, то в зависимости от операции (PUT или EXCHANGE) мы форматируем сообщение и отправляем его с помощью метода
sendNotification() бина TelegramSubscriptionServiceAgent в наш телеграмм бот.
</details>


<details><summary>Демонстрация работы телеграмм-бота:</summary>

1. Задали параметр окружения, в котором указали адрес на файл с токеном:

![2. назначение переменной окружения.png](https://github.com/AndreyJavaEdu/microservices-currency-exchanger/blob/readme-file/%D0%A1%D1%85%D0%B5%D0%BC%D1%8B%20%D0%B4%D0%BB%D1%8F%20README/Notification/%D0%B4%D0%B5%D0%BC%D0%BE%D0%BD%D1%81%D1%82%D1%80%D0%B0%D1%86%D0%B8%D1%8F/2.%20%D0%BD%D0%B0%D0%B7%D0%BD%D0%B0%D1%87%D0%B5%D0%BD%D0%B8%D0%B5%20%D0%BF%D0%B5%D1%80%D0%B5%D0%BC%D0%B5%D0%BD%D0%BD%D0%BE%D0%B9%20%D0%BE%D0%BA%D1%80%D1%83%D0%B6%D0%B5%D0%BD%D0%B8%D1%8F.png)

2. Запускаем все необходимые микросервисы

![1. Запустили микросервисы в Idea.png](https://github.com/AndreyJavaEdu/microservices-currency-exchanger/blob/readme-file/%D0%A1%D1%85%D0%B5%D0%BC%D1%8B%20%D0%B4%D0%BB%D1%8F%20README/Notification/%D0%B4%D0%B5%D0%BC%D0%BE%D0%BD%D1%81%D1%82%D1%80%D0%B0%D1%86%D0%B8%D1%8F/1.%20%D0%97%D0%B0%D0%BF%D1%83%D1%81%D1%82%D0%B8%D0%BB%D0%B8%20%D0%BC%D0%B8%D0%BA%D1%80%D0%BE%D1%81%D0%B5%D1%80%D0%B2%D0%B8%D1%81%D1%8B%20%D0%B2%20Idea.png)

3. Отправляем команду /subscribe Misha:1122 данному телеграмм боту, в отпвет получаем сообщение, что подписка оформлена:

![3. Пописка пользователя Misha.png](https://github.com/AndreyJavaEdu/microservices-currency-exchanger/blob/readme-file/%D0%A1%D1%85%D0%B5%D0%BC%D1%8B%20%D0%B4%D0%BB%D1%8F%20README/Notification/%D0%B4%D0%B5%D0%BC%D0%BE%D0%BD%D1%81%D1%82%D1%80%D0%B0%D1%86%D0%B8%D1%8F/3.%20%D0%9F%D0%BE%D0%BF%D0%B8%D1%81%D0%BA%D0%B0%20%D0%BF%D0%BE%D0%BB%D1%8C%D0%B7%D0%BE%D0%B2%D0%B0%D1%82%D0%B5%D0%BB%D1%8F%20Misha.png)

4. Теперь пробуем через Web-клиент (в нашем случае Postman) пополнить счет c id=1 на 1000 и переведем 1000 с 1 счета на 2 счет:

![4(1). Пополнение счета клиента.png](https://github.com/AndreyJavaEdu/microservices-currency-exchanger/blob/readme-file/%D0%A1%D1%85%D0%B5%D0%BC%D1%8B%20%D0%B4%D0%BB%D1%8F%20README/Notification/%D0%B4%D0%B5%D0%BC%D0%BE%D0%BD%D1%81%D1%82%D1%80%D0%B0%D1%86%D0%B8%D1%8F/4(1).%20%D0%9F%D0%BE%D0%BF%D0%BE%D0%BB%D0%BD%D0%B5%D0%BD%D0%B8%D0%B5%20%D1%81%D1%87%D0%B5%D1%82%D0%B0%20%D0%BA%D0%BB%D0%B8%D0%B5%D0%BD%D1%82%D0%B0.png)

![4(2) Перевод с 1 счета на 2 счет 1000.png](https://github.com/AndreyJavaEdu/microservices-currency-exchanger/blob/readme-file/%D0%A1%D1%85%D0%B5%D0%BC%D1%8B%20%D0%B4%D0%BB%D1%8F%20README/Notification/%D0%B4%D0%B5%D0%BC%D0%BE%D0%BD%D1%81%D1%82%D1%80%D0%B0%D1%86%D0%B8%D1%8F/4(2)%20%D0%9F%D0%B5%D1%80%D0%B5%D0%B2%D0%BE%D0%B4%20%D1%81%201%20%D1%81%D1%87%D0%B5%D1%82%D0%B0%20%D0%BD%D0%B0%202%20%D1%81%D1%87%D0%B5%D1%82%201000.png)

![4(3) Демонстрация оповещения по операциям.png](https://github.com/AndreyJavaEdu/microservices-currency-exchanger/blob/readme-file/%D0%A1%D1%85%D0%B5%D0%BC%D1%8B%20%D0%B4%D0%BB%D1%8F%20README/Notification/%D0%B4%D0%B5%D0%BC%D0%BE%D0%BD%D1%81%D1%82%D1%80%D0%B0%D1%86%D0%B8%D1%8F/4(3)%20%D0%94%D0%B5%D0%BC%D0%BE%D0%BD%D1%81%D1%82%D1%80%D0%B0%D1%86%D0%B8%D1%8F%20%D0%BE%D0%BF%D0%BE%D0%B2%D0%B5%D1%89%D0%B5%D0%BD%D0%B8%D1%8F%20%D0%BF%D0%BE%20%D0%BE%D0%BF%D0%B5%D1%80%D0%B0%D1%86%D0%B8%D1%8F%D0%BC.png)

Итак телеграмм бот оповещает подписанного пользователя об операциях на его счетах.

5. Теперь попробуем отменить подписку пользователя на оповещение телеграмм ботом, командой /unsubscribe:

![5. Отмена подписки пользователя.png](https://github.com/AndreyJavaEdu/microservices-currency-exchanger/blob/readme-file/%D0%A1%D1%85%D0%B5%D0%BC%D1%8B%20%D0%B4%D0%BB%D1%8F%20README/Notification/%D0%B4%D0%B5%D0%BC%D0%BE%D0%BD%D1%81%D1%82%D1%80%D0%B0%D1%86%D0%B8%D1%8F/5.%20%D0%9E%D1%82%D0%BC%D0%B5%D0%BD%D0%B0%20%D0%BF%D0%BE%D0%B4%D0%BF%D0%B8%D1%81%D0%BA%D0%B8%20%D0%BF%D0%BE%D0%BB%D1%8C%D0%B7%D0%BE%D0%B2%D0%B0%D1%82%D0%B5%D0%BB%D1%8F.png)
</details>






