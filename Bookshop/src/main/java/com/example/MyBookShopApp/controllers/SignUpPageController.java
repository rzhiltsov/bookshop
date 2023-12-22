package com.example.MyBookShopApp.controllers;

import com.example.MyBookShopApp.entities.book.BookEntity;
import com.example.MyBookShopApp.entities.book.links.Book2UserEntity;
import com.example.MyBookShopApp.entities.book.links.Book2UserTypeEntity;
import com.example.MyBookShopApp.entities.enums.ContactType;
import com.example.MyBookShopApp.entities.user.UserContactEntity;
import com.example.MyBookShopApp.entities.user.UserEntity;
import com.example.MyBookShopApp.services.BookService;
import com.example.MyBookShopApp.services.UserContactService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
public class SignUpPageController {

    private final UserService userService;
    private final UserContactService userContactService;
    private final ObjectMapper objectMapper;
    private final PasswordEncoder passwordEncoder;
    private final BookService bookService;

    @Autowired
    public SignUpPageController(UserService userService, UserContactService userContactService, ObjectMapper objectMapper,
                                PasswordEncoder passwordEncoder, BookService bookService) {
        this.userService = userService;
        this.userContactService = userContactService;
        this.objectMapper = objectMapper;
        this.passwordEncoder = passwordEncoder;
        this.bookService = bookService;
    }

    @GetMapping("/signup")
    public String signUpPage() {
        if (SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken) {
            return "signup";
        }
        return "redirect:/my";
    }

    @PostMapping("/requestContactConfirmation")
    @ResponseBody
    public ObjectNode requestContactConfirmation(@RequestParam Map<String, String> contact) {
        if (contact.get("contact") == null || contact.get("type") == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        ObjectNode result = objectMapper.createObjectNode();
        UserContactEntity userContactEntity = userContactService.getUserContactEntityByContact(contact.get("contact"));
        if (userContactEntity != null) {
            if (userContactEntity.getUser() != null) {
                result.put("result", false);
                switch (contact.get("type")) {
                    case "phone" -> result.put("error", "Такой номер телефона уже используется");
                    case "mail" -> result.put("error", "Такой почтовый адрес уже используется");
                }
                return result;
            }
        } else {
            userContactEntity = new UserContactEntity();
            switch (contact.get("type")) {
                case "phone" -> userContactEntity.setType(ContactType.PHONE);
                case "mail" -> userContactEntity.setType(ContactType.MAIL);
            }
            userContactEntity.setContact(contact.get("contact"));
        }
        userContactService.addContactUserEntity(userContactEntity);
        result.put("result", true);
        return result;
    }

    @PostMapping("/approveContact")
    @ResponseBody
    public ObjectNode approveContact(@RequestParam Map<String, String> contact) {
        if (contact.get("contact") == null && contact.get("code") == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        ObjectNode result = objectMapper.createObjectNode();
        UserContactEntity userContactEntity = userContactService.getUserContactEntityByContact(contact.get("contact"));
        userContactEntity.setApproved(true);
        userContactService.addContactUserEntity(userContactEntity);
        result.put("result", true);
        return result;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestParam Map<String, String> user, HttpServletRequest request, HttpServletResponse response) {
        if (user.get("name") == null || user.get("password") == null || user.get("phone") == null || user.get("mail") == null) {
            return ResponseEntity.badRequest().build();
        }
        UserEntity userEntity = new UserEntity();
        userEntity.setName(user.get("name"));
        userEntity.setPassword(passwordEncoder.encode(user.get("password")));
        LocalDateTime dateTime = LocalDateTime.now();
        userEntity.setRegTime(dateTime);
        userEntity.setRole("USER");
        String userHash = Stream.of(user.get("phone"), user.get("mail"), user.get("name"), dateTime.toString())
                .map(s -> Integer.toHexString(s.hashCode())).collect(Collectors.joining());
        userEntity.setHash(userHash);
        List<UserContactEntity> userContactEntities = new ArrayList<>();
        UserContactEntity phoneContact = userContactService.getUserContactEntityByContact(user.get("phone"));
        phoneContact.setUser(userEntity);
        userContactEntities.add(phoneContact);
        UserContactEntity mailContact = userContactService.getUserContactEntityByContact(user.get("mail"));
        mailContact.setUser(userEntity);
        userContactEntities.add(mailContact);
        userEntity.setContacts(userContactEntities);
        userService.addUser(userEntity);
        Map<String, List<String>> data = (LinkedHashMap) request.getAttribute("data");
        data.forEach((key, value) -> {
            Book2UserTypeEntity status;
            if (key.equals("CART")) status = userService.getBook2EntityTypeByName("CART");
            else if (key.equals("KEPT")) status = userService.getBook2EntityTypeByName("KEPT");
            else return;
            value.forEach(slug -> {
                BookEntity bookEntity = bookService.getBookEntityBySlug(slug);
                if (bookEntity == null) return;
                Book2UserEntity book2UserEntity = new Book2UserEntity();
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
        UserDetails userDetails = userService.loadUserByUsername(user.get("mail"));
        Claims claims = new DefaultClaims();
        claims.setSubject(userDetails.getUsername());
        claims.put("authorities", userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());
        String token = userService.generateUserToken(claims);
        cookie = new Cookie("user_token", token);
        cookie.setHttpOnly(true);
        cookie.setAttribute("SameSite", "Lax");
        cookie.setMaxAge((int) dateTime.until(dateTime.plusMonths(1), ChronoUnit.SECONDS));
        response.addCookie(cookie);
        return ResponseEntity.ok().build();
    }

}
