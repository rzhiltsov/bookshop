package com.example.MyBookShopApp.services;

import com.example.MyBookShopApp.entities.author.AuthorEntity;
import com.example.MyBookShopApp.repositories.AuthorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuthorService {
    private final AuthorRepository authorRepository;

    @Autowired
    public AuthorService(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    public Map<String, Set<AuthorEntity>> getAuthorsMap() {
        return authorRepository.findAll().stream().collect(Collectors.groupingBy(author -> author.toString().substring(0, 1),
                TreeMap::new, Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(AuthorEntity::toString)))));
    }

    public AuthorEntity getAuthorBySlug(String slug) {
        return authorRepository.findAuthorEntityBySlug(slug);
    }

    public List<String> getAuthorDescription(String slug) {
        AuthorEntity authorEntity = authorRepository.findAuthorEntityBySlug(slug);
        ArrayList<String> description = new ArrayList<>(2);
        if (authorEntity == null) {
            description.add("");
            description.add("");
            return description;
        }
        int charLimit = 250;
        int splitIndex = authorEntity.getDescription().lastIndexOf(" ", charLimit);
        if (authorEntity.getDescription().length() <= charLimit) {
            description.add(authorEntity.getDescription());
            description.add("");
        } else if (splitIndex == -1) {
            description.add(authorEntity.getDescription().substring(0, charLimit));
            description.add(authorEntity.getDescription().substring(charLimit));
        } else {
            description.add(authorEntity.getDescription().substring(0, splitIndex + 1));
            description.add(authorEntity.getDescription().substring(splitIndex + 1));
        }
        return description;
    }

    public List<AuthorEntity> getAuthorsByBookIdOrdered(int bookId) {
        return authorRepository.findAuthorEntitiesByBookIdOrdered(bookId);
    }
}
