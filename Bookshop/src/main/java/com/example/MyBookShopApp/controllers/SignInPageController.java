package com.example.MyBookShopApp.controllers;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
public class SignInPageController {

    @ModelAttribute
    public void cartAndKeptAmount(Model model, HttpServletRequest request) {
        Map<String, Set<String>> cookies = Map.of();
        if (request.getCookies() != null) {
            cookies = Stream.of(request.getCookies())
                    .collect(Collectors.toMap(Cookie::getName, cookie -> {
                        if (cookie.getValue().isEmpty()) return Set.of();
                        else if (cookie.getValue().contains("/")) return Set.of(cookie.getValue().split("/"));
                        else return Set.of(cookie.getValue());
                    }));
        }
        model.addAttribute("cartAmount", cookies.getOrDefault("CART", Set.of()).size());
        model.addAttribute("keptAmount", cookies.getOrDefault("KEPT", Set.of()).size());
    }

    @GetMapping("/signin")
    public String signInPage() {
        return "signin";
    }
}
