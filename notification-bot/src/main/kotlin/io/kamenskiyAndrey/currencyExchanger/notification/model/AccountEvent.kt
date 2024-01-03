package io.kamenskiyAndrey.currencyExchanger.notification.model

import java.math.BigDecimal
import java.util.*

/*
Класс который будет представлять десириализованное событие из кафки из JSON в класс. Создаем модель
описывающую JSON, который лежит в топике кафки.
 */
data class AccountEvent(
    val uuid: String,
    val accountId: Long,
    val userId: Long,
    val fromAccount: Long?,
    val currencyCode: String,
    val operation: Operation,
    val amount: BigDecimal,
    val created: Date
    )