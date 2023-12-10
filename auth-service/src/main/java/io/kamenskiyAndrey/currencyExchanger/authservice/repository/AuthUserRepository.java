package io.kamenskiyAndrey.currencyExchanger.authservice.repository;

import io.kamenskiyAndrey.currencyExchanger.authservice.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthUserRepository extends JpaRepository<UserEntity, Integer> {

}
