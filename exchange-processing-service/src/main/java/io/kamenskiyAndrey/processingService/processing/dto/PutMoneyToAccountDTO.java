package io.kamenskiyAndrey.processingService.processing.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PutMoneyToAccountDTO {
    @JsonAlias("uid")
    private String uid;

    @JsonAlias("account")
    private Long accountId;

    @JsonAlias("amount")
    private BigDecimal amountOfMoney;
}
