package com.example.MyBookShopApp.repositories;

import com.example.MyBookShopApp.entities.payments.BalanceTransactionEntity;
import com.example.MyBookShopApp.entities.user.UserEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<BalanceTransactionEntity, Integer> {

    public List<BalanceTransactionEntity> findBalanceTransactionEntitiesByUser(UserEntity userEntity, Pageable pageable);
}
