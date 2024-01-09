package io.kamenskiyAndrey.currencyExchanger.notification.config

import io.kamenskiyAndrey.currencyExchanger.notification.service.TelegramSubscriptionServiceAgent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

/*
Класс конфигурации, чтобы прочитать конфигурацию из application.yml
 */
@Configuration
class TelegramBotConfig() {
    @Autowired
    private lateinit var appContext: ApplicationContext //инжектим данный бин, чтобы мы могли прочитать файлы из внешнего ресурса

    //также подключим наши настройки которые мы указали в application.yml с помощью аннотации Value мы их достанем из файла application
    @Value("\${telegram.botName}")
    private val botName: String = ""

    @Value("\${telegram.token}")
    private val token: String = ""

    /*Функция чтения файла конфигурации и получение имени и токена.
    Путь к файлу с токеном прописан в переменной окружения и используется при запуске сервиса.
    Сам файл с токеном телеграм бота находится в папке conf в проекте.
     */
    @Bean
    fun botSettings(): BotSettings {
        //реализуем чтение контента из файла с токеном
        val resource = appContext.getResource("file:$token")
        val content = if (resource.exists()) resource.file.readText()
        else throw RuntimeException("File $token not found !!!")
        return BotSettings(botName, content) //заполняем бин именем бота и его токеном
    }


    /*
    Данный бин необходим для обращения в сервис аутентификациии
    для получения токена пользователя, для дальнейшей расшифровки токена и получения
    userId из полезной нагрузки
     */
    @Bean
    fun restTemplate(builder: RestTemplateBuilder): RestTemplate {
        return builder.build()
    }


    /*
    Создаем бин TelegramBotsApi, в параметр бина приходит TelegramSubscriptionServiceAgent (агент телеграмм бота)
    и нам нужно его зарегистрировать в классе TelegramBotsApi.
    Бин TelegramBotsApi необходим, чтобы сказать библиотеке телеграм бота, что мы
    создали класс, который будет обрабатывать команды от нашего
    телеграмм бота - ExchangeRatesBot.
     */
    @Bean
    fun telegramBotsApi(agent: TelegramSubscriptionServiceAgent): TelegramBotsApi {
        val api = TelegramBotsApi(DefaultBotSession::class.java)
        api.registerBot(agent)
        return api
    }
}