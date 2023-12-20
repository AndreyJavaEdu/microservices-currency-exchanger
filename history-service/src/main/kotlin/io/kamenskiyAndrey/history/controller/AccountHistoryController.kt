package io.kamenskiyAndrey.history.controller

import io.kamenskiyAndrey.history.model.AccountEvent
import io.kamenskiyAndrey.history.service.AccountEventService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/history")
class AccountHistoryController(private val historyService: AccountEventService) {

    @GetMapping("/account/{id}")
    fun findAllOperationsInAccountHistory(@PathVariable("id") accountId: Long): List<AccountEvent>{
        return historyService.findAllByAccountEventsFromHistoryDB(accountId)
    }
}