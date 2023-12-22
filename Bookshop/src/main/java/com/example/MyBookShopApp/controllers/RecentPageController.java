package com.example.MyBookShopApp.controllers;

import com.example.MyBookShopApp.services.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;

@Controller
public class RecentPageController {

    private final BookService bookService;

    @Autowired
    public RecentPageController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/books/recentPage")
    public String recentPage(Model model) {
        String from = LocalDate.now().minusYears(5).toString();
        String to = LocalDate.now().toString();
        model.addAttribute("recentBooks", bookService.getRecentBooks(from, to, 0, 20));
        return "/books/recent";
    }
}
