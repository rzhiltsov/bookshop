package com.example.MyBookShopApp.entities.user;

import com.example.MyBookShopApp.entities.book.BookEntity;
import com.example.MyBookShopApp.entities.book.rating.BookRatingEntity;
import com.example.MyBookShopApp.entities.book.review.BookReviewEntity;
import com.example.MyBookShopApp.entities.book.review.BookReviewLikeEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(columnDefinition = "VARCHAR(255) NOT NULL", unique = true)
    private String hash;

    @Column(columnDefinition = "TIMESTAMP NOT NULL")
    private LocalDateTime regTime;

    @Column(columnDefinition = "INT NOT NULL DEFAULT 0")
    private int balance;

    @Column(columnDefinition = "VARCHAR(255) NOT NULL")
    private String name;

    @Column(columnDefinition = "VARCHAR(255) NOT NULL")
    private String role;

    @OneToMany(mappedBy = "user")
    private List<UserContactEntity> contacts;

    @OneToMany(mappedBy = "user")
    private List<BookRatingEntity> ratings;

    @OneToMany(mappedBy = "user")
    private List<BookReviewEntity> reviews;

    @OneToMany(mappedBy = "user")
    private List<BookReviewLikeEntity> reviewLikes;

    @ManyToMany
    @JoinTable(name = "book2user",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "book_id"))
    private List<BookEntity> books;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public LocalDateTime getRegTime() {
        return regTime;
    }

    public void setRegTime(LocalDateTime regTime) {
        this.regTime = regTime;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<UserContactEntity> getContacts() {
        return contacts;
    }

    public void setContacts(List<UserContactEntity> contacts) {
        this.contacts = contacts;
    }

    public List<BookRatingEntity> getRatings() {
        return ratings;
    }

    public void setRatings(List<BookRatingEntity> ratings) {
        this.ratings = ratings;
    }

    public List<BookReviewEntity> getReviews() {
        return reviews;
    }

    public void setReviews(List<BookReviewEntity> reviews) {
        this.reviews = reviews;
    }

    public List<BookReviewLikeEntity> getReviewLikes() {
        return reviewLikes;
    }

    public void setReviewLikes(List<BookReviewLikeEntity> reviewLikes) {
        this.reviewLikes = reviewLikes;
    }

    public List<BookEntity> getBooks() {
        return books;
    }

    public void setBooks(List<BookEntity> books) {
        this.books = books;
    }
}
