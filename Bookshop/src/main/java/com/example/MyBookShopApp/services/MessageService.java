package com.example.MyBookShopApp.services;

import com.example.MyBookShopApp.entities.book.review.MessageEntity;
import com.example.MyBookShopApp.repositories.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageService {

    private final MessageRepository messageRepository;

    @Autowired
    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public void addMessage(MessageEntity messageEntity) {
        messageRepository.save(messageEntity);
    }

}
