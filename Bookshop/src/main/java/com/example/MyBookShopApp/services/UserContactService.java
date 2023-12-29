package com.example.MyBookShopApp.services;

import com.example.MyBookShopApp.entities.user.UserContactEntity;
import com.example.MyBookShopApp.repositories.UserContactRepository;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class UserContactService {

    private final String phoneId;
    private final String phoneToken;
    private final String mailUsername;
    private final JavaMailSender mailSender;
    private final UserContactRepository userContactRepository;

    @Autowired
    public UserContactService(@Value("${phone.id}") String phoneId, @Value("${phone.token}") String phoneToken,
                              @Value("${spring.mail.username}") String mailUsername, JavaMailSender mailSender,
                              UserContactRepository userContactRepository) {
        this.phoneId = phoneId;
        this.phoneToken = phoneToken;
        this.mailUsername = mailUsername;
        this.mailSender = mailSender;
        this.userContactRepository = userContactRepository;
    }

    public UserContactEntity getUserContactEntityByContact(String contact) {
        return userContactRepository.findUserContactEntityByContact(contact);
    }

    public void addUserContactEntity(UserContactEntity userContactEntity) {
        userContactRepository.save(userContactEntity);
    }

    public void deleteUserContactEntity(UserContactEntity userContactEntity) {
        userContactRepository.delete(userContactEntity);
    }

    public String generateConfirmationCode() {
        return new Random().ints(6, 0, 10).mapToObj(String::valueOf).collect(Collectors.joining());
    }

    public void sendConfirmationCodeByPhone(String to, String code) {
        to = to.replaceAll("[ ()-]", "");
        String url = "https://wa-sms.com/api/send/sms";
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("device", phoneId);
        data.add("secret", phoneToken);
        data.add("mode", "devices");
        data.add("phone", to);
        data.add("message", "Ваш код подтверждения: " + code);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(data, headers);
        ResponseEntity<ObjectNode> response = new RestTemplate().postForEntity(url, request, ObjectNode.class);
        if (response.getBody().get("status").asInt() != 200) {
            throw new RestClientException("Ошибка отправки SMS");
        }
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
