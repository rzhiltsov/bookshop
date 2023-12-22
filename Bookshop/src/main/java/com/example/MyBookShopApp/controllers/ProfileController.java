package com.example.MyBookShopApp.controllers;

import com.example.MyBookShopApp.dto.Book;
import com.example.MyBookShopApp.entities.book.links.Book2UserEntity;
import com.example.MyBookShopApp.entities.user.UserEntity;
import com.example.MyBookShopApp.services.BookService;
import com.example.MyBookShopApp.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class ProfileController {

    private final UserService userService;
    private final BookService bookService;

    @Autowired
    public ProfileController(UserService userService, BookService bookService) {
        this.userService = userService;
        this.bookService = bookService;
    }

    @GetMapping("/my")
    public String myPage(Model model) {
        String userHash = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity userEntity = userService.getUserEntityByHash(userHash);
        List<Integer> bookIds = userService.getBook2UsersByUserId(userEntity.getId()).stream()
                .filter(book2UserEntity -> book2UserEntity.getType().getName().equals("PAID"))
                .map(Book2UserEntity::getBookId).toList();
        List<Book> books = bookService.getBookEntitiesByIds(bookIds).stream().map(bookService::createBook).toList();
        model.addAttribute("books", books);
        return "my";
    }

    @GetMapping("/my/archive")
    public String myArchivePage(Model model) {
        String userHash = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity userEntity = userService.getUserEntityByHash(userHash);
        List<Integer> bookIds = userService.getBook2UsersByUserId(userEntity.getId()).stream()
                .filter(book2UserEntity -> book2UserEntity.getType().getName().equals("ARCHIVED"))
                .map(Book2UserEntity::getBookId).toList();
        List<Book> books = bookService.getBookEntitiesByIds(bookIds).stream().map(bookService::createBook).toList();
        model.addAttribute("books", books);
        return "myarchive";
    }

    @GetMapping("/profile")
    public String profilePage() {
        return "profile";
    }
}
