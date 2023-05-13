package com.example.MyBookShopApp.controllers;

import com.example.MyBookShopApp.dto.Book;
import com.example.MyBookShopApp.services.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class MainPageController {

    private final BookService bookService;
    private final ObjectMapper objectMapper;

    @Autowired
    public MainPageController(BookService bookService, ObjectMapper objectMapper) {
        this.bookService = bookService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/")
    public String showMainPage(Model model) {
        model.addAttribute("recommendedBooks", bookService.getRecommendedBooks(0, 6));
        model.addAttribute("recentBooks", bookService.getRecentBooks(0, 6));
        model.addAttribute("popularBooks", bookService.getPopularBooks(0, 6));
        return "index";
    }

    @GetMapping("/books/recommended")
    @ResponseBody
    public ObjectNode loadRecommendedBooks(@RequestParam int offset, @RequestParam int limit) {
        ObjectNode data = objectMapper.createObjectNode();
        List<Book> resultList = bookService.getRecommendedBooks(offset, limit);
        data.put("count", resultList.size());
        ArrayNode books = data.putArray("books");
        resultList.forEach(books::addPOJO);
        return data;
    }

    @GetMapping("/books/recent")
    @ResponseBody
    public ObjectNode loadRecentBooks(@RequestParam(required = false) String from, @RequestParam(required = false) String to, @RequestParam int offset, @RequestParam int limit) {
        ObjectNode data = objectMapper.createObjectNode();
        List<Book> resultList = bookService.getRecentBooks(from, to, offset, limit);
        data.put("count", resultList.size());
        ArrayNode books = data.putArray("books");
        resultList.forEach(books::addPOJO);
        return data;
    }

    @GetMapping("/books/popular")
    @ResponseBody
    public ObjectNode loadPopularBooks(@RequestParam int offset, @RequestParam int limit) {
        ObjectNode data = objectMapper.createObjectNode();
        List<Book> resultList = bookService.getPopularBooks(offset, limit);
        data.put("count", resultList.size());
        ArrayNode books = data.putArray("books");
        resultList.forEach(books::addPOJO);
        return data;
    }
}