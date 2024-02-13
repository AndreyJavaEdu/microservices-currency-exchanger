package io.kamenskiyAndrey.processingService.processing.model;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.math.BigDecimal;
import java.util.Date;

/*
Класс-модель события для отправки сообщения в Кафку
 */
@Data
@Builder
public class AccountEvent {
    @NonNull
    private String uuid; //уникальный номер операции

    private long userId, accountId;

    private Long fromAccount; //откуда списаны деньги

    @NonNull
    private String currencyCode; //код валюты

    @NonNull
    private Operation operation; // Операции - пополнение счета и перевод (тип операции)

    @NonNull
    private BigDecimal amount;

    @NonNull
    private Date created;
}
