package com.example.MyBookShopApp.controllers;

import com.example.MyBookShopApp.entities.other.DocumentEntity;
import com.example.MyBookShopApp.services.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class DocumentsPageController {

    private final DocumentService documentService;

    @Autowired
    public DocumentsPageController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping("/documents")
    public String documentsPage(Model model) {
        model.addAttribute("documents", documentService.getAllDocumentEntities());
        return "/documents/index";
    }

    @GetMapping("/documents/{slug}")
    public String selectedDocumentPage(@PathVariable String slug, Model model) {
        DocumentEntity documentEntity = documentService.getDocumentEntityBySlug(slug);
        if (documentEntity == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        model.addAttribute("document", documentEntity);
        return "documents/slug";
    }
}
