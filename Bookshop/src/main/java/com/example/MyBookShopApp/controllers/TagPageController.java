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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
public class TagPageController {

    private final TagService tagService;
    private final BookService bookService;
    private final ObjectMapper objectMapper;

    @Autowired
    public TagPageController(TagService tagService, BookService bookService, ObjectMapper objectMapper) {
        this.tagService = tagService;
        this.bookService = bookService;
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
    }

    @GetMapping("/tags/{slug}")
    public String selectedTagPage(@PathVariable String slug, Model model) {
        String tagName = tagService.getTagName(slug);
        if (tagName == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        model.addAttribute("books", bookService.getBooksByTagSlug(slug, 0, 20));
        model.addAttribute("name", tagName);
        model.addAttribute("slug", slug);
        return "tags/index";
    }

    @GetMapping("/books/tag/{slug}")
    @ResponseBody
    public ObjectNode loadBooksByTag(@PathVariable String slug, @RequestParam int offset, @RequestParam int limit) {
        ObjectNode data = objectMapper.createObjectNode();
        List<Book> resultList = bookService.getBooksByTagSlug(slug, offset, limit);
        data.put("count", resultList.size());
        ArrayNode books = data.putArray("books");
        resultList.forEach(books::addPOJO);
        return data;
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> responseError(ResponseStatusException exception) {
        return ResponseEntity.status(exception.getStatusCode()).build();
    }
}
