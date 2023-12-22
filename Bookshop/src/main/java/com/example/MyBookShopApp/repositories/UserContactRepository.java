package com.example.MyBookShopApp.repositories;

import com.example.MyBookShopApp.entities.user.UserContactEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserContactRepository extends JpaRepository<UserContactEntity, Integer> {

    UserContactEntity findUserContactEntityByContact(String contact);
}
