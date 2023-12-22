package com.example.MyBookShopApp.controllers;

import com.example.MyBookShopApp.entities.book.review.MessageEntity;
import com.example.MyBookShopApp.entities.user.UserEntity;
import com.example.MyBookShopApp.services.MessageService;
import com.example.MyBookShopApp.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.Map;

@Controller
public class ContactsPageController {

    private final UserService userService;
    private final MessageService messageService;

    public ContactsPageController(UserService userService, MessageService messageService) {
        this.userService = userService;
        this.messageService = messageService;
    }

    @GetMapping("/contacts")
    public String contactsPage() {
        return "contacts";
    }

    @PostMapping("/message")
    public ResponseEntity<Void> message(@RequestParam Map<String, String> message) {
        MessageEntity messageEntity = new MessageEntity();
        if (SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken) {
            messageEntity.setName(message.get("name"));
            messageEntity.setMail(message.get("mail"));
        } else {
            String userHash = SecurityContextHolder.getContext().getAuthentication().getName();
            UserEntity userEntity = userService.getUserEntityByHash(userHash);
            messageEntity.setName(userEntity.getName());
            String mail = userService.createUser(userEntity).getMail();
            messageEntity.setMail(mail);
            messageEntity.setUser(userEntity);
        }
        messageEntity.setSubject(message.get("subject"));
        messageEntity.setText(message.get("text"));
        messageEntity.setTime(LocalDateTime.now());
        messageService.addMessage(messageEntity);
        return ResponseEntity.ok().build();
    }

}
