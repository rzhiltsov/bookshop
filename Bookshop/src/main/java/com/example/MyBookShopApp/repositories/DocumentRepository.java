package com.example.MyBookShopApp.repositories;

import com.example.MyBookShopApp.entities.other.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends JpaRepository<DocumentEntity, Integer> {

    DocumentEntity findDocumentEntityBySlug(String slug);
}
