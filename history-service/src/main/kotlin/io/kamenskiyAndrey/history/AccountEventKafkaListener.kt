package io.kamenskiyAndrey.history

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kamenskiyAndrey.history.repository.AccountEventRepository
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

/*
Сервис, который играет роль Consumer для топика Kafka
 */
@Service
class AccountEventKafkaListener(private val repository: AccountEventRepository) {
    private val mapper = jacksonObjectMapper()


    //Метод обработки события полученного из Kafka
    @KafkaListener(topics = ["account-events"]) //указали что данным методом мы читаем из топика кафки
    fun consumerEvent(record: ConsumerRecord<Long, String>){

    }

}