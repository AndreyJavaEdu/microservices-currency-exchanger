package com.kamenskiyandrey.identityservice.config;

import com.kamenskiyandrey.identityservice.entity.UserCredential;
import com.kamenskiyandrey.identityservice.repository.UserCredentialRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;

/*
Кастомный класс, который загружает данные о пользователе по имени используя метод интерфейса UserDetailsService -
loadUserByUsername.
 */
@Data
@RequiredArgsConstructor
@Component
public class CustomUserDetailService implements UserDetailsService {
    private final UserCredentialRepository repository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
       Optional<UserCredential> credential = repository.findByName(username); //Создали метод который ищет пользователя по имени в БД используя репозиторий
        //Необходимо преобразовать объект типа UserCredential к объекту типа UserDetails и далее вернуть его в данном методе.
        //Преобразование в объект CustomUserDetails делаем с помощью вызова конструктора в классе CustomUserDetails
        //и проверяем существует ли такой объект с помощью orElseThrow.
        return credential.map(CustomUserDetails::new).orElseThrow(() -> new UsernameNotFoundException("User not found with name" + username));
    }
}
