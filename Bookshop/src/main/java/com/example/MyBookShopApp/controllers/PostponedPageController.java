package com.example.MyBookShopApp.controllers;

import com.example.MyBookShopApp.dto.Book;
import com.example.MyBookShopApp.entities.author.AuthorEntity;
import com.example.MyBookShopApp.services.AuthorService;
import com.example.MyBookShopApp.services.BookService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
public class PostponedPageController {

    private final BookService bookService;
    private final AuthorService authorService;

    @Autowired
    public PostponedPageController(BookService bookService, AuthorService authorService) {
        this.bookService = bookService;
        this.authorService = authorService;
    }

    @GetMapping("/postponed")
    public String postponedPage(Model model, HttpServletRequest request) {
        Map<String, Set<String>> cookies = Map.of();
        if (request.getCookies() != null) {
            cookies = Stream.of(request.getCookies())
                    .collect(Collectors.toMap(Cookie::getName, cookie -> {
                        if (cookie.getValue().isEmpty()) return Set.of();
                        else if (cookie.getValue().contains("/")) return Set.of(cookie.getValue().split("/"));
                        else return Set.of(cookie.getValue());
                    }));
        }
        Map<Book, String[][]> books = cookies.getOrDefault("KEPT", Set.of()).stream()
                .map(bookService::getBookEntityBySlug).filter(Objects::nonNull)
                .collect(Collectors.toMap(bookEntity -> bookService.getBookBySlug(bookEntity.getSlug()), bookEntity -> {
                    List<AuthorEntity> authorEntities = authorService.getAuthorsByBookIdOrdered(bookEntity.getId());
                    String[][] authors = new String[authorEntities.size()][3];
                    for (int i = 0; i < authors.length; i++) {
                        String separator = i < authors.length - 1 ? ", " : "";
                        authors[i] = new String[]{authorEntities.get(i).getSlug(), authorEntities.get(i).getName(), separator};
                    }
                    return authors;
                }, (v1, v2) -> v2, () -> new TreeMap<>(Comparator.comparing(Book::getTitle))));
        model.addAttribute("books", books);
        model.addAttribute("totalPrice", books.keySet().stream().mapToInt(Book::getPrice).sum());
        model.addAttribute("totalDiscountPrice", books.keySet().stream().mapToInt(Book::getDiscountPrice).sum());
        model.addAttribute("slugs", books.keySet().stream().map(Book::getSlug).collect(Collectors.joining(", ")));
        model.addAttribute("cartAmount", cookies.getOrDefault("CART", Set.of()).size());
        model.addAttribute("keptAmount", cookies.getOrDefault("KEPT", Set.of()).size());
        return "postponed";
    }
}
