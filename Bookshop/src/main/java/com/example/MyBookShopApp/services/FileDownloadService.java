package com.example.MyBookShopApp.services;

import com.example.MyBookShopApp.entities.book.BookEntity;
import com.example.MyBookShopApp.entities.book.file.FileDownloadEntity;
import com.example.MyBookShopApp.entities.user.UserEntity;
import com.example.MyBookShopApp.repositories.FileDownloadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FileDownloadService {

    private final FileDownloadRepository fileDownloadRepository;

    @Autowired
    public FileDownloadService(FileDownloadRepository fileDownloadRepository) {
        this.fileDownloadRepository = fileDownloadRepository;
    }

    public FileDownloadEntity getFileDownloadByBookIdAndUserId(BookEntity bookEntity, UserEntity userEntity) {
        return fileDownloadRepository.findFileDownloadEntityByBookAndUser(bookEntity, userEntity);
    }

    public void addFileDownload(FileDownloadEntity fileDownloadEntity) {
        fileDownloadRepository.save(fileDownloadEntity);
    }

}
