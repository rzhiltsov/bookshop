package com.example.MyBookShopApp.controllers;

import com.example.MyBookShopApp.dto.Book;
import com.example.MyBookShopApp.entities.author.AuthorEntity;
import com.example.MyBookShopApp.entities.user.UserEntity;
import com.example.MyBookShopApp.services.AuthorService;
import com.example.MyBookShopApp.services.BookService;
import com.example.MyBookShopApp.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class CartPageController {

    private final BookService bookService;
    private final AuthorService authorService;
    private final UserService userService;

    @Autowired
    public CartPageController(BookService bookService, AuthorService authorService, UserService userService) {
        this.bookService = bookService;
        this.authorService = authorService;
        this.userService = userService;
    }

    @GetMapping("/cart")
    public String cartPage(Model model, HttpServletRequest request) {
        List<Book> bookList;
        if (SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken) {
            Map<String, List<String>> data = (LinkedHashMap) request.getAttribute("data");
            bookList = data.getOrDefault("CART", List.of()).stream().map(bookService::getBookEntityBySlug)
                    .filter(Objects::nonNull).map(bookService::createBook).toList();
        } else {
            String userHash = SecurityContextHolder.getContext().getAuthentication().getName();
            UserEntity userEntity = userService.getUserEntityByHash(userHash);
            bookList = userEntity.getBooks().stream().map(bookService::createBook)
                    .filter(book -> book.getStatus().equals("CART")).toList();
        }
        Map<Book, String[][]> bookMap = bookList.stream().collect(Collectors.toMap(Function.identity(), book -> {
            List<AuthorEntity> authorEntities = authorService.getAuthorsByBookIdOrdered(book.getId());
            String[][] authors = new String[authorEntities.size()][3];
            for (int i = 0; i < authors.length; i++) {
                String separator = i < authors.length - 1 ? ", " : "";
                authors[i] = new String[]{authorEntities.get(i).getSlug(), authorEntities.get(i).getName(), separator};
            }
            return authors;
        }, (v1, v2) -> v2, () -> new TreeMap<>(Comparator.comparing(Book::getTitle))));
        model.addAttribute("books", bookMap);
        model.addAttribute("totalPrice", bookMap.keySet().stream().mapToInt(Book::getPrice).sum());
        model.addAttribute("totalDiscountPrice", bookMap.keySet().stream().mapToInt(Book::getDiscountPrice).sum());
        return "cart";
    }
}
