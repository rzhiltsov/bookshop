package com.example.MyBookShopApp.services;

import com.example.MyBookShopApp.entities.other.DocumentEntity;
import com.example.MyBookShopApp.repositories.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;

    @Autowired
    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public List<DocumentEntity> getAllDocumentEntities() {
        return documentRepository.findAll(Sort.by("sortIndex", "title"));
    }

    public DocumentEntity getDocumentEntityBySlug(String slug) {
        return documentRepository.findDocumentEntityBySlug(slug);
    }
}
