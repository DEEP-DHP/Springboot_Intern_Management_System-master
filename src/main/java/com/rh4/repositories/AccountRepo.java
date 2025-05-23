package com.rh4.repositories;

import com.rh4.entities.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepo extends JpaRepository<Account, Long> {

    Account findByEmailId(String username);

}
