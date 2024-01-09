package com.kamenskiyandrey.identityservice.config;

import com.kamenskiyandrey.identityservice.repository.UserCredentialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.server.SecurityWebFilterChain;

/*Это класс конфигурации в котором мы можем указать для Spring Boot какой APP мы будем
футентифицировать или какой APP может пропустить аунтентификацию
 */
@Configuration
@EnableWebSecurity
public class AuthConfig {

    private final UserCredentialRepository repository;

    @Autowired
    public AuthConfig(UserCredentialRepository repository) {
        this.repository = repository;
    }

    //Добавляем бин настройки безопасности, в нем мы настраиваем так чтобы методы контроллера не поддавались аутентификации
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.csrf().disable() //отключили защиту от CSRF атак, т.к. используем токены JWT
                .authorizeHttpRequests() //Конфигурируем цепочкой следующих методов политику авторизации (сюда входит настройка ролей)
                //тут указали правила авторизации, для определенных эндпоитов разрешили для всех пользователей без авторизации и аутентификации
                .requestMatchers("/auth/registration", "/auth/token", "/auth/validate").permitAll()
                .and()
                .build();
    }


    //Определили бин бикрипт шифровальщика паролей
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    //Определили бин который будет загружать данные о пользователях и передавать их AuthenticationManagerProvider, а он в свою очередь
    //передаст эти данные AuthenticationManager-у. В даноом бине используем созданный нами кастомный класс CustomUserDetailService, который будет
    //имплементировать интерфейс userDetailsService.
    @Bean
    public UserDetailsService userDetailsService() {
        return new CustomUserDetailService(repository);
    }

    //Определили бин провайдера AuthenticationProvider, который реализован на основе класса DaoAuthenticationProvider
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService());
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthenticationProvider;
    }

    //Определяем бин AuthenticationManager с помощью метода getAuthenticationManager класса AuthenticationConfiguration
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
