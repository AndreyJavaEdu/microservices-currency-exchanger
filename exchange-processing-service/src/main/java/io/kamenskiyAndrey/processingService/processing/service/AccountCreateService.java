package io.kamenskiyAndrey.processingService.processing.service;

import io.kamenskiyAndrey.processingService.processing.domainModel.AccountEntity;
import io.kamenskiyAndrey.processingService.processing.dto.NewAccountDTO;
import io.kamenskiyAndrey.processingService.processing.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountCreateService {

    private final AccountRepository repository;

    //Метод создания счета
    @Transactional
    public AccountEntity createNewAccount(NewAccountDTO dto) {
        //заполняем Entity объект данными из DTO
        var account = new AccountEntity();
        account.setCurrencyCode(dto.getCurrencyCode());
        account.setUserId(dto.getUserId());
        account.setBalance(new BigDecimal(0));

        //Сохраняем объект в базу
        var entityAccountObjectInBase = repository.save(account);
        return entityAccountObjectInBase;
    }

    //Метод пополнения счета
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ)
    public AccountEntity addMoneyToAccount(String uid, Long accountId, BigDecimal money) {
        Optional<AccountEntity> accEntity = repository.findById(accountId);
        AccountEntity account = accEntity.orElseThrow(() -> new IllegalArgumentException("Account with" +
                " id = " + accountId + " is not found"));
        BigDecimal addMoneyToAccount = account.getBalance().add(money);
        account.setBalance(addMoneyToAccount);

// Второй способ реализации метода
//        var optionalBalance = accEntity.map(acc -> {
//            var balance = acc.getBalance().add(money);
//            acc.setBalance(balance);
//            return repository.save(acc);
//        });
//        AccountEntity account = optionalBalance.orElseThrow(() -> new IllegalArgumentException("Account with" +
//                " id = " + accountId + " is not found"));
        return repository.save(account);
    }

    //Метод получения счета по идентификатору
    @Transactional
    public AccountEntity getAccountById(Long accountId){
        AccountEntity account = repository.findById(accountId).orElseThrow(() -> new IllegalArgumentException("Account with" +
                " id = " + accountId + " is not found"));
        return account;
    }
}
