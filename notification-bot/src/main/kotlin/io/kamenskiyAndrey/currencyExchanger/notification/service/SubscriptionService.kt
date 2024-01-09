package io.kamenskiyAndrey.currencyExchanger.notification.service

import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

/*
Реализация механизма подписки в виде сервиса (это класс в котором храняться данные о подписке в HashMap)
 */
@Service
class SubscriptionService {
    val subscriptions = ConcurrentHashMap<Long, Long>() //В дженериках идетификатор пользователя и идентификатор chat id

    //Реализуем функцию подписки пользователя
    fun subscribeUser(userId: Long, chatId: Long) = subscriptions.put(userId, chatId)

    //Реализуем метод отписки пользователя по его chatId
    fun unSubscribeUser(chatId: Long) = subscriptions.entries.removeIf { (_, value) -> value==chatId }

    //Метод получения подписки для конкретного пользователя. (мы будем отдавать chat id для конкретного пользователя)
    fun getSubscription(userId: Long): Long? = subscriptions[userId]
}