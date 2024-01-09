package com.kamenskiyandrey.identityservice.config;

import com.kamenskiyandrey.identityservice.entity.UserCredential;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/*
Кастомный класс использует данные класса UserCredential.
Использовали конструктор чтобы поля данного класса проинициализировать из значений объекта класса UserCredential.
 */
@Data
public class CustomUserDetails implements UserDetails {

    private String userName;
    private  String password;



    public CustomUserDetails(UserCredential user) {
        this.userName = user.getName();
        this.password = user.getPassword();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return userName;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
