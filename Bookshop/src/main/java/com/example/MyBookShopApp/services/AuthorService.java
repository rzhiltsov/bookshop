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
        List<AuthorEntity> authors = authorRepository.findAll();
        return authors.stream().collect(Collectors.groupingBy(author -> author.toString().substring(0, 1), TreeMap::new,
                Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(AuthorEntity::toString)))));
    }
}
