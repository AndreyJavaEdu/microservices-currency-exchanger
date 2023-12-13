package io.kamenskiyAndrey.currencyExchanger.authservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.oauth2.server.servlet.OAuth2AuthorizationServerProperties;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;


@RequiredArgsConstructor
//@EnableAuthorizationServer
//@Configuration
public class OAuthTwoServerConfiguration extends OAuth2AuthorizationServerProperties {

//    private final AuthenticationManager authenticationManager;
//
//    private final TokenStore tokenStore = new InMemoryTokenStore();
//
//    private final UserDetailsService userDetailsService;
//
//    private  final PasswordEncoder passwordEncoder;
//
//
////Здесь конфигурируем AuthenticationManager и там где будем искать пользователей
//    @Override
//    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
//        endpoints.authenticationManager(authenticationManager)
//                .tokenStore(tokenStore)
//                .userDetailsService(userDetailsService);
//    }
////Конфигурация клиентов для OAuth авторизаций
//    @Override
//    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
//        clients.inMemory()
//                .withClient("web-client")
//                .secret(passwordEncoder.encode("pin123"))
//                .authorizedGrantTypes("password")
//                .scopes("web").and()
//                .withClient("processing")
//                .secret(passwordEncoder.encode("processing123"))
//                .authorizedGrantTypes("password")
//                .scopes("system");
//
//    }




//    private final AuthenticationManager authenticationManager;
//
//    private final TokenStore tokenStore = new InMemoryTokenStore();
//
//    private final UserDetailsService userDetailsService;
//
//    private final PasswordEncoder passwordEncoder;

//    @Override
//    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
//        endpoints.authenticationManager(authenticationManager)
//                .tokenStore(tokenStore).userDetailsService(userDetailsService);
//    }
//
//    @Override
//    public void configure(AuthorizationServerSecurityConfigurer security) {
//        security.checkTokenAccess("isAuthenticated()").tokenKeyAccess("permitAll()");
//    }
//
//    @Override
//    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
//        clients.inMemory()
//                .withClient("web-client")
//                .secret(passwordEncoder.encode("pin123"))
//                .authorizedGrantTypes("password")
//                .scopes("web").and()
//                .withClient("processing")
//                .secret(passwordEncoder.encode("processing123"))
//                .authorizedGrantTypes("password").scopes("system").and()
//                .withClient("history")
//                .secret(passwordEncoder.encode("history123"))
//                .authorizedGrantTypes("authorization_code","password","refresh_token")
//                .scopes("system");

//    }
}
