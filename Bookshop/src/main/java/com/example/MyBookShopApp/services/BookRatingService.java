package com.example.MyBookShopApp.services;

import com.example.MyBookShopApp.entities.book.rating.BookRatingEntity;
import com.example.MyBookShopApp.repositories.BookRatingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookRatingService {

    private final BookRatingRepository bookRatingRepository;

    @Autowired
    public BookRatingService(BookRatingRepository bookRatingRepository) {
        this.bookRatingRepository = bookRatingRepository;
    }

    public BookRatingEntity addRating(BookRatingEntity bookRatingEntity) {
        return bookRatingRepository.save(bookRatingEntity);
    }
}
