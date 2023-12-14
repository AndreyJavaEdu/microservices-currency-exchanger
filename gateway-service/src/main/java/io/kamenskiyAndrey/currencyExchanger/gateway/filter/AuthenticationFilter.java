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


            return chain.filter(exchange);
        });
    }


    public static class Config{

    }

}
