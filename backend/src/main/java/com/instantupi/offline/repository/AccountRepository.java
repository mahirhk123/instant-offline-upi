package com.instantupi.offline.repository;

import com.instantupi.offline.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, String> {

}
