package io.kamenskiyAndrey.processingService.processing.model;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.math.BigDecimal;
import java.util.Date;

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
    private Operation operation; // Операции - пополнение счета и перевод

    @NonNull
    private BigDecimal amount;

    @reactor.util.annotation.NonNull
    private Date created;
}
