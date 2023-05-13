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
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping({"/searchPage/", "/searchPage/{query}"})
    public String searchPage(@PathVariable(required = false) String query, Model model) {
        model.addAttribute("query", query);
        model.addAttribute("foundBooks", searchService.getFoundBooks(query, 0, 20));
        model.addAttribute("resultLabel", searchService.getResultLabel(query));
        return "/search/index";
    }

    @GetMapping({"/search/", "/search/{query}"})
    @ResponseBody
    public ObjectNode loadFoundBooks(@PathVariable(required = false) String query, @PathVariable(required = false) @RequestParam int offset, @RequestParam int limit) {
        ObjectNode data = objectMapper.createObjectNode();
        List<Book> resultList = searchService.getFoundBooks(query, offset, limit);
        data.put("count", resultList.size());
        ArrayNode books = data.putArray("books");
        resultList.forEach(books::addPOJO);
        return data;
    }
}
