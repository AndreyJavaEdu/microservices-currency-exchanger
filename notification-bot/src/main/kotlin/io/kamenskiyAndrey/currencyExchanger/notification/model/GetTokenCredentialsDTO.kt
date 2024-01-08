package io.kamenskiyAndrey.currencyExchanger.notification.model

import com.fasterxml.jackson.annotation.JsonAlias

data class GetTokenCredentialsDTO(
    @JsonAlias("name")
    val name: String,
    @JsonAlias("password")
    val password: String )
