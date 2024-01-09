package com.kamenskiyandrey.identityservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
Класс для тела метода addNewUser(AddNewUserDTO dto) в контроллере
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddNewUserDTO {
    @JsonProperty(value = "name")
    private String userName;

    @JsonProperty(value = "email")
    private String email;

    @JsonProperty(value = "password")
    private String password;
}
