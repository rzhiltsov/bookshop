package com.example.MyBookShopApp.controllers;

import com.example.MyBookShopApp.entities.author.AuthorEntity;
import com.example.MyBookShopApp.entities.book.BookEntity;
import com.example.MyBookShopApp.entities.book.rating.BookRatingEntity;
import com.example.MyBookShopApp.entities.tag.TagEntity;
import com.example.MyBookShopApp.services.AuthorService;
import com.example.MyBookShopApp.services.BookRatingService;
import com.example.MyBookShopApp.services.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
public class BooksPageController {

    private final BookService bookService;
    private final AuthorService authorService;
    private  final BookRatingService bookRatingService;
    private final ObjectMapper objectMapper;

    @Autowired
    public BooksPageController(BookService bookService, AuthorService authorService, BookRatingService bookRatingService, ObjectMapper objectMapper) {
        this.bookService = bookService;
        this.authorService = authorService;
        this.bookRatingService = bookRatingService;
        this.objectMapper = objectMapper;
    }

    @GetMapping(value = "/books/{slug}")
    public String bookPage(@PathVariable String slug, Model model, HttpServletRequest request) {
        BookEntity bookEntity = bookService.getBookEntityBySlug(slug);
        if (bookEntity == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        List<TagEntity> tagEntities = bookEntity.getTags();
        String[][] tags = new String[tagEntities.size()][3];
        for (int i = 0; i < tags.length; i++) {
            String separator = i < tags.length - 1 ? ", " : "";
            tags[i] = new String[]{tagEntities.get(i).getSlug(), tagEntities.get(i).getName(), separator};
        }
        List<AuthorEntity> authorEntities = authorService.getAuthorsByBookIdOrdered(bookEntity.getId());
        String[][] authors = new String[authorEntities.size()][3];
        for (int i = 0; i < authors.length; i++) {
            String separator = i < authors.length - 1 ? ", " : "";
            authors[i] = new String[]{authorEntities.get(i).getSlug(), authorEntities.get(i).getName(), separator};
        }
        model.addAttribute("book", bookService.getBookBySlug(slug));
        model.addAttribute("tags", tags);
        model.addAttribute("authors", authors);
        Map<Integer, Integer> ratings = bookEntity.getRatings().stream()
                .collect(Collectors.toMap(bookRatingEntity -> (int) bookRatingEntity.getValue(), bookRatingEntity -> 1, Integer::sum));
        model.addAttribute("ratings", ratings);
        model.addAttribute("ratingsCount", ratings.values().stream().mapToInt(Integer::valueOf).sum());
        Map<String, Set<String>> cookies = Map.of();
        if (request.getCookies() != null) {
            cookies = Stream.of(request.getCookies())
                    .collect(Collectors.toMap(Cookie::getName, cookie -> {
                        if (cookie.getValue().isEmpty()) return Set.of();
                        else if (cookie.getValue().contains("/")) return Set.of(cookie.getValue().split("/"));
                        else return Set.of(cookie.getValue());
                    }));
        }
        List<String> cartStatusText = Arrays.asList("Купить", "В корзине");
        if (cookies.getOrDefault("CART", Set.of()).contains(slug)) {
            Collections.reverse(cartStatusText);
            model.addAttribute("cartClass", "btn btn_primary btn_outline btn_check");
            model.addAttribute("cartCheck", true);
        }
        else {
            model.addAttribute("cartClass", "btn btn_primary btn_outline");
            model.addAttribute("cartCheck", false);
        }
        model.addAttribute("cartText", cartStatusText.get(0));
        model.addAttribute("cartAltText", cartStatusText.get(1));
        model.addAttribute("cartAmount", cookies.getOrDefault("CART", Set.of()).size());
        List<String> keptStatusText = Arrays.asList("Отложить", "Отложено");
        if (cookies.getOrDefault("KEPT", Set.of()).contains(slug)) {
            Collections.reverse(cartStatusText);
            model.addAttribute("keptClass", "btn btn_primary btn_outline btn_check");
            model.addAttribute("keptCheck", true);
        }
        else {
            model.addAttribute("keptClass", "btn btn_primary btn_outline");
            model.addAttribute("keptCheck", false);
        }
        model.addAttribute("keptText", keptStatusText.get(0));
        model.addAttribute("keptAltText", keptStatusText.get(1));
        model.addAttribute("keptAmount", cookies.getOrDefault("KEPT", Set.of()).size());
        return "books/slug";
    }

    @PostMapping(value = "/changeBookStatus")
    @ResponseBody
    public ObjectNode changeBookStatus(@RequestParam Map<String, String> status, HttpServletRequest request, HttpServletResponse response) {
        if (status.get("status") == null || status.get("booksIds") == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        String changingStatus = status.get("status");
        List<String> ids = List.of(status.get("booksIds").split(", "));
        Map<String, Set<String>> cookies = Map.of();
        if (request.getCookies() == null) {
            if (!changingStatus.equals("UNLINK")) {
                cookies = Map.of(changingStatus, Set.copyOf(ids));
                response.addCookie(new Cookie(changingStatus, String.join("/", ids)));
            }
        }
        else {
            cookies = Stream.of(request.getCookies()).filter(cookie -> cookie.getName().matches("(CART)|(KEPT)|(PAID)|(ARCHIVED)"))
                    .collect(Collectors.toMap(Cookie::getName, cookie -> {
                        if (cookie.getValue().isEmpty()) return new LinkedHashSet<>();
                        else if (cookie.getValue().contains("/")) return new LinkedHashSet<>(List.of(cookie.getValue().split("/")));
                        else return new LinkedHashSet<>(List.of(cookie.getValue()));
                    }));
            if (!cookies.containsKey(changingStatus) && !changingStatus.equals("UNLINK")) {
                cookies.put(changingStatus, new LinkedHashSet<>());
            }
            cookies.forEach((key, value) -> {
                if (key.equals(changingStatus)) {
                     if (value.addAll(ids)) {
                        response.addCookie(new Cookie(key, String.join("/", value)));
                    }
                }
                else if (value.removeAll(ids)) {
                    response.addCookie(new Cookie(key, String.join("/", value)));
                }
            });
        }
        ObjectNode result = objectMapper.createObjectNode();
        result.put("result", true);
        result.put("cartAmount", cookies.getOrDefault("CART", Set.of()).size());
        result.put("keptAmount", cookies.getOrDefault("KEPT", Set.of()).size());
        return result;
    }

    @PostMapping("/rateBook")
    @ResponseBody
    public ObjectNode rateBook(@RequestParam Map<String, String> status) {
        if (status.get("bookId") == null || status.get("value") == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        BookEntity bookEntity = bookService.getBookEntityBySlug(status.get("bookId"));
        ObjectNode result = objectMapper.createObjectNode();
        if (bookEntity == null) {
            result.put("result", false);
            result.put("error", "Книга не найдена");
        }
        else {
            BookRatingEntity bookRatingEntity = new BookRatingEntity();
            bookRatingEntity.setBook(bookEntity);
            bookRatingEntity.setValue(Short.parseShort(status.get("value")));
            bookRatingService.addRating(bookRatingEntity);
            result.put("result", true);
        }
        return result;
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> responseError(ResponseStatusException exception) {
        return ResponseEntity.status(exception.getStatusCode()).build();
    }
}
