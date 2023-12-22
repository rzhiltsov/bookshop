package com.example.MyBookShopApp.entities.genre;

import com.example.MyBookShopApp.entities.book.BookEntity;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "genre")
public class GenreEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "parent_id", columnDefinition = "INT")
    private GenreEntity parent;

    @Column(columnDefinition = "VARCHAR(255) NOT NULL", unique = true)
    private String slug;

    @Column(columnDefinition = "VARCHAR(255) NOT NULL")
    private String name;

    @OneToMany(mappedBy = "parent")
    private List<GenreEntity> children;

    @ManyToMany
    @JoinTable(name = "book2genre",
            joinColumns = @JoinColumn(name = "genre_id"),
            inverseJoinColumns = @JoinColumn(name = "book_id"))
    private List<BookEntity> books;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public GenreEntity getParent() {
        return parent;
    }

    public void setParent(GenreEntity parent) {
        this.parent = parent;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<BookEntity> getBooks() {
        return books;
    }

    public void setBooks(List<BookEntity> books) {
        this.books = books;
    }

    public List<GenreEntity> getChildren() {
        return children;
    }

    public void setChildren(List<GenreEntity> children) {
        this.children = children;
    }
}
