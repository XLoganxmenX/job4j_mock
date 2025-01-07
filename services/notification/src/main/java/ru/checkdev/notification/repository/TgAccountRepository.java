package ru.checkdev.notification.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import ru.checkdev.notification.domain.TgAccount;

import java.util.Optional;

public interface TgAccountRepository extends CrudRepository<TgAccount, Integer> {
    Optional<TgAccount> findByEmail(String email);

    Optional<TgAccount> findByTgUserId(int userId);

    @Modifying
    @Transactional
    @Query("delete from TgAccount ta where ta.email = :email")
    void deleteByEmail(@Param("email") String email);
}
