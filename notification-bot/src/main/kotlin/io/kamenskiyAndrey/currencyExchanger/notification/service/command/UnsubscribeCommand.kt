package io.kamenskiyAndrey.currencyExchanger.notification.service.command

import io.kamenskiyAndrey.currencyExchanger.notification.service.SubscriptionService
import org.springframework.stereotype.Service

/*
Сервис отвечающий за отмену подписки пользователя
 */
@Service("unsubscribe")
class UnsubscribeCommand(val subscriptionService: SubscriptionService): BotCommandProcessor {
    override fun processMessage(message: String, chatId: Long): String {
        subscriptionService.unSubscribeUser(chatId)
        return "Подписка отменена"
    }
}