package com.example.MyBookShopApp.services;

import com.example.MyBookShopApp.dto.BookReview;
import com.example.MyBookShopApp.entities.book.review.BookReviewEntity;
import com.example.MyBookShopApp.entities.user.UserEntity;
import com.example.MyBookShopApp.repositories.BookReviewRepository;
import com.example.MyBookShopApp.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class BookReviewService {

    private final BookReviewRepository bookReviewRepository;
    private final UserService userService;

    @Autowired
    public BookReviewService(BookReviewRepository bookReviewRepository, UserRepository userRepository, UserService userService) {
        this.bookReviewRepository = bookReviewRepository;
        this.userService = userService;
    }

    public BookReview createBookReview(BookReviewEntity bookReviewEntity) {
        if (bookReviewEntity == null) return null;
        BookReview bookReview = new BookReview();
        bookReview.setId(bookReviewEntity.getId());
        bookReview.setUserName(bookReviewEntity.getUser().getName());
        bookReview.setTime(bookReviewEntity.getTime());
        if (!(SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken)) {
            String userHash = SecurityContextHolder.getContext().getAuthentication().getName();
            UserEntity userEntity = userService.getUserEntityByHash(userHash);
            bookReviewEntity.getReviewLikes().stream().forEach(bookReviewLikeEntity -> {
                if (bookReviewLikeEntity.getUser().getId() == userEntity.getId()) {
                    if (bookReviewLikeEntity.getValue() == 1) {
                        bookReview.setLiked(true);
                    } else if (bookReviewLikeEntity.getValue() == -1) {
                        bookReview.setDisliked(true);
                    }
                }
            });
        }
        bookReview.setLikesCount((int) bookReviewEntity.getReviewLikes().stream()
                .filter(bookReviewLikeEntity -> bookReviewLikeEntity.getValue() > 0).count());
        bookReview.setDislikesCount((int) bookReviewEntity.getReviewLikes().stream()
                .filter(bookReviewLikeEntity -> bookReviewLikeEntity.getValue() < 0).count());
        int charLimit = 160;
        int splitIndex = bookReviewEntity.getText().lastIndexOf(" ", charLimit);
        if (bookReviewEntity.getText().length() <= charLimit) {
            bookReview.setShownText(bookReviewEntity.getText());
            bookReview.setHiddenText("");
        } else if (splitIndex == -1) {
            bookReview.setShownText(bookReviewEntity.getText().substring(0, charLimit));
            bookReview.setHiddenText(bookReviewEntity.getText().substring(charLimit));
        } else {
            bookReview.setShownText(bookReviewEntity.getText().substring(0, splitIndex + 1));
            bookReview.setHiddenText(bookReviewEntity.getText().substring(splitIndex + 1));
        }
        return bookReview;
    }

    public String getReviewsCountLabel(int reviewsCount) {
        if (reviewsCount == 0) {
            return "Отзывов пока нет";
        } else if (reviewsCount % 10 == 1 && reviewsCount % 100 != 11) {
            return reviewsCount + " отзыв";
        } else if (reviewsCount % 10 == 2 || reviewsCount % 10 == 3 || reviewsCount % 10 == 4) {
            return reviewsCount + " отзыва";
        } else return reviewsCount + " отзывов";
    }

    public void addReview(BookReviewEntity bookReviewEntity) {
        bookReviewRepository.save(bookReviewEntity);
    }

    public BookReviewEntity findBookReviewEntityById(int id) {
        return bookReviewRepository.findById(id).orElse(null);
    }
}
