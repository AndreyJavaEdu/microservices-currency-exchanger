package io.kamenskiyAndrey.processingService.processing.controller;


import io.kamenskiyAndrey.processingService.processing.domainModel.AccountEntity;
import io.kamenskiyAndrey.processingService.processing.dto.ExchangeMoneyDTO;
import io.kamenskiyAndrey.processingService.processing.dto.NewAccountDTO;
import io.kamenskiyAndrey.processingService.processing.dto.PutMoneyToAccountDTO;
import io.kamenskiyAndrey.processingService.processing.service.AccountCreateService;
import io.kamenskiyAndrey.processingService.processing.service.ExchangerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RequiredArgsConstructor
@RestController
@RequestMapping("/processing")
public class ProcessingAccountController {
    private final AccountCreateService service;
    private final ExchangerService exchangerService;

    @PostMapping(path = "/account")
    public AccountEntity createAccount(@RequestBody NewAccountDTO account){
        return service.createNewAccount(account);
    }

    @PutMapping(path = "/account/{id}")
    public AccountEntity putMoney(@PathVariable(value = "id") Long accountId, @RequestBody PutMoneyToAccountDTO data){
       return service.addMoneyToAccount(data.getUid(), accountId, data.getAmountOfMoney());
    }

    @PutMapping(path = "/exchange/{uid}")
    public BigDecimal exchangeCurrency (@PathVariable(value = "uid") String uid, @RequestBody ExchangeMoneyDTO data){
        return exchangerService.exchangeCurrency(uid, data.getFromAccountId(), data.getToAccountId(), data.getAmount());

    }
}



