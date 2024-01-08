package io.kamenskiyAndrey.currencyExchanger.notification.service

import io.kamenskiyAndrey.currencyExchanger.notification.model.GetTokenCredentialsDTO
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class ServiceToGetUserId (val restTemplate: RestTemplate) {
    val logger: Logger = LoggerFactory.getLogger(ServiceToGetUserId::class.java)

    fun getToken( data: GetTokenCredentialsDTO): String? {
        try {
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON
            val requestEntity: HttpEntity<GetTokenCredentialsDTO> = HttpEntity(data, headers)
            val httpResponse = restTemplate.postForEntity("http://localhost:9797/auth/token", requestEntity, String::class.java)
            return httpResponse.body
        } catch (ex: RuntimeException) {
            logger.error("Вышла ошибка при подключении к сервису аутентификации и получению токена {}", ex.message)
        }
        return "Что то пошло не так"
    }
}