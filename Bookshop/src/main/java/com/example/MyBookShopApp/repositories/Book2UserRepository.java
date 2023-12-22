package com.example.MyBookShopApp.repositories;

import com.example.MyBookShopApp.entities.book.links.Book2UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Book2UserRepository extends JpaRepository<Book2UserEntity, Integer> {

    Book2UserEntity findBook2UserEntityByBookIdAndUserId(int bookId, int userId);

    List<Book2UserEntity> findBook2UserEntitiesByUserId(int userId);
}
