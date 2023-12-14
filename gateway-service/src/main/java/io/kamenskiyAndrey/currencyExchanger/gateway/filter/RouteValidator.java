package io.kamenskiyAndrey.currencyExchanger.gateway.filter;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

/*
Класс валидатор для проверки и обхода эндпоинтов.
В этом классе мы проверяем наш запрос и об[ходим три эндпоинта которые ниже.
Т.е. мы должны позволить пользователю получить токен и отправить его в запросе в ввиде Хедера
и далее мы будем аутентифицировать этот токен в API Gateway
 */
@Component
public class RouteValidator {

    public static final List<String> openApiEndpoints = List.of(
            "/auth/registration",
            "/auth/token",
            "/eureka"
    );

    /*Ссылка на объект функционального интерфейса, у которого есть метод test.
    Объект предиката с помощью которого можно проверить запрос, содержит ли этот запрос строку
    из строк которые мы определили выше в списке openApiEndpoints
     */
    public Predicate<ServerHttpRequest> isSecured =

             request -> openApiEndpoints
                    .stream()
                    .noneMatch(uri -> request.getURI().getPath().contains(uri)); //сравниваем содержится ли строка из списка Эндпоинтов, которые указали ранее в URL запросе
}
