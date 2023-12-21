package io.kamenskiyAndrey.currencyExchanger.gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClaims;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/*
Специальный севис класс для получения JWT токена и его валидации (подтверждения),
здесь в логике мы используем библиотеку JWT, зависимости которой добавлили ранее
 */
@Component
public class JWTUtil {
    public final static String SECRET = "087F23A66DEE433829D5EB642EA91A0D7C2AE1E0CF1F30B2041EAB3465898840"; //секретный 32 битный ключ от Зеона сгенерированный Морфеусом =))
//Метод для валидации токена
    public void validateToken(final String token){
        JwtParser parser = Jwts.parserBuilder().setSigningKey(getSignKey()).build(); //Строим парсер с учетом заданного секретного ключа
        parser.parseClaimsJws(token); //Парcим наш токен по значению секретного ключа и проверяем (в случае несоответствия будет Exception)
    }


    // Метод получения секретного ключа после декодирования - ключ подписи токена
    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET); //декодируем строку, которая закодирована в BASE64
        return Keys.hmacShaKeyFor(keyBytes); // получаем секретный ключ
    }




    //Метод по извлечению данных - например имени пользователя из Токена
    public String extractUserName(String token){
        return extractClaim(token, Claims::getSubject); //метод по извлечению значения в полезной нагрузке с ключем sub
    }

    //Метод получения userId
    public Integer extractUserId(String token){
        return extractAllClaims(token).get("userId", Integer.class);
    }

    //Общий метод по извлечению
    public  <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        T apply = claimsResolver.apply(claims);
        return apply;
    }
    //Метод получения тела полезной нагрузки токена
    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
