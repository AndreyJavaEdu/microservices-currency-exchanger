package io.kamenskiyAndrey.currencyExchanger.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

/*
Класс фильтра запросов - создаем кастомный
 пользовательский фильтр запросов AuthenticationFilter
 */
@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    public AuthenticationFilter() {
        super(Config.class);
    }

    //
    @Override
    public GatewayFilter apply(Config config) {
        //Здесь пишем логику фильтра
        return ((exchange, chain) ->{
/*тут пишем логику проверки есть Хедер в запросе или нет. Если запрос содержит Хедер, то мы должны делать код валидации
токена но перед этим мы должны показать для каких эндпоинтов мы будем проводить валидацию. Для этого создадим класс валидации
RouteValidator.
 */

            return chain.filter(exchange);
        });
    }


    public static class Config{

    }

}
