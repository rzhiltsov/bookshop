package com.example.MyBookShopApp.repositories;

import com.example.MyBookShopApp.entities.other.FaqEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FaqRepository extends JpaRepository<FaqEntity, Integer> {
}
