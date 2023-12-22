package com.example.MyBookShopApp.controllers;

import com.example.MyBookShopApp.entities.book.BookEntity;
import com.example.MyBookShopApp.entities.book.links.Book2UserEntity;
import com.example.MyBookShopApp.entities.book.links.Book2UserTypeEntity;
import com.example.MyBookShopApp.entities.user.UserEntity;
import com.example.MyBookShopApp.services.BookService;
import com.example.MyBookShopApp.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SignInPageController {

    private final ObjectMapper objectMapper;
    private final AuthenticationProvider authenticationProvider;
    private final UserService userService;
    private final BookService bookService;

    @Autowired
    public SignInPageController(ObjectMapper objectMapper, AuthenticationProvider authenticationProvider, UserService userService,
                                BookService bookService) {
        this.objectMapper = objectMapper;
        this.authenticationProvider = authenticationProvider;
        this.userService = userService;
        this.bookService = bookService;
    }

    @GetMapping("/signin")
    public String signInPage(HttpServletRequest request, HttpServletResponse response) {
        if (SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken) {
            String host = request.getHeader("Host");
            String referer = request.getHeader("Referer");
            if (referer != null && referer.contains(host)) {
                Cookie cookie = new Cookie("loginReferer", referer);
                cookie.setMaxAge(3600);
                response.addCookie(cookie);
            }
            return "signin";
        }
        return "redirect:/my";

    }

    @PostMapping("/login")
    @ResponseBody
    public ObjectNode login(@RequestParam Map<String, String> user, HttpServletRequest request, HttpServletResponse response) {
        if (user.get("contact") == null || user.get("password") == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        ObjectNode result = objectMapper.createObjectNode();
        try {
            Authentication authentication = authenticationProvider.authenticate(new UsernamePasswordAuthenticationToken(user.get("contact"), user.get("password")));
            UserEntity userEntity = userService.getUserEntityByHash(authentication.getName());
            LocalDateTime dateTime = LocalDateTime.now();
            Map<String, List<String>> data = (LinkedHashMap) request.getAttribute("data");
            data.forEach((key, value) -> {
                Book2UserTypeEntity status;
                if (key.equals("CART")) status = userService.getBook2EntityTypeByName("CART");
                else if (key.equals("KEPT")) status = userService.getBook2EntityTypeByName("KEPT");
                else return;
                value.forEach(slug -> {
                    BookEntity bookEntity = bookService.getBookEntityBySlug(slug);
                    if (bookEntity == null) return;
                    Book2UserEntity book2UserEntity = userService.getBook2UserByBookIdAndUserId(bookEntity.getId(), userEntity.getId());
                    if (book2UserEntity != null) return;
                    book2UserEntity = new Book2UserEntity();
                    book2UserEntity.setBookId(bookEntity.getId());
                    book2UserEntity.setUserId(userEntity.getId());
                    book2UserEntity.setType(status);
                    book2UserEntity.setTime(dateTime);
                    userService.addBook2User(book2UserEntity);
                });
            });
            Cookie cookie = new Cookie("anonymous_token", null);
            cookie.setMaxAge(0);
            response.addCookie(cookie);
            Claims claims = new DefaultClaims();
            claims.setSubject(authentication.getName());
            claims.put("authorities", authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());
            String token = userService.generateUserToken(claims);
            cookie = new Cookie("user_token", token);
            cookie.setHttpOnly(true);
            cookie.setAttribute("SameSite", "Lax");
            cookie.setMaxAge((int) dateTime.until(dateTime.plusMonths(1), ChronoUnit.SECONDS));
            response.addCookie(cookie);
            result.put("result", true);
        } catch (BadCredentialsException exception) {
            result.put("result", false);
            result.put("error", "Неправильные логин или пароль");
        }
        return result;
    }

}
