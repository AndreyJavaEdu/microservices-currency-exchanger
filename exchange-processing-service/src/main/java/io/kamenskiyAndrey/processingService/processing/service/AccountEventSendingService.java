package io.kamenskiyAndrey.processingService.processing.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kamenskiyAndrey.processingService.processing.model.AccountEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

/*
Сервис отправки события
 */
@Service
public class AccountEventSendingService {

    public static final String ACCOUNT_EVENTS = "account-events";
    private final ObjectMapper mapper = new ObjectMapper();
    //Будем использовать данный класс для отправки события в Кафку
    private final KafkaTemplate<Long, String> kafkaTemplate;

    public AccountEventSendingService(KafkaTemplate<Long, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /*Метод отправки события в Кафку - момент, когда нам необходимо сделать вызова данного метода
    это после коммита транзакции, где мы совершаем операцию на счете, т.е. необходимо создать слушателя
    транзакции и после завершения этого слушателя мы можем отправить данные в топик в Кафку.
    В качестве слушателя будем использовать Transactional Event Listener - специальный механизм Spring.
     */
    public void sendEvent(AccountEvent event) {
        //получили id счета
        var accountId = event.getAccountId();

        //формируем сообщение в Кафку, из объекта переводим в строку типа Json
        String message;
        try {
            message = mapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        //Отправим событие в ввиде сериализованного сообщения втопик - это асинхронная операция
        var future = kafkaTemplate.send(ACCOUNT_EVENTS, accountId, message);

        //Убеждыемся, что сообщение дошло до топика и туда сохранилось
        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

    }
}
