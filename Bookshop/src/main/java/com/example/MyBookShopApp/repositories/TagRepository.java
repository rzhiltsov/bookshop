package com.example.MyBookShopApp.repositories;

import com.example.MyBookShopApp.entities.tag.TagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<TagEntity, Integer> {

    TagEntity findTagEntityBySlug(String slug);
}
