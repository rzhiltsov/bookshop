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
import org.springframework.mail.MailException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClientException;
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
    private final BookService bookService;

    @Autowired
    public SignUpPageController(UserService userService, UserContactService userContactService, ObjectMapper objectMapper,
                                BookService bookService) {
        this.userService = userService;
        this.userContactService = userContactService;
        this.objectMapper = objectMapper;
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
        if (!contact.get("type").equals("phone") && !contact.get("type").equals("mail")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        ObjectNode result = objectMapper.createObjectNode();
        LocalDateTime dateTime = LocalDateTime.now();
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
            if (userContactEntity.getCodeTime().plusMinutes(5).isBefore(dateTime)) {
                userContactEntity.setCodeTrails((short) 0);
            }
            if (userContactEntity.getCodeTrails() == 3) {
                result.put("result", false);
                long interval = dateTime.until(userContactEntity.getCodeTime().plusMinutes(5), ChronoUnit.SECONDS);
                result.put("error", "Слишком много попыток, попробуйте через " + interval + " сек");
                return result;
            }
        } else {
            userContactEntity = new UserContactEntity();
            userContactEntity.setContact(contact.get("contact"));
            switch (contact.get("type")) {
                case "phone" -> userContactEntity.setType(ContactType.PHONE);
                case "mail" -> userContactEntity.setType(ContactType.MAIL);
            }
        }
        String code = userContactService.generateConfirmationCode();
        userContactEntity.setCode(code);
        userContactEntity.setCodeTrails((short) (userContactEntity.getCodeTrails() + 1));
        userContactEntity.setCodeTime(dateTime);
        if (userContactEntity.getType() == ContactType.PHONE) {
            try {
                userContactService.sendConfirmationCodeByPhone(contact.get("contact"), code);
            } catch (RestClientException e) {
                result.put("result", false);
                result.put("error", "Ошибка отправки SMS");
                return result;
            }
        } else if (userContactEntity.getType() == ContactType.MAIL) {
            try {
                userContactService.sendConfirmationCodeByMail(contact.get("contact"), code);
            } catch (MailException e) {
                result.put("result", false);
                result.put("error", "Ошибка отправки письма");
                return result;
            }
        }
        userContactService.addContactUserEntity(userContactEntity);
        result.put("result", true);
        return result;
    }

    @PostMapping("/approveContact")
    @ResponseBody
    public ObjectNode approveContact(@RequestParam Map<String, String> contact) {
        if (contact.get("contact") == null || contact.get("code") == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        ObjectNode result = objectMapper.createObjectNode();
        LocalDateTime dateTime = LocalDateTime.now();
        UserContactEntity userContactEntity = userContactService.getUserContactEntityByContact(contact.get("contact"));
        if (userContactEntity == null) {
            result.put("result", false);
            result.put("error", "Контакт не найден");
            return result;
        }
        if (userContactEntity.getUser() != null) {
            result.put("result", false);
            result.put("error", "Такой контакт уже используется");
            return result;
        }
        String code = contact.get("code").replaceAll("\\s", "");
        if (userContactEntity.getCodeTime().plusMinutes(10).isBefore(dateTime)) {
            result.put("result", false);
            result.put("error", "Код подтверждения истёк");
            return result;
        } else if (!code.equals(userContactEntity.getCode())) {
            result.put("result", false);
            result.put("error", "Неправильный код подтверждения");
            return result;
        } else {
            userContactEntity.setCodeTrails((short) 0);
        }
        userContactEntity.setApproved(true);
        userContactService.addContactUserEntity(userContactEntity);
        result.put("result", true);
        return result;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestParam Map<String, String> user, HttpServletRequest request, HttpServletResponse response) {
        if (user.get("name") == null || user.get("phone") == null || user.get("mail") == null) {
            return ResponseEntity.badRequest().build();
        }
        UserContactEntity phoneContact = userContactService.getUserContactEntityByContact(user.get("phone"));
        if (phoneContact == null || !phoneContact.isApproved() || phoneContact.getUser() != null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        UserContactEntity mailContact = userContactService.getUserContactEntityByContact(user.get("mail"));
        if (mailContact == null || !mailContact.isApproved() || mailContact.getUser() != null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        UserEntity userEntity = new UserEntity();
        userEntity.setName(user.get("name"));
        LocalDateTime dateTime = LocalDateTime.now();
        userEntity.setRegTime(dateTime);
        userEntity.setRole("USER");
        String userHash = Stream.of(user.get("phone"), user.get("mail"), user.get("name"), dateTime.toString())
                .map(s -> Integer.toHexString(s.hashCode())).collect(Collectors.joining());
        userEntity.setHash(userHash);
        List<UserContactEntity> userContactEntities = new ArrayList<>();
        phoneContact.setUser(userEntity);
        userContactEntities.add(phoneContact);
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
        Claims claims = new DefaultClaims();
        claims.setSubject(userEntity.getHash());
        claims.put("authorities", userService.getAuthorities(userEntity.getRole()));
        String token = userService.generateUserToken(claims);
        cookie = new Cookie("user_token", token);
        cookie.setHttpOnly(true);
        cookie.setAttribute("SameSite", "Lax");
        cookie.setMaxAge((int) dateTime.until(dateTime.plusMonths(1), ChronoUnit.SECONDS));
        response.addCookie(cookie);
        return ResponseEntity.ok().build();
    }

}
