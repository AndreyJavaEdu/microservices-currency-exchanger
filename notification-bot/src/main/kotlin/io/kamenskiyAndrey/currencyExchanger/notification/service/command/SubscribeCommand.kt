package io.kamenskiyAndrey.currencyExchanger.notification.service.command

import io.kamenskiyAndrey.currencyExchanger.notification.model.GetTokenCredititialsDTO
import io.kamenskiyAndrey.currencyExchanger.notification.service.ServiceToGetUserId
import io.kamenskiyAndrey.currencyExchanger.notification.service.SubscriptionService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service("subscribe")
class SubscribeCommand(
    val authService: ServiceToGetUserId,
val subscriptionService: SubscriptionService
): BotCommandProcessor {
    val logger: Logger = LoggerFactory.getLogger(SubscribeCommand::class.java)
    override fun processMessage(message: String, chatId: Long): String {
        val (user, password) = message.split(":")
            .let { array -> array[0] to array[1] } // из введеного сообщения мы получаем логин и пароль пользователя



        val credentials = GetTokenCredititialsDTO(name = user, password = password)


        logger.info("Получение объекта ДТО - {}", credentials)

        val token = authService.getToken(credentials)
        logger.info("Получыенный токен - {}", token)
        return "Для пробы"

//        TODO("userId - необходимо реализовать получение")
//        if (userId != null) {
//            subscriptionService.subscribeUser(userId, chatId)
//            return "Подписка оформлена"
//        }else return "Подписка не оформлена, т.к. данный пользователь $user не существует!!!"
    }
}