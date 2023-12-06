package io.kamenskiyAndrey.processingService.processing.domainModel;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ACCOUNT")
public class AccountEntity {

    @Id
    @SequenceGenerator(name = "account_generator", sequenceName = "ACCOUNT_SEQ", allocationSize = 1)
    @GeneratedValue(generator = "account_generator", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "USER_ID", nullable = false, unique = true)
    private Long userId;

    @Column(name = "CURRENCY_CODE", length = 3, nullable = false)
    private String currencyCode;

    @Column(name = "BALANCE", length = 3, nullable = false)
    private BigDecimal balance;
}
