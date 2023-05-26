package com.example.MyBookShopApp.services;

import com.example.MyBookShopApp.dto.Genre;
import com.example.MyBookShopApp.entities.genre.GenreEntity;
import com.example.MyBookShopApp.repositories.GenreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

@Service
public class GenreService {

    private final GenreRepository genreRepository;

    @Autowired
    public GenreService(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    private Genre createGenre(GenreEntity genreEntity) {
        Genre genre = new Genre();
        genre.setSlug(genreEntity.getSlug());
        genre.setName(genreEntity.getName());
        genre.setChildren(genreEntity.getChildren().stream().map(this::createGenre).toList());
        genre.setBooksCount(genreEntity.getBooks().size() + genre.getChildren().stream().mapToInt(Genre::getBooksCount).sum());
        return genre;
    }

    public Deque<Genre> getCurrentGenrePath(String slug) {
        ArrayDeque<Genre> genres = new ArrayDeque<>();
        GenreEntity genreEntity = genreRepository.findGenreEntityBySlug(slug);
        while (genreEntity != null) {
            Genre genre = new Genre();
            genre.setName(genreEntity.getName());
            genre.setSlug(genreEntity.getSlug());
            genres.push(genre);
            genreEntity = genreEntity.getParent();
        }
        return genres;
    }

    public List<Genre> getGenresTree() {
        return genreRepository.findRootGenres().stream().map(this::createGenre).toList();
    }

}
