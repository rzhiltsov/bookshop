package com.example.MyBookShopApp.entities.book;

import com.example.MyBookShopApp.entities.author.AuthorEntity;
import com.example.MyBookShopApp.entities.book.rating.BookRatingEntity;
import com.example.MyBookShopApp.entities.genre.GenreEntity;
import com.example.MyBookShopApp.entities.tag.TagEntity;
import com.example.MyBookShopApp.entities.user.UserEntity;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "book")
public class BookEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "SMALLINT NOT NULL DEFAULT 0")
    private short discount;

    @Column(columnDefinition = "VARCHAR(255)")
    private String image;

    @Column(columnDefinition = "SMALLINT NOT NULL")
    private short isBestseller;

    @Column(columnDefinition = "REAL NOT NULL DEFAULT 0")
    private double popularity;

    @Column(columnDefinition = "INT NOT NULL")
    private int price;

    @Column(columnDefinition = "DATE NOT NULL")
    private LocalDate pubDate;

    @Column(columnDefinition = "VARCHAR(255) NOT NULL", unique = true)
    private String slug;

    @Column(columnDefinition = "VARCHAR(255) NOT NULL")
    private String title;

    @ManyToMany(mappedBy = "books")
    private List<AuthorEntity> authors;

    @ManyToMany(mappedBy = "books")
    private List<GenreEntity> genres;

    @ManyToMany(mappedBy = "books")
    private List<UserEntity> users;

    @ManyToMany(mappedBy = "books")
    private List<TagEntity> tags;

    @OneToMany(mappedBy = "book")
    private List<BookRatingEntity> ratings;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public short getDiscount() {
        return discount;
    }

    public void setDiscount(short discount) {
        this.discount = discount;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public short getIsBestseller() {
        return isBestseller;
    }

    public void setIsBestseller(short isBestseller) {
        this.isBestseller = isBestseller;
    }

    public double getPopularity() {
        return popularity;
    }

    public void setPopularity(double popularity) {
        this.popularity = popularity;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public LocalDate getPubDate() {
        return pubDate;
    }

    public void setPubDate(LocalDate pubDate) {
        this.pubDate = pubDate;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<AuthorEntity> getAuthors() {
        return authors;
    }

    public void setAuthors(List<AuthorEntity> authors) {
        this.authors = authors;
    }

    public List<GenreEntity> getGenres() {
        return genres;
    }

    public void setGenres(List<GenreEntity> genres) {
        this.genres = genres;
    }

    public List<UserEntity> getUsers() {
        return users;
    }

    public void setUsers(List<UserEntity> users) {
        this.users = users;
    }

    public List<TagEntity> getTags() {
        return tags;
    }

    public void setTags(List<TagEntity> tags) {
        this.tags = tags;
    }

    public List<BookRatingEntity> getRatings() {
        return ratings;
    }

    public void setRatings(List<BookRatingEntity> ratings) {
        this.ratings = ratings;
    }
}
