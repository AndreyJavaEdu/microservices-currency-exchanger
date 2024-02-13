package io.kamenskiyAndrey.currencyExchanger.notification.service

import io.kamenskiyAndrey.currencyExchanger.notification.config.BotSettings
import io.kamenskiyAndrey.currencyExchanger.notification.service.command.BotCommandProcessor
import org.springframework.stereotype.Service
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update

@Service
class TelegramSubscriptionServiceAgent(
    private val settings: BotSettings,
    val commands: Map<String, BotCommandProcessor> //инжектим мапу с ключем имени сервиса и сам сервис реализуещий интефейс BotCommandProcessor
) : TelegramLongPollingBot(settings.token) {

    override fun getBotUsername(): String = settings.nameOfBot

    override fun onUpdateReceived(update: Update) {
        if (update.hasMessage()) { //проверяем есть ли сообщение вообще, и если есть то попытаемся далее в теле if понять что это за команда
            val message = update.message //получаем тектсовое сообщение
            val chatId = message.chatId //из сообщения получаем chatId

            val responseText: String =
                if (message.hasText()) { //проверяем есть ли текст в сообщении, если текст найдем то пытаемся понять что это за команда
                    val messageText = message.text // получили текст сообщения

                    //определяем что это за команда отдельным методом
                    resolveCommand(messageText).let { command ->
                        if (command != null && command in commands)
                            commands[command]?.processMessage(extractMessage(messageText, command), chatId) //тут отделяем название команды от текста
                                ?: "Комманда $command не зарегистрирована"
                        else "Команда из $messageText не распознана"
                    }
                } else {
                    "Введите текстовую команду"
                }
            sendNotification(chatId, responseText)
        }
    }
//Метод получения подстроки равной длине команды плюс 1 и без пробелов - получаем текст сообщения
    private fun extractMessage(messageText: String, command: String): String =
        messageText.substring(command.length + 1).trim()


    /*
    Метод определения команды - данная функция используется в методе onUpdateReceived выше в коде.
    Функция `resolveCommand` проверяет, начинается ли текст с символа `/`.
    Если да, то она возвращает подстроку до первого пробела (без символа `/`). Если нет, то она возвращает `null`.
     */
    private fun resolveCommand(text: String): String? =
        //Проверяем если команда начинается со / то берем текст до первого пробела
        if (text.startsWith("/")) text.substringBefore(' ').substring(1) else null



        //Метод отправки сообщений
    fun sendNotification(chatId: Long, responseText: String) {
        val responseMessage =
            SendMessage(chatId.toString(), responseText) //создали объект сообщения для отправки в чат бота
        execute(responseMessage) // данным методом отправляем сообщение
    }
}