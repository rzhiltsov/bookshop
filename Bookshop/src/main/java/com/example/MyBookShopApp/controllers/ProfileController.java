package com.example.MyBookShopApp.controllers;

import com.example.MyBookShopApp.dto.Book;
import com.example.MyBookShopApp.entities.book.links.Book2UserEntity;
import com.example.MyBookShopApp.entities.enums.ContactType;
import com.example.MyBookShopApp.entities.user.UserContactEntity;
import com.example.MyBookShopApp.entities.user.UserEntity;
import com.example.MyBookShopApp.services.BookService;
import com.example.MyBookShopApp.services.UserContactService;
import com.example.MyBookShopApp.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
public class ProfileController {

    private final UserService userService;
    private final BookService bookService;
    private final UserContactService userContactService;

    @Autowired
    public ProfileController(UserService userService, BookService bookService, UserContactService userContactService) {
        this.userService = userService;
        this.bookService = bookService;
        this.userContactService = userContactService;
    }

    @GetMapping("/my")
    public String myPage(Model model) {
        String userHash = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity userEntity = userService.getUserEntityByHash(userHash);
        List<Integer> bookIds = userService.getBook2UsersByUserId(userEntity.getId()).stream()
                .filter(book2UserEntity -> book2UserEntity.getType().getName().equals("PAID"))
                .map(Book2UserEntity::getBookId).toList();
        List<Book> books = bookService.getBookEntitiesByIds(bookIds).stream().map(bookService::createBook).toList();
        model.addAttribute("books", books);
        return "my";
    }

    @GetMapping("/my/archive")
    public String myArchivePage(Model model) {
        String userHash = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity userEntity = userService.getUserEntityByHash(userHash);
        List<Integer> bookIds = userService.getBook2UsersByUserId(userEntity.getId()).stream()
                .filter(book2UserEntity -> book2UserEntity.getType().getName().equals("ARCHIVED"))
                .map(Book2UserEntity::getBookId).toList();
        List<Book> books = bookService.getBookEntitiesByIds(bookIds).stream().map(bookService::createBook).toList();
        model.addAttribute("books", books);
        return "myarchive";
    }

    @GetMapping("/profile")
    public String profilePage() {
        return "profile";
    }

    @PostMapping("/editProfile")
    public ResponseEntity<Void> editProfile(@RequestParam Map<String, String> property) {
        if (property.get("name") == null && property.get("contact") == null) {
            return ResponseEntity.badRequest().build();
        }
        String userHash = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity userEntity = userService.getUserEntityByHash(userHash);
        if (property.get("contact") != null) {
            UserContactEntity contactToAdd = userContactService.getUserContactEntityByContact(property.get("contact"));
            if (contactToAdd == null || !contactToAdd.isApproved() || contactToAdd.getUser() != null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            UserContactEntity contactToDelete = switch (contactToAdd.getType()) {
                case PHONE -> userEntity.getContacts().stream()
                        .filter(contact -> contact.getType() == ContactType.PHONE).findFirst().orElse(null);
                case MAIL -> userEntity.getContacts().stream()
                        .filter(contact -> contact.getType() == ContactType.MAIL).findFirst().orElse(null);
            };
            if (contactToDelete != null) {
                userContactService.deleteUserContactEntity(contactToDelete);
            }
            contactToAdd.setUser(userEntity);
            userContactService.addUserContactEntity(contactToAdd);
        }
        if (property.get("name") != null) {
            userEntity.setName(property.get("name"));
            userService.addUser(userEntity);
        }
        return ResponseEntity.ok().build();
    }
}
