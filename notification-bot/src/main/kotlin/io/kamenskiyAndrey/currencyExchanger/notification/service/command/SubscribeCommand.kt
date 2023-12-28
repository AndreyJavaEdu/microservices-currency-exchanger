package io.kamenskiyAndrey.currencyExchanger.notification.service.command

import io.kamenskiyAndrey.currencyExchanger.notification.service.SubscriptionService
import org.springframework.stereotype.Service

@Service("subscribe")
class SubscribeCommand(
val subscriptionService: SubscriptionService
): BotCommandProcessor {
    override fun processMessage(message: String, chatId: Long): String {
        val (user, password) = message.split(":").let { array -> array[0] to array[1] }

        TODO("userId - необходимо реализовать получение")
        if (userId != null) {
            subscriptionService.subscribeUser(userId, chatId)
            return "Подписка оформлена"
        }else return "Подписка не оформлена, т.к. данный пользователь $user не существует!!!"

    }
}