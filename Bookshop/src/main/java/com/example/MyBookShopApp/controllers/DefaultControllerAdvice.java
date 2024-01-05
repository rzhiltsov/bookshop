package com.example.MyBookShopApp.controllers;

import com.example.MyBookShopApp.entities.user.UserEntity;
import com.example.MyBookShopApp.services.UserService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.LocaleResolver;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@ControllerAdvice(annotations = Controller.class)
public class DefaultControllerAdvice {

    private final LocaleResolver localeResolver;
    private final UserService userService;

    @Autowired
    public DefaultControllerAdvice(LocaleResolver localeResolver, UserService userService) {
        this.localeResolver = localeResolver;
        this.userService = userService;
    }


    @ModelAttribute
    public void languageInformation(Model model, HttpServletRequest request) {
        model.addAttribute("lang", localeResolver.resolveLocale(request).getLanguage());
    }

    @ModelAttribute
    public void userInformation(Model model, HttpServletRequest request, HttpServletResponse response) {
        if (SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken) {
            Map<String, List<String>> data = new LinkedHashMap<>();
            if (request.getCookies() != null) {
                Cookie token = Stream.of(request.getCookies()).filter(cookie -> cookie.getName().matches("anonymous_token"))
                        .findFirst().orElse(null);
                if (token != null && token.getValue() != null) {
                    Claims claims = userService.extractAnonymousData(token.getValue());
                    if (claims != null) {
                        data = claims.get("data", LinkedHashMap.class);
                    } else {
                        token.setMaxAge(0);
                        response.addCookie(token);
                    }
                }
            }
            request.setAttribute("data", data);
            model.addAttribute("authentication", "anonymous");
            model.addAttribute("keptAmount", data.getOrDefault("KEPT", List.of()).size());
            model.addAttribute("cartAmount", data.getOrDefault("CART", List.of()).size());
        } else {
            String userHash = SecurityContextHolder.getContext().getAuthentication().getName();
            UserEntity userEntity = userService.getUserEntityByHash(userHash);
            model.addAttribute("authentication", "user");
            model.addAttribute("user", userService.createUser(userEntity));
            model.addAttribute("paidAmount", userService.getBook2UsersByUserId(userEntity.getId()).stream()
                    .filter(book2UserEntity -> book2UserEntity.getType().getName().matches("(PAID)|(ARCHIVED)")).count());
            model.addAttribute("keptAmount", userService.getBook2UsersByUserId(userEntity.getId()).stream()
                    .filter(book2UserEntity -> book2UserEntity.getType().getName().equals("KEPT")).count());
            model.addAttribute("cartAmount", userService.getBook2UsersByUserId(userEntity.getId()).stream()
                    .filter(book2UserEntity -> book2UserEntity.getType().getName().equals("CART")).count());
        }
    }

}
