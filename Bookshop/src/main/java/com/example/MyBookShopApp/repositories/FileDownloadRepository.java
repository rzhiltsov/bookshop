package com.example.MyBookShopApp.repositories;

import com.example.MyBookShopApp.entities.book.BookEntity;
import com.example.MyBookShopApp.entities.book.file.FileDownloadEntity;
import com.example.MyBookShopApp.entities.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileDownloadRepository extends JpaRepository<FileDownloadEntity, Integer> {

    FileDownloadEntity findFileDownloadEntityByBookAndUser(BookEntity bookEntity, UserEntity userEntity);
}
