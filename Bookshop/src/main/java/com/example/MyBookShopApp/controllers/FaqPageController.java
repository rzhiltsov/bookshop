package com.example.MyBookShopApp.controllers;

import com.example.MyBookShopApp.services.FaqService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FaqPageController {

    private final FaqService faqService;

    @Autowired
    public FaqPageController(FaqService faqService) {
        this.faqService = faqService;
    }

    @GetMapping("/faq")
    public String faqPage(Model model) {
        model.addAttribute("faqs", faqService.getAllFaqEntities());
        return "faq";
    }
}
