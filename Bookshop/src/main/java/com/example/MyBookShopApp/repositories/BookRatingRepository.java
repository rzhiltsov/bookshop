package com.example.MyBookShopApp.repositories;

import com.example.MyBookShopApp.entities.book.rating.BookRatingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRatingRepository extends JpaRepository<BookRatingEntity, Integer> {
}
