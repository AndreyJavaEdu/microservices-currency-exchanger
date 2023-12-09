package io.kamenskiyAndrey.processingService.processing.repository;

import io.kamenskiyAndrey.processingService.processing.domainModel.AccountEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends CrudRepository<AccountEntity, Long> {
    List<AccountEntity>findAllByUserId(Long UserId);
}
