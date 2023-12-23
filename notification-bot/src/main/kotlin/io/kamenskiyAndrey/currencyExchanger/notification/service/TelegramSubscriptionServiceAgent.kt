package io.kamenskiyAndrey.currencyExchanger.notification.service

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update

class TelegramSubscriptionServiceAgent(botToken: String?) : TelegramLongPollingBot(botToken) {




    override fun getBotUsername(): String {
        TODO("Not yet implemented")
    }

    override fun onUpdateReceived(p0: Update?) {
        TODO("Not yet implemented")
    }


}