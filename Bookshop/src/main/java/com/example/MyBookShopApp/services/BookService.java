package com.example.MyBookShopApp.services;

import com.example.MyBookShopApp.dto.Book;
import com.example.MyBookShopApp.entities.author.AuthorEntity;
import com.example.MyBookShopApp.entities.book.BookEntity;
import com.example.MyBookShopApp.entities.book.links.Book2UserEntity;
import com.example.MyBookShopApp.entities.book.rating.BookRatingEntity;
import com.example.MyBookShopApp.entities.genre.GenreEntity;
import com.example.MyBookShopApp.entities.user.UserEntity;
import com.example.MyBookShopApp.repositories.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final GenreRepository genreRepository;
    private final UserRepository userRepository;
    private final Book2UserRepository book2UserRepository;
    private final HttpServletRequest request;

    @Autowired
    public BookService(BookRepository bookRepository, AuthorRepository authorRepository, GenreRepository genreRepository,
                       UserRepository userRepository, Book2UserRepository book2UserRepository, HttpServletRequest request) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.genreRepository = genreRepository;
        this.userRepository = userRepository;
        this.book2UserRepository = book2UserRepository;
        this.request = request;
    }

    public Book createBook(BookEntity bookEntity) {
        if (bookEntity == null) return null;
        Book book = new Book();
        book.setId(bookEntity.getId());
        book.setDescription(bookEntity.getDescription());
        book.setImage(bookEntity.getImage());
        book.setPrice(bookEntity.getPrice());
        book.setSlug(bookEntity.getSlug());
        book.setBestseller(bookEntity.isBestseller());
        book.setTitle(bookEntity.getTitle());
        book.setDiscount(bookEntity.getDiscount());
        book.setRating((int) Math.round(bookEntity.getRatings().stream().mapToInt(BookRatingEntity::getValue).average().orElse(0)));
        int discountPrice = Math.round(bookEntity.getPrice() * (float) (100 - bookEntity.getDiscount()) / 100);
        book.setDiscountPrice(discountPrice);
        if (SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken) {
            Map<String, List<String>> data = (LinkedHashMap) request.getAttribute("data");
            if (data.getOrDefault("CART", List.of()).stream().anyMatch(bookEntity.getSlug()::equals)) {
                book.setStatus("CART");
            } else if (data.getOrDefault("KEPT", List.of()).stream().anyMatch(bookEntity.getSlug()::equals)) {
                book.setStatus("KEPT");
            } else {
                book.setStatus("");
            }
        } else {
            String userHash = SecurityContextHolder.getContext().getAuthentication().getName();
            UserEntity userEntity = userRepository.findUserEntityByHash(userHash);
            Book2UserEntity book2UserEntity = book2UserRepository.findBook2UserEntityByBookIdAndUserId(bookEntity.getId(), userEntity.getId());
            book.setStatus(book2UserEntity != null ? book2UserEntity.getType().getName() : "");
        }
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
        PageRequest pageRequest = PageRequest.of(offset, limit);
        return bookRepository.findBookEntitiesOrderByPopularity(pageRequest).stream().map(this::createBook).toList();
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

    public BookEntity getBookEntityBySlug(String slug) {
        return bookRepository.findBookEntityBySlug(slug);
    }

    public List<BookEntity> getBookEntitiesByIds(Iterable<Integer> bookIds) {
        return bookRepository.findAllById(bookIds);
    }

    public String getBookInfo(BookEntity bookEntity) {
        String authors = authorRepository.findAuthorEntitiesByBookIdOrdered(bookEntity.getId()).stream().map(AuthorEntity::getName).collect(Collectors.joining(", "));
        return "title: " + bookEntity.getTitle() + "\n" +
                "authors: " + authors + "\n" +
                "description: " + bookEntity.getDescription() + "\n" +
                "rating: " + bookEntity.getRatings().stream().mapToInt(BookRatingEntity::getValue).average().orElse(0) + "\n" +
                "image: " + bookEntity.getImage() + "\n" +
                "slug: " + bookEntity.getSlug() + "\n" +
                "publication date: " + bookEntity.getPubDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) + "\n" +
                "price: " + bookEntity.getPrice() + " рублей" + "\n" +
                "discount: " + bookEntity.getDiscount() + "%" + "\n" +
                "discount price: " + Math.round(bookEntity.getPrice() * (float) (100 - bookEntity.getDiscount()) / 100) + " рублей";
    }


}
