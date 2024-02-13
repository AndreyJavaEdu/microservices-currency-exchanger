package io.kamenskiyAndrey.currencyExchanger.notification.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kamenskiyAndrey.currencyExchanger.notification.model.AccountEvent
import io.kamenskiyAndrey.currencyExchanger.notification.model.Operation
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

/*
Сервис который принимает событие из Кафки
и далее отправляет сообщение в телеграм,
оповещая клиента об операции
 */
@Service
class AccountEventKafkaListener(
    val subscription: SubscriptionService,
    val agent: TelegramSubscriptionServiceAgent
) { //описываем зависимости для кафки слушателя
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    private val mapper = jacksonObjectMapper() //тут получаем мэппер для десериализации данных из топика Кафки


    //Метод обработки события полученного из Kafka
    @KafkaListener(topics = ["account-events"]) //указали что данным методом мы читаем из топика кафки
    fun consumerEvent(record: ConsumerRecord<Long, String>) {
        //получаем ключ и значение записи
        val key = record.key()
        val value = record.value()

        logger.info("consume message $value for account: $key")

        //Десериализовываем  объект entity - AccountEvent из Json в объект java
        val event: AccountEvent = try { //получаем само событие
            mapper.readValue(value)
        } catch (e: Exception) {
            logger.error(e.message, e)
            throw e
        }
            /*
            Тут мы проверяем есть ли у пользователя с определенным userId подписка на чат,
             если есть то мы получаем его chatId. Далее по данному chatId
             в зависимости от операции мы форматируем сообщение и отправляем это сообщение в телеграмм агент
             */
        val chatId = subscription.getSubscription(event.userId)
        if (chatId != null) {
            val message = when (event.operation) {
                Operation.PUT -> formatPutEvt(event)
                Operation.EXCHANGE -> formatExchangeEvt(event)
            }
            agent.sendNotification(chatId, message) //отправляем сообщение в телеграмм
        }
        logger.info("consumed event: $event")
    }


    //Вспомогательная функция форматирования и получения сообщения, когда операция PUT
    private fun formatPutEvt(event: AccountEvent): String =
        "Счет № ${event.accountId}. Дата: ${event.created}.\n" +
                "Операция ${event.operation} на сумму ${event.amount} ${event.currencyCode}"


    //Вспомогательная функция форматирования и получения сообщения, когда операция EXCHANGE
    private fun formatExchangeEvt(event: AccountEvent): String =
        "Счет № ${event.accountId}. Дата: ${event.created}.\n" +
                "Операция ${event.operation} на сумму ${event.amount} ${event.currencyCode} \n" +
                "со счета ${event.fromAccount}"
}