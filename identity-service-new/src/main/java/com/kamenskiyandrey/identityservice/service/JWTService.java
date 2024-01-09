package com.kamenskiyandrey.identityservice.service;

import com.kamenskiyandrey.identityservice.repository.UserCredentialRepository;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/*
Специальный севис класс для получения JWT токена и его валидации (подтверждения),
здесь в логике мы используем библиотеку JWT, зависимости которой добавлили ранее
 */
@Component
@RequiredArgsConstructor
public class JWTService {
    private final UserCredentialRepository repository;
    public final static String SECRET = "087F23A66DEE433829D5EB642EA91A0D7C2AE1E0CF1F30B2041EAB3465898840"; //секретный 32 битный ключ от Зеона сгенерированный Морфеусом =))
//Метод для валидации токена
    public void validateToken(final String token){
        JwtParser parser = Jwts.parserBuilder().setSigningKey(getSignKey()).build(); //Строим парсер с учетом заданного секретного ключа
        parser.parseClaimsJws(token); //Парcим наш токен по значению секретного ключа и проверяем (в случае несоответствия будет Exception)
    }
    //метод получения токена, который вызывает другой метод по созданию токена
    public String generateToken(String userName) {
        Map<String, Object> claims = new HashMap<>();
        Integer userId = repository.findByName(userName)
                .orElseThrow(() -> new IllegalArgumentException("Такой пользователь не зарегистрирован")).getId();
        claims.put("userId", userId);
        return createToken(claims, userName);
    }

    //Метод создания токена JWT, заполнения его payload
    private String createToken(Map<String, Object> claims, String userName) {
        //Это все переопределенные методы Jwts парсера
        return Jwts.builder()
                .setClaims(claims) // заполняем полезную нагрузку JWT
                .setSubject(userName)// установили утверждение "sub" в payload JWT
                .setIssuedAt(new Date(System.currentTimeMillis())) // установили время создания токена в payload JWT (в параметре миллисекунды)
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 30)) //установили время после которого токен нельзя использовать (30 минут)
                .signWith(getSignKey(), SignatureAlgorithm.HS256).compact();//тут токену присваивается подпись и создается сам токен JWT
    }

    // Метод получения секретного ключа после декодирования
    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET); //декодируем строку, которая закодирована в BASE64
        return Keys.hmacShaKeyFor(keyBytes); // получаем секретный ключ
    }
}
