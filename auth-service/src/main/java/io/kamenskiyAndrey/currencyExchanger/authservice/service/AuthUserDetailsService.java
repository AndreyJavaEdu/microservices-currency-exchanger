package io.kamenskiyAndrey.currencyExchanger.authservice.service;

import io.kamenskiyAndrey.currencyExchanger.authservice.repository.AuthUserRepository;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthUserDetailsService implements UserDetailsService {


    private final AuthUserRepository repository;

//    public AuthUserDetailsService(AuthUserRepository repository) {
//        this.repository = repository;
//    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return repository.findByUserName(username).orElseThrow(
                () -> new UsernameNotFoundException("Username not found! Username is wrong!")
        );
    }
}
