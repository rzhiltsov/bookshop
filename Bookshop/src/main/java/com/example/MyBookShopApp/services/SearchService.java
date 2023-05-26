package com.example.MyBookShopApp.services;

import com.example.MyBookShopApp.dto.Book;
import com.example.MyBookShopApp.entities.author.AuthorEntity;
import com.example.MyBookShopApp.entities.book.BookEntity;
import com.example.MyBookShopApp.repositories.AuthorRepository;
import com.example.MyBookShopApp.repositories.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;

    @Autowired
    public SearchService(BookRepository bookRepository, AuthorRepository authorRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
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

    public List<Book> getFoundBooks(String query, int offset, int limit) {
        PageRequest pageRequest = PageRequest.of(offset, limit);
        return bookRepository.findBookEntitiesByPattern(query, pageRequest).stream().map(this::createBook).toList();
    }

    public String getResultLabel(String query) {
        if (query == null) {
            return "Поисковый запрос не задан";
        }
        int booksCount = bookRepository.findBookEntitiesByPattern(query, Pageable.unpaged()).size();
        if (booksCount == 0) {
            return "По вашему запросу книги не найдены";
        } else if (booksCount % 10 == 1 && booksCount % 100 != 11) {
            return "Найдена " + booksCount + " книга";
        } else if (booksCount % 10 == 2 || booksCount % 10 == 3 || booksCount % 10 == 4) {
            return "Найдено " + booksCount + " книги";
        } else return "Найдено " + booksCount + " книг";
    }
}
