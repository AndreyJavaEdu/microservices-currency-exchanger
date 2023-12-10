package io.kamenskiyAndrey.currencyExchanger.authservice.service;

import io.kamenskiyAndrey.currencyExchanger.authservice.model.UserEntity;
import io.kamenskiyAndrey.currencyExchanger.authservice.repository.AuthUserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

/*
Класс для загрузки пользователя при старте приложения
 */
@Component
@RequiredArgsConstructor
public class AuthUsersLoader implements CommandLineRunner {

    private final AuthUserRepository repository;

    //Используем данный Бин для кодирования пароля
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        //Загружаем первого пользователя
        repository.save(
                UserEntity.builder().userId(1)
                        .userName("pavel").userPassword(passwordEncoder.encode("pavel123"))
                        .build()
        );

        //Загружаем второго пользователя
        repository.save(
                UserEntity.builder().userId(2)
                        .userName("artem").userPassword(passwordEncoder.encode("artem123"))
                        .build()
        );
    }
}
