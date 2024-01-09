package io.kamenskiyAndrey.currencyExchanger.notification.service.command

import io.kamenskiyAndrey.currencyExchanger.notification.model.GetTokenCredentialsDTO
import io.kamenskiyAndrey.currencyExchanger.notification.service.ServiceToGetToken
import io.kamenskiyAndrey.currencyExchanger.notification.service.SubscriptionService
import io.kamenskiyAndrey.currencyExchanger.notification.util.UtilJwtGettingPayload
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service("subscribe")
class SubscribeCommand(
    val utilJwt: UtilJwtGettingPayload,
    val authService: ServiceToGetToken,
    val subscriptionService: SubscriptionService
) : BotCommandProcessor {
    val logger: Logger = LoggerFactory.getLogger(SubscribeCommand::class.java)
    override fun processMessage(message: String, chatId: Long): String {
        val (user, password) = message.split(":")
            .let { array -> array[0] to array[1] } // из введеного сообщения мы получаем логин и пароль пользователя

        val credentials = GetTokenCredentialsDTO(name = user, password = password)
        logger.info("Получение объекта ДТО - {}", credentials)

        val token = authService.getToken(credentials)
        logger.info("Получыенный токен - {}", token)
        if (token != null) {
            val userId = utilJwt.extractUserId(token)
            logger.info("Полученный userId - {}", userId)
            subscriptionService.subscribeUser(userId.toLong(), chatId)
            return "Подписка оформлена"
        } else {
            throw Exception("Токен не обнаружен, возможно не аутентифицирован пользователь")
        }
    }
}