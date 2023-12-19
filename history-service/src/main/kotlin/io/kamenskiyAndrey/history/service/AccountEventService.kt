package io.kamenskiyAndrey.history.service

import io.kamenskiyAndrey.history.repository.AccountEventRepository
import org.springframework.stereotype.Service

@Service
class AccountEventService(private val repository: AccountEventRepository) { //инжектим бин репозитория

    //Метод выборки из БД по условию идентификатора счета и пользователя
}