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
import com.example.MyBookShopApp.services.VkAuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.json.JsonParseException;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
public class SignInPageController {

    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final BookService bookService;
    private final UserContactService userContactService;
    private final VkAuthService vkAuthService;

    @Autowired
    public SignInPageController(ObjectMapper objectMapper, UserService userService, BookService bookService,
                                UserContactService userContactService, VkAuthService vkAuthService) {
        this.objectMapper = objectMapper;
        this.userService = userService;
        this.bookService = bookService;
        this.userContactService = userContactService;
        this.vkAuthService = vkAuthService;
    }

    @GetMapping("/signin")
    public String signInPage(HttpServletRequest request, HttpServletResponse response, Model model) {
        if (SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken) {
            String host = request.getHeader("Host");
            String referer = request.getHeader("Referer");
            Cookie cookie = new Cookie("loginReferer", referer);
            if (referer != null && referer.contains(host)) {
                cookie.setMaxAge(3600);
            } else {
                cookie.setMaxAge(0);
            }
            response.addCookie(cookie);
            model.addAttribute("vkAuthRef", vkAuthService.buildVkAuthRef());
            return "signin";
        }
        return "redirect:/profile";
    }

    @PostMapping("/login_sendContact")
    @ResponseBody
    public ObjectNode sendContact(@RequestParam Map<String, String> user) {
        if (user.get("contact") == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        ObjectNode result = objectMapper.createObjectNode();
        LocalDateTime dateTime = LocalDateTime.now();
        UserContactEntity userContactEntity = userContactService.getUserContactEntityByContact(user.get("contact"));
        if (userContactEntity == null || !userContactEntity.isApproved() || userContactEntity.getUser() == null) {
            result.put("result", false);
            result.put("error", "Пользователь не найден");
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
        String code = userContactService.generateConfirmationCode();
        userContactEntity.setCode(code);
        userContactEntity.setCodeTrails((short) (userContactEntity.getCodeTrails() + 1));
        userContactEntity.setCodeTime(dateTime);
        if (userContactEntity.getType() == ContactType.PHONE) {
            try {
                userContactService.sendConfirmationCodeByPhone(user.get("contact"), code);
            } catch (RestClientException e) {
                result.put("result", false);
                result.put("error", "Ошибка отправки сообщения");
                return result;
            }
        } else if (userContactEntity.getType() == ContactType.MAIL) {
            try {
                userContactService.sendConfirmationCodeByMail(user.get("contact"), code);
            } catch (MailException e) {
                result.put("result", false);
                result.put("error", "Ошибка отправки письма");
                return result;
            }
        }
        userContactService.addUserContactEntity(userContactEntity);
        result.put("result", true);
        return result;
    }

    @PostMapping("/login_approveContact")
    @ResponseBody
    public ObjectNode approveContact(@RequestParam Map<String, String> user, HttpServletRequest request, HttpServletResponse response) {
        if (user.get("contact") == null || user.get("code") == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        ObjectNode result = objectMapper.createObjectNode();
        LocalDateTime dateTime = LocalDateTime.now();
        UserContactEntity userContactEntity = userContactService.getUserContactEntityByContact(user.get("contact"));
        if (userContactEntity == null || !userContactEntity.isApproved() || userContactEntity.getUser() == null) {
            result.put("result", false);
            result.put("error", "Пользователь не найден");
            return result;
        }
        String code = user.get("code").replaceAll("\\s", "");
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
        userContactService.addUserContactEntity(userContactEntity);
        UserEntity userEntity = userContactEntity.getUser();
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
        claims.setSubject(userEntity.getHash());
        claims.put("authorities", userService.getAuthorities(userEntity.getRole()));
        String token = userService.generateUserToken(claims);
        cookie = new Cookie("user_token", token);
        cookie.setHttpOnly(true);
        cookie.setAttribute("SameSite", "Lax");
        cookie.setMaxAge((int) dateTime.until(dateTime.plusMonths(1), ChronoUnit.SECONDS));
        response.addCookie(cookie);
        result.put("result", true);
        return result;
    }

    @GetMapping("/vk_auth")
    public String vkAuth(@RequestParam String payload, HttpServletRequest request, HttpServletResponse response) {
        Map<String, ?> payloadMap;
        try {
            payloadMap = new JacksonJsonParser().parseMap(payload);
        } catch (JsonParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        if (payloadMap.get("token") == null || payloadMap.get("uuid") == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        Map<String, ?> userInfo = vkAuthService.getUserInfo(payloadMap.get("token").toString(), payloadMap.get("uuid").toString());
        String mail = userInfo.get("email").toString();
        UserContactEntity mailContact = userContactService.getUserContactEntityByContact(mail);
        LocalDateTime dateTime = LocalDateTime.now();
        UserEntity userEntity;
        boolean isUserExists;
        if (mailContact == null || mailContact.getUser() == null) {
            isUserExists = false;
            userEntity = new UserEntity();
            String firstName = userInfo.get("first_name").toString();
            String lastName = userInfo.get("last_name").toString();
            userEntity.setName(firstName + " " + lastName);
            String userHash = Stream.of(mail, firstName, lastName, dateTime.toString())
                    .map(s -> Integer.toHexString(s.hashCode())).collect(Collectors.joining());
            userEntity.setHash(userHash);
            userEntity.setRole("USER");
            userEntity.setRegTime(dateTime);
            if (mailContact == null) {
                mailContact = new UserContactEntity();
                mailContact.setContact(mail);
                mailContact.setType(ContactType.MAIL);
                mailContact.setCode(userContactService.generateConfirmationCode());
                mailContact.setCodeTime(dateTime);
            }
            mailContact.setApproved(true);
            mailContact.setUser(userEntity);
            userService.addUser(userEntity);
            userContactService.addUserContactEntity(mailContact);
        } else {
            isUserExists = true;
            userEntity = mailContact.getUser();
        }
        Map<String, List<String>> data = (LinkedHashMap) request.getAttribute("data");
        data.forEach((key, value) -> {
            Book2UserTypeEntity status;
            if (key.equals("CART")) status = userService.getBook2EntityTypeByName("CART");
            else if (key.equals("KEPT")) status = userService.getBook2EntityTypeByName("KEPT");
            else return;
            value.forEach(slug -> {
                BookEntity bookEntity = bookService.getBookEntityBySlug(slug);
                if (bookEntity == null) return;
                if (isUserExists) {
                    Book2UserEntity book2UserEntity = userService.getBook2UserByBookIdAndUserId(bookEntity.getId(), userEntity.getId());
                    if (book2UserEntity != null) return;
                }
                Book2UserEntity book2UserEntity = new Book2UserEntity();
                book2UserEntity.setBookId(bookEntity.getId());
                book2UserEntity.setUserId(userEntity.getId());
                book2UserEntity.setType(status);
                book2UserEntity.setTime(dateTime);
                userService.addBook2User(book2UserEntity);
            });
        });
        String redirect = null;
        if (request.getCookies() != null) {
            Cookie loginReferer = Arrays.stream(request.getCookies()).filter(cookie -> cookie.getName().equals("loginReferer"))
                    .findFirst().orElse(null);
            if (loginReferer != null) {
                if (loginReferer.getValue() != null) {
                    redirect = loginReferer.getValue();
                }
                loginReferer.setMaxAge(0);
                response.addCookie(loginReferer);
            }
        }
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
        return "redirect:" + (redirect != null ? redirect : "/profile");
    }

}
