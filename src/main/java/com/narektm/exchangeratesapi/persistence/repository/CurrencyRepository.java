package com.narektm.exchangeratesapi.persistence.repository;

import com.narektm.exchangeratesapi.persistence.entity.CurrencyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Set;

public interface CurrencyRepository extends JpaRepository<CurrencyEntity, Long> {

    @Query("SELECT code FROM CurrencyEntity")
    Set<String> getAllCodes();

    CurrencyEntity findByCode(String code);
}
