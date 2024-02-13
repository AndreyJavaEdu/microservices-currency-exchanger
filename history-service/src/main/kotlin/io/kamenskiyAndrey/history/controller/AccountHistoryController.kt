package io.kamenskiyAndrey.history.controller

import io.kamenskiyAndrey.history.model.AccountEvent
import io.kamenskiyAndrey.history.service.AccountEventService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/history")
class AccountHistoryController(private val historyService: AccountEventService) {
/*
В данном методе пользователь вводит свой id своего аккаунта, id самого пользователя приходит из
сервиса gate way в Хэдере запроса, с помощью @RequestHeader мы получаем id пользователя из Хэдера запроса
 */
    @GetMapping("/account/{id}")
    fun findAllOperationsInAccountHistory(@PathVariable("id") accountId: Long, @RequestHeader userId: String): List<AccountEvent>{
        val userIdFromHeader: Long = userId.toLong()
        return historyService.findAllByAccountEventsFromHistoryDB(accountId, userIdFromHeader)
    }
}