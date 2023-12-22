package com.example.MyBookShopApp.controllers;

import com.example.MyBookShopApp.services.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PopularPageController {

    private final BookService bookService;

    @Autowired
    public PopularPageController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/books/popularPage")
    public String popularPage(Model model) {
        model.addAttribute("popularBooks", bookService.getPopularBooks(0, 20));
        return "/books/popular";
    }
}
