package io.kamenskiyAndrey.history.service

import io.kamenskiyAndrey.history.model.AccountEvent
import io.kamenskiyAndrey.history.repository.AccountEventRepository
import org.springframework.stereotype.Service

@Service
class AccountEventService(private val repository: AccountEventRepository) { //инжектим бин репозитория

    //Метод выборки из БД по условию идентификатора счета и пользователя
    fun findAllByAccountEventsFromHistoryDB(accountId: Long): List<AccountEvent>{ //Идентификатор счета получаем из контроллера из вне
       //TODO - нужно дописать получение  userId из сервиса аутентификации
        val userId =
       return repository.findAllByAccountIdAAndUserIdOrderByCreatedDesc(accountId, userId)
    }
}