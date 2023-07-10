package com.example.MyBookShopApp.entities.book.rating;

import com.example.MyBookShopApp.entities.book.BookEntity;
import com.example.MyBookShopApp.entities.user.UserEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "book_rating")
public class BookRatingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(columnDefinition = "SMALLINT NOT NULL")
    private short value;

    @ManyToOne
    @JoinColumn(name = "book_id", columnDefinition = "INT NOT NULL")
    private BookEntity book;

    @ManyToOne
    @JoinColumn(name = "user_id", columnDefinition = "INT")
    private UserEntity user;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public short getValue() {
        return value;
    }

    public void setValue(short value) {
        this.value = value;
    }

    public BookEntity getBook() {
        return book;
    }

    public void setBook(BookEntity book) {
        this.book = book;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }
}
