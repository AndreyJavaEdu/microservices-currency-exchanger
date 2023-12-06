package io.kamenskiyAndrey.processingService.processing.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class NewAccountDTO {
    @JsonAlias("currency")
    private String currencyCode;

    @JsonAlias("user")
    private Long UserId;

}
