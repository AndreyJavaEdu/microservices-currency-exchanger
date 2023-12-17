package io.kamenskiyAndrey.processingService.processing.service;

import io.kamenskiyAndrey.processingService.processing.model.AccountEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

//Слушатель транзакции
@Service
@RequiredArgsConstructor
public class AccountOperationEventListener {

    private final AccountEventSendingService sendingService;

    // Метод по обработке события по завершению транзакции (после коммита транзакции данный метод будет вызван)
    @TransactionalEventListener
    public void handleEvent(AccountEvent event) {
        sendingService.sendEvent(event);
    }

}
