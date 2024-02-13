package io.kamenskiyAndrey.currencyExchanger.gateway.filter;


import io.kamenskiyAndrey.currencyExchanger.gateway.util.JWTUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/*
Класс фильтра запросов - создали кастомный
 пользовательский фильтр запросов AuthenticationFilter
 согласно паттерну
 */
@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {
    Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);


    @Autowired
    private RouteValidator validator;
    @Autowired
    private RestTemplate template;
    @Autowired
    private JWTUtil jwtUtil;

    public AuthenticationFilter() {
        super(Config.class);
    }

    //
    @Override
    public GatewayFilter apply(Config config) {
        //Здесь пишем логику фильтра
        return ((exchange, chain) -> {
            ServerHttpRequest request = null;
        /*тут пишем логику проверки есть ли Хедер в запросе или нет. Если запрос содержит Хедер, то мы должны делать код валидации
        токена, но перед этим мы должны показать для каких эндпоинтов мы будем проводить валидацию. Для этого создадим класс валидатор
        RouteValidator.
        */
            if (validator.isSecured.test(exchange.getRequest())) {
                //тут проверяем содержит ли Header токен или не содержит
                if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) { //проверяем есть ли Хедер авторизации в заголовке запроса
                    logger.error("Headers are not have authorization");
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
//                    String requestOfValidationToken = template.getForObject("http://authentificational-service/auth/validate?token=" + authHeader, String.class);
                    jwtUtil.validateToken(authHeader);

                    //Здесь мы меняем Хэдер запроса, который содержит Токен на информацию, а именно UserId - идентификатор пользователя
                     request = exchange.getRequest()
                            .mutate()
                            .header("UserId", String.valueOf(jwtUtil.extractUserId(authHeader)))
                            .build();


                } catch (Exception ex) {
                    logger.error("Invalid Access");
                    throw new RuntimeException("Неавторизованный запрос в приложении, токен не прошел валидацию");
                }
            }
            return chain.filter(exchange.mutate().request(request).build());
        });
    }

    public static class Config {
    }
}
