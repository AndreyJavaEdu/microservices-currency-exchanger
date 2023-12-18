package io.kamenskiyAndrey.history.model

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.util.*

@Entity
@IdClass(EventKey::class)
@Table(name = "ACCOUNT_EVENT")
data class AccountEvent(
        @Id
        @Column(name = "uuid", nullable = false)
        val uid: String,

        @Id
        @Column(name = "account_id", nullable = false)
        val accountId: Long,

        @Column(name = "user_id", nullable = false)
        val userId: Long,

        @Column(name = "from_account", nullable = true)
        val fromAccount: Long?,

        @Column(name = "currency_code", nullable = false)
        val currencyCode: String,

        @Column(name = "operation_code", nullable = false)
        val operation: Operation,

        @Column(name = "amount", nullable = false)
        val amount: BigDecimal,

        @Column(name = "date_creation_event", nullable = false)
        val dateCreationEvent: Date,
)

@Embeddable
class EventKey(
        val uid: String,
        val accountId: Long
): Serializable

