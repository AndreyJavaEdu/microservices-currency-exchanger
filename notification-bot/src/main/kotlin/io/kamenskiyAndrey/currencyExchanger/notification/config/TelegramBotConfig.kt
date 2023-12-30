package io.kamenskiyAndrey.currencyExchanger.notification.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/*
Класс конфигурации, чтобы прочитать конфигурацию из application.yml
 */
@Configuration
class TelegramBotConfig(settings: BotSettings) {
    private lateinit var appContext: ApplicationContext //инжектим данный бин, чтобы мы могли прочитать файлы из внешнего ресурса

    //также подключим наши настройки которые мы указали в application.yml с помощью аннотации Value мы их достанем из файла application
    @Value("\${telegram.botName}")
    private val botName: String = ""

    @Value("\${telegram.token}")
    private val token: String = ""

    //Функция чтения файла конфигурации и получение имени и токена
    @Bean
    fun botSettings(): BotSettings{
    //реализуем чтение контента из файла с токеном
        val resource = appContext.getResource("file:$token")
        val content = if(resource.exists()) resource.file.readText()
                    else throw RuntimeException("File $token not found !!!")
        return BotSettings(botName, content) //заполняем бин именем бота и его токеном

    }
}