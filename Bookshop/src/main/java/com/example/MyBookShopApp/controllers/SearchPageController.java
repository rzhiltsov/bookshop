package com.example.MyBookShopApp.controllers;

import com.example.MyBookShopApp.dto.Book;
import com.example.MyBookShopApp.services.SearchService;
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
public class SearchPageController {

    private final SearchService searchService;
    private final ObjectMapper objectMapper;

    @Autowired
    public SearchPageController(SearchService searchService, ObjectMapper objectMapper) {
        this.searchService = searchService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/searchPage")
    public String searchPage(@RequestParam(required = false) String query, Model model) {
        model.addAttribute("query", query);
        List<Book> books = query == null ? List.of() : searchService.getFoundBooks(query, 0, 20);
        model.addAttribute("foundBooks", books);
        model.addAttribute("resultLabel", searchService.getResultLabel(query == null ? -1 : books.size()));
        return "/search/index";
    }

    @GetMapping("/search")
    @ResponseBody
    public ObjectNode loadFoundBooks(@RequestParam String query, @RequestParam int offset, @RequestParam int limit) {
        ObjectNode data = objectMapper.createObjectNode();
        List<Book> resultList = searchService.getFoundBooks(query, offset, limit);
        data.put("count", resultList.size());
        ArrayNode books = data.putArray("books");
        resultList.forEach(books::addPOJO);
        return data;
    }
}
