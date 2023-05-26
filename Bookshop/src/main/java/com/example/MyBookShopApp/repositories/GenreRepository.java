package com.example.MyBookShopApp.repositories;

import com.example.MyBookShopApp.entities.genre.GenreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GenreRepository extends JpaRepository<GenreEntity, Integer> {

    GenreEntity findGenreEntityBySlug(String slug);

    @Query("FROM GenreEntity WHERE parent = null")
    List<GenreEntity> findRootGenres();

}
