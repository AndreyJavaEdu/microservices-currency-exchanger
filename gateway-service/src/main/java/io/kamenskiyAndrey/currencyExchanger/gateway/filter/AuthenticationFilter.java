package io.kamenskiyAndrey.currencyExchanger.gateway.filter;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/*
Класс фильтра запросов - создали кастомный
 пользовательский фильтр запросов AuthenticationFilter
 согласно паттерну
 */
@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private RouteValidator validator;
    @Autowired
    private RestTemplate template;

    public AuthenticationFilter() {
        super(Config.class);
    }

    //
    @Override
    public GatewayFilter apply(Config config) {
        //Здесь пишем логику фильтра
        return ((exchange, chain) -> {
        /*тут пишем логику проверки есть ли Хедер в запросе или нет. Если запрос содержит Хедер, то мы должны делать код валидации
        токена, но перед этим мы должны показать для каких эндпоинтов мы будем проводить валидацию. Для этого создадим класс валидатор
        RouteValidator.
        */
            if (validator.isSecured.test(exchange.getRequest())) {
                //тут проверяем содержит ли Header токен или не содержит
                if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) { //проверяем есть ли Хедер авторизации в заголовке запроса
                    throw new RuntimeException("Headers are not have authorization");
                }
                //если условие выше не выполняется, тогда получим этот Хедер как строку
                String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0); //получили Хедер
                if (authHeader != null && authHeader.contains("Bearer ")) {
                    authHeader = authHeader.substring(7); //обрезаем Хедер и возвращаем его с 7 индекса (т.е. от Хедера отрежется слово Bearer и один пробел)
                }
                try {
                    /*Этот код не безопасен, если кто то сможет получить данные из данного REST запроса, поэтому лучше замаскировать этот токен который мы получили
                     */
                    //Делаем REST запрос с использование RestTemplate в сервис аутентификации identity-service для проверки валидности токена
                    String requestOfValidationToken = template.getForObject("http://authentificational-service/auth/validate?token=" + authHeader, String.class);
                } catch (Exception ex) {
                    System.out.println("invalid access!!!");
                    throw new RuntimeException("Неавторизованный запрос в приложении, токен не прошел валидацию");
                }
            }
            return chain.filter(exchange);
        });
    }

    public static class Config {
    }
}
