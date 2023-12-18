package io.kamenskiyAndrey.history.repository

import io.kamenskiyAndrey.history.model.AccountEvent
import io.kamenskiyAndrey.history.model.EventKey
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AccountEventRepository: JpaRepository<AccountEvent, EventKey> {
}