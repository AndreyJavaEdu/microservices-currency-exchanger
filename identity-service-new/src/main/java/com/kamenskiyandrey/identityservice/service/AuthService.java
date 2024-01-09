package com.kamenskiyandrey.identityservice.service;

import com.kamenskiyandrey.identityservice.dto.AddNewUserDTO;
import com.kamenskiyandrey.identityservice.entity.UserCredential;
import com.kamenskiyandrey.identityservice.repository.UserCredentialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserCredentialRepository repository;
    private final PasswordEncoder passwordEncoder; //Инжектим бин кодировщика паролей.
    private final JWTService jwtService; //Инжектим бин создания токена

    //Метод сохранения пользователя - т.е. как только пользователь появится, только тогда мы сможем пройти аутентификацию
    //После мы сможем сгенерировать токен используя эти данные пользователя и правильно его аутентифицировать
    @Transactional
    public String saveUser(AddNewUserDTO dto){
        var credential = new UserCredential();
        credential.setName(dto.getUserName());
        credential.setEmail(dto.getEmail());
        credential.setPassword(passwordEncoder.encode(dto.getPassword())); //Извлекди пароль, закодировали и поместили в объект UserCredential
        repository.save(credential); //хранить пароль нужно закодированным, поэтому создадим специальный кодировщик
        return "user added to the system";
    }

    //Метод который генерирует токен для пользователя по его имени
    public String generateToken(String userName){
        return jwtService.generateToken(userName);
    }
    //Метод проверки, валидации токена - используем метод ранее созданного класса, который мы заинжектили как бин jwtService
    public void validateToken(String token){
        jwtService.validateToken(token);
    }
}
