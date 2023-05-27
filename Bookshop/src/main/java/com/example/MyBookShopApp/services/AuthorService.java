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
        AuthorEntity authorEntity = authorRepository.findAuthorEntityBySlug(slug);
        return authorEntity == null ? new AuthorEntity() : authorEntity;
    }

    public List<String> getAuthorDescription(String slug) {
        AuthorEntity authorEntity = authorRepository.findAuthorEntityBySlug(slug);
        ArrayList<String> description = new ArrayList<>(2);
        if (authorEntity == null) {
            description.add("");
            description.add("");
            return description;
        }
        String[] words = authorEntity.getDescription().split("\\s");
        int charLimit = 160;
        if (authorEntity.getDescription().length() <= charLimit) {
            description.add(authorEntity.getDescription());
            description.add("");
            return description;
        }
        String openedText = "";
        String hiddenText = "";
        for (int i = 1; i <= words.length; i++) {
            String text = Arrays.stream(words).limit(i).collect(Collectors.joining(" "));
            if (text.length() <= charLimit) {
                openedText = text;
            } else {
                hiddenText = Arrays.stream(words).skip(i - 1).collect(Collectors.joining(" "));
                break;
            }
        }
        description.add(openedText);
        description.add(hiddenText);
        return description;
    }
}
