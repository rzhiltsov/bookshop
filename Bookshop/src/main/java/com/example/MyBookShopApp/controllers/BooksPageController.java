package com.example.MyBookShopApp.controllers;

import com.example.MyBookShopApp.dto.Book;
import com.example.MyBookShopApp.dto.BookReview;
import com.example.MyBookShopApp.entities.author.AuthorEntity;
import com.example.MyBookShopApp.entities.book.BookEntity;
import com.example.MyBookShopApp.entities.book.file.BookFileEntity;
import com.example.MyBookShopApp.entities.book.file.FileDownloadEntity;
import com.example.MyBookShopApp.entities.book.links.Book2UserEntity;
import com.example.MyBookShopApp.entities.book.links.Book2UserTypeEntity;
import com.example.MyBookShopApp.entities.book.rating.BookRatingEntity;
import com.example.MyBookShopApp.entities.book.review.BookReviewEntity;
import com.example.MyBookShopApp.entities.book.review.BookReviewLikeEntity;
import com.example.MyBookShopApp.entities.tag.TagEntity;
import com.example.MyBookShopApp.entities.user.UserEntity;
import com.example.MyBookShopApp.services.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URLConnection;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class BooksPageController {

    private final BookService bookService;
    private final AuthorService authorService;
    private final BookRatingService bookRatingService;
    private final ObjectMapper objectMapper;
    private final BookReviewService bookReviewService;
    private final BookReviewLikeService bookReviewLikeService;
    private final BookFileService bookFileService;
    private final UserService userService;
    private final FileDownloadService fileDownloadService;

    @Autowired
    public BooksPageController(BookService bookService, AuthorService authorService, BookRatingService bookRatingService,
                               ObjectMapper objectMapper, BookReviewService bookReviewService, BookReviewLikeService bookReviewLikeService,
                               BookFileService bookFileService, UserService userService, FileDownloadService fileDownloadService) {
        this.bookService = bookService;
        this.authorService = authorService;
        this.bookRatingService = bookRatingService;
        this.objectMapper = objectMapper;
        this.bookReviewService = bookReviewService;
        this.bookReviewLikeService = bookReviewLikeService;
        this.bookFileService = bookFileService;
        this.userService = userService;
        this.fileDownloadService = fileDownloadService;
    }

    @GetMapping("/books/{slug}")
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
        Book book = bookService.createBook(bookEntity);
        model.addAttribute("book", book);
        model.addAttribute("tags", tags);
        model.addAttribute("authors", authors);
        Map<Integer, Integer> ratings = bookEntity.getRatings().stream()
                .collect(Collectors.toMap(bookRatingEntity -> (int) bookRatingEntity.getValue(), bookRatingEntity -> 1, Integer::sum));
        model.addAttribute("ratings", ratings);
        model.addAttribute("ratingsCount", ratings.values().stream().mapToInt(Integer::valueOf).sum());
        Comparator<BookReview> reviewComparator = Comparator.comparing((BookReview bookReview) -> bookReview.getLikesCount() - bookReview.getDislikesCount())
                .thenComparing(BookReview::getTime).reversed();
        List<BookReview> reviews = bookEntity.getReviews().stream().map(bookReviewService::createBookReview).sorted(reviewComparator).toList();
        model.addAttribute("reviewsCountLabel", bookReviewService.getReviewsCountLabel(reviews.size()));
        model.addAttribute("reviews", reviews);
        List<String> cartStatusText = Arrays.asList("Купить", "В корзине");
        String cartClass = "btn btn_primary btn_outline";
        boolean cartCheck = false;
        List<String> keptStatusText = Arrays.asList("Отложить", "Отложено");
        String keptClass = "btn btn_primary btn_outline";
        boolean keptCheck = false;
        List<String> archivedStatusText = Arrays.asList("В архив", "Вернуть из архива");
        String archivedClass = "btn btn_primary btn_outline";
        boolean archivedCheck = false;
        String ratingClass = "Rating Rating_input";
        int ratingValue = 0;
        if (SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken) {
            Map<String, List<String>> data = (LinkedHashMap) request.getAttribute("data");
            if (data.getOrDefault("CART", List.of()).contains(slug)) {
                Collections.reverse(cartStatusText);
                cartClass = cartClass.concat(" btn_check");
                cartCheck = true;
            }
            if (data.getOrDefault("KEPT", List.of()).contains(slug)) {
                Collections.reverse(keptStatusText);
                keptClass = keptClass.concat(" btn_check");
                keptCheck = true;
            }
        } else {
            if (book.getStatus().equals("CART")) {
                Collections.reverse(cartStatusText);
                cartClass = cartClass.concat(" btn_check");
                cartCheck = true;
            }
            if (book.getStatus().equals("KEPT")) {
                Collections.reverse(keptStatusText);
                keptClass = keptClass.concat(" btn_check");
                keptCheck = true;
            }
            if (book.getStatus().equals("ARCHIVED")) {
                Collections.reverse(archivedStatusText);
                archivedClass = archivedClass.concat(" btn_check");
                archivedCheck = true;
            }
            String userHash = SecurityContextHolder.getContext().getAuthentication().getName();
            UserEntity userEntity = userService.getUserEntityByHash(userHash);
            BookRatingEntity bookRatingEntity = userEntity.getRatings().stream()
                    .filter(rating -> rating.getBook().getId() == bookEntity.getId()).findFirst().orElse(null);
            if (bookRatingEntity != null) {
                ratingClass = ratingClass.concat(" Rating_inputClick");
                ratingValue = bookRatingEntity.getValue();
            }
        }
        model.addAttribute("cartText", cartStatusText.get(0));
        model.addAttribute("cartAltText", cartStatusText.get(1));
        model.addAttribute("cartClass", cartClass);
        model.addAttribute("cartCheck", cartCheck);
        model.addAttribute("keptText", keptStatusText.get(0));
        model.addAttribute("keptAltText", keptStatusText.get(1));
        model.addAttribute("keptClass", keptClass);
        model.addAttribute("keptCheck", keptCheck);
        if (SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken) {
            return "books/slug";
        } else {
            model.addAttribute("archivedText", archivedStatusText.get(0));
            model.addAttribute("archivedAltText", archivedStatusText.get(1));
            model.addAttribute("archivedClass", archivedClass);
            model.addAttribute("archivedCheck", archivedCheck);
            model.addAttribute("ratingClass", ratingClass);
            model.addAttribute("ratingValue", ratingValue);
            List<BookFileEntity> bookFileEntities = bookEntity.getFiles();
            bookFileEntities.sort(Comparator.comparing(file -> file.getType().getId()));
            model.addAttribute("files", bookFileEntities);
            return "books/slugmy";
        }
    }

    @PostMapping("/changeBookStatus")
    @ResponseBody
    public ObjectNode changeBookStatus(@RequestParam Map<String, String> status, HttpServletRequest request, HttpServletResponse response) {
        if (status.get("status") == null || status.get("bookSlugs") == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        ObjectNode result = objectMapper.createObjectNode();
        String changingStatus = status.get("status");
        List<String> slugs = List.of(status.get("bookSlugs").split(", "));
        int cartAmount;
        int keptAmount;
        if (SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken) {
            slugs.forEach(slug -> {
                if (bookService.getBookEntityBySlug(slug) == null)
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            });
            if (status.get("status").matches("(PAID|ARCHIVED)"))
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            Map<String, List<String>> data = (LinkedHashMap) request.getAttribute("data");
            if (!data.containsKey(changingStatus) && !changingStatus.equals("UNLINK")) {
                data.put(changingStatus, new LinkedList<>());
            }
            data.forEach((key, value) -> {
                if (key.equals(changingStatus)) {
                    value.addAll(slugs);
                } else {
                    value.removeAll(slugs);
                }
            });
            Claims claims = new DefaultClaims();
            claims.put("data", data);
            String token = userService.generateAnonymousToken(claims);
            Cookie cookie = new Cookie("anonymous_token", token);
            cookie.setHttpOnly(true);
            cookie.setAttribute("SameSite", "Lax");
            LocalDateTime dateTime = LocalDateTime.now();
            cookie.setMaxAge((int) dateTime.until(dateTime.plusMonths(1), ChronoUnit.SECONDS));
            response.addCookie(cookie);
            cartAmount = data.getOrDefault("CART", List.of()).size();
            keptAmount = data.getOrDefault("KEPT", List.of()).size();
        } else {
            String userHash = SecurityContextHolder.getContext().getAuthentication().getName();
            UserEntity userEntity = userService.getUserEntityByHash(userHash);
            Book2UserTypeEntity book2UserTypeEntity = userService.getBook2EntityTypeByName(changingStatus);
            slugs.forEach(slug -> {
                BookEntity bookEntity = bookService.getBookEntityBySlug(slug);
                if (bookEntity == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
                Book2UserEntity book2UserEntity = userService.getBook2UserByBookIdAndUserId(bookEntity.getId(), userEntity.getId());
                if (book2UserEntity != null && changingStatus.equals("UNLINK")) {
                    userService.deleteBook2User(book2UserEntity);
                } else if (book2UserTypeEntity != null) {
                    if (book2UserEntity == null) {
                        book2UserEntity = new Book2UserEntity();
                        book2UserEntity.setBookId(bookEntity.getId());
                        book2UserEntity.setUserId(userEntity.getId());
                    }
                    book2UserEntity.setTime(LocalDateTime.now());
                    book2UserEntity.setType(book2UserTypeEntity);
                    userService.addBook2User(book2UserEntity);
                }
            });
            cartAmount = (int) userService.getBook2UsersByUserId(userEntity.getId()).stream()
                    .filter(book2UserEntity -> book2UserEntity.getType().getName().equals("CART")).count();
            keptAmount = (int) userService.getBook2UsersByUserId(userEntity.getId()).stream()
                    .filter(book2UserEntity -> book2UserEntity.getType().getName().equals("KEPT")).count();
        }
        result.put("result", true);
        result.put("cartAmount", cartAmount);
        result.put("keptAmount", keptAmount);
        return result;
    }

    @PostMapping("/rateBook")
    @ResponseBody
    public ObjectNode rateBook(@RequestParam Map<String, String> status) {
        if (status.get("bookSlug") == null || status.get("value") == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        short value;
        try {
            value = Short.parseShort(status.get("value"));
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        BookEntity bookEntity = bookService.getBookEntityBySlug(status.get("bookSlug"));
        ObjectNode result = objectMapper.createObjectNode();
        if (bookEntity == null) {
            result.put("result", false);
            result.put("error", "Книга не найдена");
        } else if (SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken) {
            result.put("result", false);
            result.put("error", "Войдите чтобы оценить книгу");
        } else {
            String userHash = SecurityContextHolder.getContext().getAuthentication().getName();
            UserEntity userEntity = userService.getUserEntityByHash(userHash);
            BookRatingEntity bookRatingEntity = userEntity.getRatings().stream()
                    .filter(rating -> rating.getBook().getId() == bookEntity.getId()).findFirst().orElse(null);
            if (bookRatingEntity == null) {
                bookRatingEntity = new BookRatingEntity();
                bookRatingEntity.setBook(bookEntity);
                bookRatingEntity.setUser(userEntity);
            }
            bookRatingEntity.setTime(LocalDateTime.now());
            bookRatingEntity.setValue(value);
            bookRatingService.addRating(bookRatingEntity);
            result.put("result", true);
        }
        return result;
    }

    @PostMapping("/bookReview")
    @ResponseBody
    public ObjectNode bookReview(@RequestParam Map<String, String> status) {
        if (status.get("bookSlug") == null || status.get("text") == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        BookEntity bookEntity = bookService.getBookEntityBySlug(status.get("bookSlug"));
        ObjectNode result = objectMapper.createObjectNode();
        if (bookEntity == null) {
            result.put("result", false);
            result.put("error", "Книга не найдена");
        } else if (SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken) {
            result.put("result", false);
            result.put("error", "Войдите чтобы оставить отзыв");
        } else {
            String userHash = SecurityContextHolder.getContext().getAuthentication().getName();
            UserEntity userEntity = userService.getUserEntityByHash(userHash);
            BookReviewEntity bookReviewEntity = new BookReviewEntity();
            bookReviewEntity.setBook(bookEntity);
            bookReviewEntity.setUser(userEntity);
            bookReviewEntity.setText(status.get("text"));
            bookReviewEntity.setTime(LocalDateTime.now());
            bookReviewService.addReview(bookReviewEntity);
            result.put("result", true);
        }
        return result;
    }

    @PostMapping("/rateBookReview")
    @ResponseBody
    public ObjectNode rateBookReview(@RequestParam Map<String, String> status) {
        if (status.get("reviewId") == null || status.get("value") == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        int reviewId;
        short value;
        try {
            reviewId = Integer.parseInt(status.get("reviewId"));
            value = Short.parseShort(status.get("value"));
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        BookReviewEntity bookReviewEntity = bookReviewService.findBookReviewEntityById(reviewId);
        ObjectNode result = objectMapper.createObjectNode();
        if (bookReviewEntity == null) {
            result.put("result", false);
            result.put("error", "Отзыв не найден");
        } else if (SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken) {
            result.put("result", false);
            result.put("error", "Войдите чтобы оценить отзыв");
        } else {
            String userHash = SecurityContextHolder.getContext().getAuthentication().getName();
            UserEntity userEntity = userService.getUserEntityByHash(userHash);
            BookReviewLikeEntity bookReviewLikeEntity = userEntity.getReviewLikes().stream()
                    .filter(reviewLike -> reviewLike.getReview().getId() == bookReviewEntity.getId()).findFirst().orElse(null);
            if (bookReviewLikeEntity != null && value == 0) {
                bookReviewLikeService.deleteReviewLike(bookReviewLikeEntity);
            } else {
                if (bookReviewLikeEntity == null) {
                    bookReviewLikeEntity = new BookReviewLikeEntity();
                    bookReviewLikeEntity.setReview(bookReviewEntity);
                    bookReviewLikeEntity.setUser(userEntity);
                }
                bookReviewLikeEntity.setTime(LocalDateTime.now());
                bookReviewLikeEntity.setValue(value);
                bookReviewLikeService.addReviewLike(bookReviewLikeEntity);
            }
            result.put("result", true);
        }
        return result;
    }

    @GetMapping("/download/{hash}")
    public ResponseEntity<ByteArrayResource> downloadBook(@PathVariable String hash, HttpServletRequest request) {
        BookFileEntity bookFileEntity = bookFileService.getBookFileByHash(hash);
        if (bookFileEntity == null) {
            return ResponseEntity.notFound().build();
        }
        String userHash = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity userEntity = userService.getUserEntityByHash(userHash);
        BookEntity bookEntity = bookFileEntity.getBook();
        Book2UserEntity book2UserEntity = userService.getBook2UserByBookIdAndUserId(bookEntity.getId(), userEntity.getId());
        FileDownloadEntity fileDownloadEntity = fileDownloadService.getFileDownloadByBookIdAndUserId(bookEntity, userEntity);
        if (request.getHeader("Check") != null) {
            if (fileDownloadEntity == null || fileDownloadEntity.getCount() < 3) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        if (book2UserEntity == null || !book2UserEntity.getType().getName().matches("(PAID)|(ARCHIVED)")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (fileDownloadEntity == null) {
            fileDownloadEntity = new FileDownloadEntity();
            fileDownloadEntity.setBook(bookEntity);
            fileDownloadEntity.setUser(userEntity);
            fileDownloadEntity.setCount(1);
            fileDownloadService.addFileDownload(fileDownloadEntity);
        } else if (fileDownloadEntity.getCount() < 3) {
            fileDownloadEntity.setCount(fileDownloadEntity.getCount() + 1);
            fileDownloadService.addFileDownload(fileDownloadEntity);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        String fileName = bookEntity.getTitle() + "." + bookFileEntity.getType().getName();
        String mimeType = URLConnection.guessContentTypeFromName(fileName);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\"" + fileName + "\"")
                .contentType(mimeType != null ? MediaType.parseMediaType(mimeType) : MediaType.APPLICATION_OCTET_STREAM)
                .body(new ByteArrayResource(bookService.getBookInfo(bookFileEntity.getBook()).getBytes()));
    }

}
