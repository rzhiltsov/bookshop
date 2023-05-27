package com.example.MyBookShopApp.repositories;

import com.example.MyBookShopApp.entities.book.BookEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<BookEntity, Integer> {

    @Query("FROM BookEntity WHERE pubDate BETWEEN CAST(?1 AS DATE) AND CAST(?2 AS DATE)")
    List<BookEntity> findBookEntitiesByPubDate(String from, String to, Pageable pageable);

    @Query("FROM BookEntity WHERE title ILIKE %?1% " +
            "UNION " +
            "FROM BookEntity WHERE description ILIKE %?1% " +
            "UNION " +
            "FROM BookEntity b JOIN Book2AuthorEntity b2a ON b.id = b2a.bookId " +
            "JOIN AuthorEntity a ON a.id = b2a.authorId WHERE a.name ILIKE %?1% " +
            "UNION " +
            "FROM BookEntity b JOIN Book2GenreEntity b2g ON b.id = b2g.bookId " +
            "JOIN GenreEntity g ON g.id = b2g.genreId WHERE g.name ILIKE %?1% " +
            "UNION " +
            "FROM BookEntity b JOIN Book2TagEntity b2t ON b.id = b2t.bookId " +
            "JOIN TagEntity t ON t.id = b2t.tagId WHERE t.name ILIKE %?1%")
    List<BookEntity> findBookEntitiesByPattern(String pattern, Pageable pageable);

    @Query("SELECT books FROM TagEntity WHERE slug = ?1")
    public List<BookEntity> findBookEntitiesByTagSlug(String slug, Pageable pageable);

    @Query("SELECT books FROM AuthorEntity WHERE slug = ?1")
    public List<BookEntity> findBookEntitiesByAuthorSlug(String slug, Pageable pageable);
}
