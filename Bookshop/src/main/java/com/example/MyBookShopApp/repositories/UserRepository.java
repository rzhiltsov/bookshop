package com.example.MyBookShopApp.repositories;

import com.example.MyBookShopApp.entities.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {

    UserEntity findUserEntityByHash(String hash);
}
