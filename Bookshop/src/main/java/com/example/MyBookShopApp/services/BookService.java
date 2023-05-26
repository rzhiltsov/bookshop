package com.example.MyBookShopApp.services;

import com.example.MyBookShopApp.dto.Book;
import com.example.MyBookShopApp.entities.author.AuthorEntity;
import com.example.MyBookShopApp.entities.book.BookEntity;
import com.example.MyBookShopApp.entities.genre.GenreEntity;
import com.example.MyBookShopApp.repositories.AuthorRepository;
import com.example.MyBookShopApp.repositories.BookRepository;
import com.example.MyBookShopApp.repositories.GenreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final GenreRepository genreRepository;

    @Autowired
    public BookService(BookRepository bookRepository, AuthorRepository authorRepository, GenreRepository genreRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.genreRepository = genreRepository;
    }

    private Book createBook(BookEntity bookEntity) {
        Book book = new Book();
        book.setDescription(bookEntity.getDescription());
        book.setImage(bookEntity.getImage());
        book.setPrice(bookEntity.getPrice());
        book.setSlug(bookEntity.getSlug());
        book.setIsBestseller(bookEntity.getIsBestseller() == 1);
        book.setTitle(bookEntity.getTitle());
        book.setDiscount(bookEntity.getDiscount());
        int discountPrice = Math.round(bookEntity.getPrice() * (float) (100 - bookEntity.getDiscount()) / 100);
        book.setDiscountPrice(discountPrice);
        book.setPubDate(bookEntity.getPubDate());
        List<AuthorEntity> authors = authorRepository.findAuthorEntitiesByBookId(bookEntity.getId());
        String authorName = authors.size() == 1 ? authors.get(0).getName() : authors.get(0).getName() + " и др.";
        book.setAuthors(authorName);
        return book;
    }

    public List<Book> getRecommendedBooks(int offset, int limit) {
        PageRequest pageRequest = PageRequest.of(offset, limit, Sort.by("rating").descending());
        return bookRepository.findAll(pageRequest).stream().map(this::createBook).toList();
    }

    public List<Book> getRecentBooks(int offset, int limit) {
        return getRecentBooks(null, null, offset, limit);
    }

    public List<Book> getRecentBooks(String from, String to, int offset, int limit) {
        from = from == null || from.isEmpty() ? "-infinity" : from;
        to = to == null || to.isEmpty() ? "infinity" : to;
        PageRequest pageRequest = PageRequest.of(offset, limit, Sort.by("pubDate").descending());
        return bookRepository.findBookEntitiesByPubDate(from, to, pageRequest).stream().map(this::createBook).toList();
    }

    public List<Book> getPopularBooks(int offset, int limit) {
        PageRequest pageRequest = PageRequest.of(offset, limit, Sort.by("popularity").descending());
        return bookRepository.findAll(pageRequest).stream().map(this::createBook).toList();
    }

    public List<Book> getBooksByTag(String slug, int offset, int limit) {
        PageRequest pageRequest = PageRequest.of(offset, limit);
        return bookRepository.findBookEntitiesByTagSlug(slug, pageRequest).stream().map(this::createBook).toList();
    }

    public List<Book> getBooksByGenre(String slug, int offset, int limit) {
        TreeSet<Book> books = new TreeSet<>(Comparator.comparing(Book::getTitle));
        GenreEntity genreEntity = genreRepository.findGenreEntityBySlug(slug);
        ArrayDeque<GenreEntity> children = new ArrayDeque<>();
        while (genreEntity != null) {
            books.addAll(genreEntity.getBooks().stream().map(this::createBook).toList());
            children.addAll(genreEntity.getChildren());
            genreEntity = children.poll();
        }
        return books.stream().skip((long) offset * limit).limit(limit).toList();
    }

}
