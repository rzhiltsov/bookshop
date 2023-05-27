package com.example.MyBookShopApp.repositories;

import com.example.MyBookShopApp.entities.author.AuthorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuthorRepository extends JpaRepository<AuthorEntity, Integer> {

    @Query("FROM AuthorEntity a JOIN Book2AuthorEntity b2a ON a.id = b2a.authorId " +
            "WHERE b2a.bookId = ?1 ORDER BY b2a.sortIndex")
    List<AuthorEntity> findAuthorEntitiesByBookId(int bookId);

    AuthorEntity findAuthorEntityBySlug(String slug);
}
