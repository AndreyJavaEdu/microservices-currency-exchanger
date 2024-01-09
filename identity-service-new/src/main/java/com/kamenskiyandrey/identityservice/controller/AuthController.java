package com.kamenskiyandrey.identityservice.controller;

import com.kamenskiyandrey.identityservice.dto.AddNewUserDTO;
import com.kamenskiyandrey.identityservice.dto.GetTokenCredititialsDTO;
import com.kamenskiyandrey.identityservice.entity.UserCredential;
import com.kamenskiyandrey.identityservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.bind.annotation.*;

//Класс контрллер для реализации методов аутентификации
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService service;
    private final AuthenticationManager authenticationManager; //Инжектим бин AuthenticationManager

    //Теперь будем вызывать три метода сервиса аутентификации здесь в контроллере (регистрация, генерация токена, проверка токена)

    //Метод регистрации, добавления нового пользователя приложения
    @PostMapping("/registration")
    public String addNewUser(@RequestBody AddNewUserDTO user) {
        return service.saveUser(user);
    }

    //Метод получения токена пользователем
    @PostMapping("/token")
    public String getToken(@RequestBody GetTokenCredititialsDTO data) {
        Authentication authenticate = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(data.getUserName(), data.getPassword())); //Получаем объект типа Authentication характеризующий аунтефицирован пользователь или нет
        if (authenticate.isAuthenticated()) { //Если пользователь аутентифицирован, то выполним генерацию токена
            return service.generateToken(data.getUserName());
        }else {
            throw new RuntimeException("Invalid access");
        }
    }

    //Метод валидации токена
    @GetMapping("/validate")
    public String validateToken(@RequestParam(name = "token") String token) { //значение токена будет браться из строки запроса url
        service.validateToken(token);
        return "Token validation is ok";
    }
}
