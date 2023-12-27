package com.example.MyBookShopApp.services;

import com.example.MyBookShopApp.entities.user.UserContactEntity;
import com.example.MyBookShopApp.repositories.UserContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class UserContactService {

    private final String mailUsername;
    private final JavaMailSender mailSender;
    private final UserContactRepository userContactRepository;

    @Autowired
    public UserContactService(@Value("${spring.mail.username}") String mailUsername, JavaMailSender mailSender,
                              UserContactRepository userContactRepository) {
        this.mailUsername = mailUsername;
        this.mailSender = mailSender;
        this.userContactRepository = userContactRepository;
    }

    public UserContactEntity getUserContactEntityByContact(String contact) {
        return userContactRepository.findUserContactEntityByContact(contact);
    }

    public void addContactUserEntity(UserContactEntity userContactEntity) {
        userContactRepository.save(userContactEntity);
    }

    public String generateConfirmationCode() {
        return new Random().ints(6, 0, 10).mapToObj(String::valueOf).collect(Collectors.joining());
    }

    public void sendConfirmationCodeByMail(String to, String code) {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom("Bookshop <" + mailUsername + ">");
        simpleMailMessage.setTo(to);
        simpleMailMessage.setSentDate(Timestamp.valueOf(LocalDateTime.now()));
        simpleMailMessage.setSubject("Подтверждение почтового адреса Bookshop");
        simpleMailMessage.setText("Ваш код подтверждения: " + code);
        mailSender.send(simpleMailMessage);
    }
}
