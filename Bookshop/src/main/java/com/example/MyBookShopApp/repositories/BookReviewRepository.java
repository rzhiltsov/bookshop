package com.example.MyBookShopApp.repositories;

import com.example.MyBookShopApp.entities.book.review.BookReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookReviewRepository extends JpaRepository<BookReviewEntity, Integer> {
}
