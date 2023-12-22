package com.example.MyBookShopApp.controllers;

import com.example.MyBookShopApp.dto.Book;
import com.example.MyBookShopApp.entities.author.AuthorEntity;
import com.example.MyBookShopApp.services.AuthorService;
import com.example.MyBookShopApp.services.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Controller
public class AuthorsPageController {

    private final AuthorService authorService;
    private final BookService bookService;
    private final ObjectMapper objectMapper;

    @Autowired
    public AuthorsPageController(AuthorService authorService, BookService bookService, ObjectMapper objectMapper) {
        this.authorService = authorService;
        this.bookService = bookService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/authors")
    public String authorsPage(Model model) {
        model.addAttribute("authorsMap", authorService.getAuthorsMap());
        return "authors/index";
    }

    @GetMapping("/authors/{slug}")
    public String selectedAuthorPage(@PathVariable String slug, Model model) {
        AuthorEntity authorEntity = authorService.getAuthorBySlug(slug);
        if (authorEntity == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        model.addAttribute("author", authorEntity);
        model.addAttribute("books", bookService.getBooksByAuthorSlug(slug, 0, 6));
        List<String> description = authorService.getAuthorDescription(slug);
        model.addAttribute("shownText", description.get(0));
        model.addAttribute("hiddenText", description.get(1));
        return "authors/slug";
    }

    @GetMapping("books/authors/{slug}")
    public String selectedAuthorBooksPage(@PathVariable String slug, Model model) {
        AuthorEntity authorEntity = authorService.getAuthorBySlug(slug);
        if (authorEntity == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        model.addAttribute("author", authorEntity);
        model.addAttribute("books", bookService.getBooksByAuthorSlug(slug, 0, 20));
        return "books/author";
    }

    @GetMapping("books/author/{slug}")
    @ResponseBody
    public ObjectNode loadAuthorBooks(@PathVariable String slug, int offset, int limit) {
        ObjectNode data = objectMapper.createObjectNode();
        List<Book> resultList = bookService.getBooksByAuthorSlug(slug, offset, limit);
        data.put("count", resultList.size());
        ArrayNode books = data.putArray("books");
        resultList.forEach(books::addPOJO);
        return data;
    }

}
