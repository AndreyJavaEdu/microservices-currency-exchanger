package io.kamenskiyAndrey.history.service

import io.kamenskiyAndrey.history.model.AccountEvent
import io.kamenskiyAndrey.history.repository.AccountEventRepository
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.RequestHeader

@Service
class AccountEventService(private val repository: AccountEventRepository) { //инжектим бин репозитория

    //Метод выборки из БД по условию идентификатора счета и пользователя
    fun findAllByAccountEventsFromHistoryDB(accountId: Long,  userId: Long): List<AccountEvent>{ //Идентификатор счета получаем из контроллера из вне

       return repository.findAllByAccountIdAndUserIdOrderByCreatedDesc(accountId, userId)
    }
}