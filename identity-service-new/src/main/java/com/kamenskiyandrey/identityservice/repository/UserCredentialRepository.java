package com.kamenskiyandrey.identityservice.repository;

import com.kamenskiyandrey.identityservice.entity.UserCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/*
Репозиторий для работы с БД
 */
@Repository
public interface UserCredentialRepository extends JpaRepository<UserCredential, Integer> {
    Optional<UserCredential> findByName(String username); //Создали данный метод который ищет пользователя в БД но пользователь может быть ненайден, поэтому Optional
}
