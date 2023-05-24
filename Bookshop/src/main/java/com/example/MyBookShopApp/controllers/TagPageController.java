package com.example.MyBookShopApp.controllers;

import com.example.MyBookShopApp.dto.Book;
import com.example.MyBookShopApp.services.BookService;
import com.example.MyBookShopApp.services.TagService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

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

    @GetMapping("/tags/{slug}")
    public String selectedTagPage(@PathVariable String slug, Model model) {
        model.addAttribute("books", bookService.getBooksByTag(slug, 0, 20));
        model.addAttribute("name", tagService.getTagName(slug));
        model.addAttribute("slug", slug);
        return "tags/index";
    }

    @GetMapping("/books/tag/{slug}")
    @ResponseBody
    public ObjectNode loadBooksByTag(@PathVariable String slug, @RequestParam int offset, @RequestParam int limit) {
        ObjectNode data = objectMapper.createObjectNode();
        List<Book> resultList = bookService.getBooksByTag(slug, offset, limit);
        data.put("count", resultList.size());
        ArrayNode books = data.putArray("books");
        resultList.forEach(books::addPOJO);
        return data;
    }
}
