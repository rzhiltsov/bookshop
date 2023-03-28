package com.example.MyBookShopApp.services;

import com.example.MyBookShopApp.entities.author.AuthorEntity;
import com.example.MyBookShopApp.repositories.AuthorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class AuthorService {
    private final AuthorRepository authorRepository;

    @Autowired
    public AuthorService(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    public Map<String, List<AuthorEntity>> getAuthorData() {
        List<AuthorEntity> authors = authorRepository.findAll();
        return authors.stream().collect(Collectors.groupingBy(author ->
                author.getName().split("\\s")[1].substring(0, 1), TreeMap::new, Collectors.toList()));
    }
}