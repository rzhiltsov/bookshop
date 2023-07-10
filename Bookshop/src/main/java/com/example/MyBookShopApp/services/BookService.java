package com.example.MyBookShopApp.services;

import com.example.MyBookShopApp.dto.Book;
import com.example.MyBookShopApp.entities.author.AuthorEntity;
import com.example.MyBookShopApp.entities.book.BookEntity;
import com.example.MyBookShopApp.entities.book.rating.BookRatingEntity;
import com.example.MyBookShopApp.entities.genre.GenreEntity;
import com.example.MyBookShopApp.repositories.AuthorRepository;
import com.example.MyBookShopApp.repositories.BookRepository;
import com.example.MyBookShopApp.repositories.GenreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;

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
        if (bookEntity == null) return null;
        Book book = new Book();
        book.setDescription(bookEntity.getDescription());
        book.setImage(bookEntity.getImage());
        book.setPrice(bookEntity.getPrice());
        book.setSlug(bookEntity.getSlug());
        book.setBestseller(bookEntity.getIsBestseller() == 1);
        book.setTitle(bookEntity.getTitle());
        book.setDiscount(bookEntity.getDiscount());
        book.setRating((int) Math.round(bookEntity.getRatings().stream().mapToInt(BookRatingEntity::getValue).average().orElse(0)));
        int discountPrice = Math.round(bookEntity.getPrice() * (float) (100 - bookEntity.getDiscount()) / 100);
        book.setDiscountPrice(discountPrice);
        List<AuthorEntity> authors = authorRepository.findAuthorEntitiesByBookIdOrdered(bookEntity.getId());
        String authorName = authors.size() == 1 ? authors.get(0).getName() : authors.get(0).getName() + " и др.";
        book.setAuthors(authorName);
        return book;
    }

    public List<Book> getRecommendedBooks(int offset, int limit) {
        PageRequest pageRequest = PageRequest.of(offset, limit);
        return bookRepository.findBookEntitiesOrderByRating(pageRequest).stream().map(this::createBook).toList();
    }

    public List<Book> getRecentBooks(int offset, int limit) {
        return getRecentBooks(null, null, offset, limit);
    }

    public List<Book> getRecentBooks(String from, String to, int offset, int limit) {
        from = from == null || from.isEmpty() ? "-infinity" : from;
        to = to == null || to.isEmpty() ? "infinity" : to;
        PageRequest pageRequest = PageRequest.of(offset, limit);
        return bookRepository.findBookEntitiesByPubDate(from, to, pageRequest).stream().map(this::createBook).toList();
    }

    public List<Book> getPopularBooks(int offset, int limit) {
        PageRequest pageRequest = PageRequest.of(offset, limit, Sort.by("popularity", "id").descending());
        return bookRepository.findAll(pageRequest).stream().map(this::createBook).toList();
    }

    public List<Book> getBooksByTagSlug(String slug, int offset, int limit) {
        PageRequest pageRequest = PageRequest.of(offset, limit);
        return bookRepository.findBookEntitiesByTagSlug(slug, pageRequest).stream().map(this::createBook).toList();
    }

    public List<Book> getBooksByGenreSlug(String slug, int offset, int limit) {
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

    public List<Book> getBooksByAuthorSlug(String slug, int offset, int limit) {
        PageRequest pageRequest = PageRequest.of(offset, limit);
        return bookRepository.findBookEntitiesByAuthorSlug(slug, pageRequest).stream().map(this::createBook).toList();
    }

    public Book getBookBySlug(String slug) {
        return createBook(bookRepository.findBookEntityBySlug(slug));
    }

    public BookEntity getBookEntityBySlug(String slug) {
        return bookRepository.findBookEntityBySlug(slug);
    }
}
