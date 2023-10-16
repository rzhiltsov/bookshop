package com.example.MyBookShopApp.services;

import com.example.MyBookShopApp.entities.book.file.BookFileEntity;
import com.example.MyBookShopApp.repositories.BookFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookFileService {

    private final BookFileRepository bookFileRepository;

    @Autowired
    public BookFileService(BookFileRepository bookFileRepository) {
        this.bookFileRepository = bookFileRepository;
    }

    public BookFileEntity getBookFileByHash(String hash) {
        return bookFileRepository.findBookFileEntityByHash(hash);
    }
}
