package io.kamenskiyAndrey.currencyExchanger.notification.util


import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import io.kamenskiyAndrey.currencyExchanger.notification.service.command.SubscribeCommand
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.security.Key
@Component
class UtilJwtGettingPayload {
    val logger: Logger = LoggerFactory.getLogger(UtilJwtGettingPayload::class.java)


    val SECRET: String =
        "087F23A66DEE433829D5EB642EA91A0D7C2AE1E0CF1F30B2041EAB3465898840" //секретный 32 битный ключ от Зеона сгенерированный Морфеусом =))

    // Метод получения секретного ключа после декодирования - ключ подписи токена
    private fun getSignKey(): Key {
        val keyBytes = Decoders.BASE64.decode(SECRET) //декодируем строку, которая закодирована в BASE64
        return Keys.hmacShaKeyFor(keyBytes) // получаем секретный ключ
    }

    //Метод получения userId
    fun extractUserId(token: String): Integer {
        return extractAllClaims(token).get("userId", Integer::class.java)
    }


    //Метод получения тела полезной нагрузки токена
    private fun extractAllClaims(token: String): Claims {
        val claims = Jwts
            .parserBuilder()
            .setSigningKey(getSignKey())
            .build()
            .parseClaimsJws(token)
            .body
        logger.info("Тело токена - {}", claims)
        return claims
    }
}