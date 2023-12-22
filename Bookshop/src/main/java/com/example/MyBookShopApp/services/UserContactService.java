package com.example.MyBookShopApp.services;

import com.example.MyBookShopApp.entities.user.UserContactEntity;
import com.example.MyBookShopApp.repositories.UserContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserContactService {

    private final UserContactRepository userContactRepository;

    @Autowired
    public UserContactService(UserContactRepository userContactRepository) {
        this.userContactRepository = userContactRepository;
    }

    public UserContactEntity getUserContactEntityByContact(String contact) {
        return userContactRepository.findUserContactEntityByContact(contact);
    }

    public void addContactUserEntity(UserContactEntity userContactEntity) {
        userContactRepository.save(userContactEntity);
    }
}
