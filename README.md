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

Техлогии и библиотеки: Lombok, Spring WEB, зависимость com.google.guava (кеш).

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







