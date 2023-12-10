package io.kamenskiyAndrey.currencyExchanger.authservice.config;

import io.kamenskiyAndrey.currencyExchanger.authservice.service.AuthUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
public class WebSecurityConfiguration   {

    private final AuthUserDetailsService service;


    public WebSecurityConfiguration(AuthUserDetailsService service) {
        this.service = service;
    }

    @Bean
    public AuthenticationConfiguration authenticationConfiguration(){
        return new AuthenticationConfiguration();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
