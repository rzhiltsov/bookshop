package com.example.MyBookShopApp.services;

import com.example.MyBookShopApp.dto.Book;
import com.example.MyBookShopApp.entities.author.AuthorEntity;
import com.example.MyBookShopApp.entities.book.BookEntity;
import com.example.MyBookShopApp.entities.book.links.Book2UserEntity;
import com.example.MyBookShopApp.entities.user.UserEntity;
import com.example.MyBookShopApp.repositories.AuthorRepository;
import com.example.MyBookShopApp.repositories.Book2UserRepository;
import com.example.MyBookShopApp.repositories.BookRepository;
import com.example.MyBookShopApp.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SearchService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final UserRepository userRepository;
    private final Book2UserRepository book2UserRepository;
    private final HttpServletRequest request;

    @Autowired
    public SearchService(BookRepository bookRepository, AuthorRepository authorRepository, UserRepository userRepository,
                         Book2UserRepository book2UserRepository, HttpServletRequest request) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.userRepository = userRepository;
        this.book2UserRepository = book2UserRepository;
        this.request = request;
    }

    public Book createBook(BookEntity bookEntity) {
        if (bookEntity == null) return null;
        Book book = new Book();
        book.setImage(bookEntity.getImage());
        book.setPrice(bookEntity.getPrice());
        book.setSlug(bookEntity.getSlug());
        book.setBestseller(bookEntity.isBestseller());
        book.setTitle(bookEntity.getTitle());
        book.setDiscount(bookEntity.getDiscount());
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

    public List<Book> getFoundBooks(String query, int offset, int limit) {
        PageRequest pageRequest = PageRequest.of(offset, limit);
        return bookRepository.findBookEntitiesByPattern(query, pageRequest).stream().map(this::createBook).toList();
    }

    public String getResultLabel(int booksCount) {
        if (booksCount == -1) {
            return "Поисковый запрос не задан";
        }
        if (booksCount == 0) {
            return "По вашему запросу книги не найдены";
        } else if (booksCount % 10 == 1 && booksCount % 100 != 11) {
            return "Найдена " + booksCount + " книга";
        } else if (booksCount % 10 == 2 || booksCount % 10 == 3 || booksCount % 10 == 4) {
            return "Найдено " + booksCount + " книги";
        } else return "Найдено " + booksCount + " книг";
    }
}
