package io.kamenskiyAndrey.history

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kamenskiyAndrey.history.model.AccountEvent
import io.kamenskiyAndrey.history.repository.AccountEventRepository
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

/*
Сервис, который играет роль Consumer для топика Kafka
 */
@Service
class AccountEventKafkaListener(private val repository: AccountEventRepository) {
    private val mapper = jacksonObjectMapper() //тут получаем мэппер для десериализации данных из топика Кафки


    //Метод обработки события полученного из Kafka
    @KafkaListener(topics = ["account-events"]) //указали что данным методом мы читаем из топика кафки
    fun consumerEvent(record: ConsumerRecord<Long, String>) {
        //получаем ключ и значение записи
        val key = record.key()
        val value = record.value()
        //десериализовываем  объект entity - AccountEvent
        val event: AccountEvent = try {
            mapper.readValue(value)
        }catch (e: Exception){
            throw e
        }
        //сохраняем в БД
        repository.save(event)
    }

}