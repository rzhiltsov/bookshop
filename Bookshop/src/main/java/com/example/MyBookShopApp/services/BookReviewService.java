package com.example.MyBookShopApp.services;

import com.example.MyBookShopApp.dto.BookReview;
import com.example.MyBookShopApp.entities.book.review.BookReviewEntity;
import com.example.MyBookShopApp.repositories.BookReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.stream.Collectors;

@Service
public class BookReviewService {

    private final BookReviewRepository bookReviewRepository;

    @Autowired
    public BookReviewService(BookReviewRepository bookReviewRepository) {
        this.bookReviewRepository = bookReviewRepository;
    }

    public BookReview createBookReview(BookReviewEntity bookReviewEntity) {
        if (bookReviewEntity == null) return null;
        BookReview bookReview = new BookReview();
        bookReview.setId(bookReviewEntity.getId());
        bookReview.setUserName("***Пользователь***");
        bookReview.setUserRating("***Оценка пользователя***");
        bookReview.setTime(bookReviewEntity.getTime());
        bookReview.setLikesCount((int) bookReviewEntity.getReviewLikes().stream()
                .filter(bookReviewLikeEntity -> bookReviewLikeEntity.getValue() > 0).count());
        bookReview.setDislikesCount((int) bookReviewEntity.getReviewLikes().stream()
                .filter(bookReviewLikeEntity -> bookReviewLikeEntity.getValue() < 0).count());
        int charLimit = 160;
        String[] words = bookReviewEntity.getText().split("\\s");
        if (bookReviewEntity.getText().length() <= charLimit) {
            bookReview.setShownText(bookReviewEntity.getText());
            bookReview.setHiddenText("");
            return bookReview;
        }
        String shownText = "";
        String hiddenText = "";
        for (int i = 0; i < words.length; i++) {
            String text = Arrays.stream(words).limit(i + 1).collect(Collectors.joining(" "));
            if (text.length() > charLimit) {
                shownText = text;
                hiddenText = Arrays.stream(words).skip(i + 1).collect(Collectors.joining(" "));
                break;
            }
        }
        bookReview.setShownText(shownText);
        bookReview.setHiddenText(hiddenText);
        return bookReview;
    }

    public void addReview(BookReviewEntity bookReviewEntity) {
        bookReviewRepository.save(bookReviewEntity);
    }

    public BookReviewEntity findBookReviewEntityById(int id) {
        return bookReviewRepository.findById(id).orElse(null);
    }
}
