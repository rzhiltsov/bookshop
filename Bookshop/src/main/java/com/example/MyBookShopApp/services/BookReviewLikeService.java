package com.example.MyBookShopApp.services;

import com.example.MyBookShopApp.entities.book.review.BookReviewLikeEntity;
import com.example.MyBookShopApp.repositories.BookReviewLikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookReviewLikeService {

    private final BookReviewLikeRepository bookReviewLikeRepository;

    @Autowired
    public BookReviewLikeService(BookReviewLikeRepository bookReviewLikeRepository) {
        this.bookReviewLikeRepository = bookReviewLikeRepository;
    }

    public void addReviewLike(BookReviewLikeEntity bookReviewLikeEntity) {
        bookReviewLikeRepository.save(bookReviewLikeEntity);
    }

    public void deleteReviewLike(BookReviewLikeEntity bookReviewLikeEntity) {
        bookReviewLikeRepository.delete(bookReviewLikeEntity);
    }
}
