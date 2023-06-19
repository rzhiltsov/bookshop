package com.example.MyBookShopApp.services;

import com.example.MyBookShopApp.entities.tag.TagEntity;
import com.example.MyBookShopApp.repositories.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TagService {

    private final TagRepository tagRepository;

    @Autowired
    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    public Map<TagEntity, String> getAllTags() {
        return tagRepository.findAll().stream().collect(Collectors.toMap(Function.identity(), tagEntity -> {
            int size = tagEntity.getBooks().size();
            if (size < 9) return "Tag Tag_xs";
            else if (size < 12) return "Tag Tag_sm";
            else if (size < 15) return "Tag";
            else if (size < 18) return "Tag Tag_md";
            else return "Tag Tag_lg";
        }, (v1, v2) -> "", () -> new TreeMap<>(Comparator.comparing(TagEntity::getName))));
    }

    public String getTagName(String slug) {
        TagEntity tagEntity = tagRepository.findTagEntityBySlug(slug);
        if (tagEntity == null) return null;
        else return tagEntity.getName();
    }
}
