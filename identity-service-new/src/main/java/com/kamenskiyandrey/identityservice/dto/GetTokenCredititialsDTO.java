package com.kamenskiyandrey.identityservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetTokenCredititialsDTO {
    @JsonProperty(value = "name")
    private String userName;
    @JsonProperty(value = "password")
    private String password;
}
