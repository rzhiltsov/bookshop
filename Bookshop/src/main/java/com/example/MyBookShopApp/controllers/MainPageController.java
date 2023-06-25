package com.example.MyBookShopApp.controllers;

import com.example.MyBookShopApp.dto.Book;
import com.example.MyBookShopApp.services.BookService;
import com.example.MyBookShopApp.services.TagService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
public class MainPageController {

    private final BookService bookService;
    private final TagService tagService;
    private final ObjectMapper objectMapper;

    @Autowired
    public MainPageController(BookService bookService, TagService tagService, ObjectMapper objectMapper) {
        this.bookService = bookService;
        this.tagService = tagService;
        this.objectMapper = objectMapper;
    }

    @ModelAttribute
    public void cartAndKeptAmount(Model model, HttpServletRequest request) {
        Map<String, Set<String>> cookies = Map.of();
        if (request.getCookies() != null) {
            cookies = Stream.of(request.getCookies())
                    .collect(Collectors.toMap(Cookie::getName, cookie -> {
                        if (cookie.getValue().isEmpty()) return Set.of();
                        else if (cookie.getValue().contains("/")) return Set.of(cookie.getValue().split("/"));
                        else return Set.of(cookie.getValue());
                    }));
        }
        model.addAttribute("cartAmount", cookies.getOrDefault("CART", Set.of()).size());
        model.addAttribute("keptAmount", cookies.getOrDefault("KEPT", Set.of()).size());
    }

    @GetMapping("/")
    public String showMainPage(Model model) {
        model.addAttribute("recommendedBooks", bookService.getRecommendedBooks(0, 6));
        model.addAttribute("recentBooks", bookService.getRecentBooks(0, 6));
        model.addAttribute("popularBooks", bookService.getPopularBooks(0, 6));
        model.addAttribute("tags", tagService.getAllTags());
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