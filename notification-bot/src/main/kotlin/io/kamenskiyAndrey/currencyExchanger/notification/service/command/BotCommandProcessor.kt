package io.kamenskiyAndrey.currencyExchanger.notification.service.command
/*
Интерфейс команд - команды будут иметь похожую структуру
 */
interface BotCommandProcessor {
    fun processMessage(message: String, chatId: Long): String
}