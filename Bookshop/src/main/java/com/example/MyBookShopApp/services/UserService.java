package com.example.MyBookShopApp.services;

import com.example.MyBookShopApp.dto.User;
import com.example.MyBookShopApp.entities.book.links.Book2UserEntity;
import com.example.MyBookShopApp.entities.book.links.Book2UserTypeEntity;
import com.example.MyBookShopApp.entities.user.UserContactEntity;
import com.example.MyBookShopApp.entities.user.UserEntity;
import com.example.MyBookShopApp.repositories.Book2UserRepository;
import com.example.MyBookShopApp.repositories.Book2UserTypeRepository;
import com.example.MyBookShopApp.repositories.UserContactRepository;
import com.example.MyBookShopApp.repositories.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService implements UserDetailsService {

    private final String jwtKey;
    private final UserRepository userRepository;
    private final UserContactRepository userContactRepository;
    private final Book2UserRepository book2UserRepository;
    private final Book2UserTypeRepository book2UserTypeRepository;

    @Autowired
    public UserService(@Value("$(jwt.key)") String jwtKey, UserRepository userRepository, UserContactRepository userContactRepository,
                       Book2UserRepository book2UserRepository, Book2UserTypeRepository book2UserTypeRepository) {
        this.jwtKey = jwtKey;
        this.userRepository = userRepository;
        this.userContactRepository = userContactRepository;
        this.book2UserRepository = book2UserRepository;
        this.book2UserTypeRepository = book2UserTypeRepository;
    }

    public User createUser(UserEntity userEntity) {
        if (userEntity == null) return null;
        User user = new User();
        user.setName(userEntity.getName());
        userEntity.getContacts().forEach(userContactEntity -> {
            switch (userContactEntity.getType()) {
                case PHONE -> user.setPhone(userContactEntity.getContact());
                case MAIL -> user.setMail(userContactEntity.getContact());
            }
        });
        user.setBalance(userEntity.getBalance());
        return user;

    }

    @Override
    public UserDetails loadUserByUsername(String userContact) throws UsernameNotFoundException {
        UserContactEntity userContactEntity = userContactRepository.findUserContactEntityByContact(userContact);
        if (userContactEntity == null || userContactEntity.getUser() == null) {
            throw new UsernameNotFoundException("Пользователь не найден.");
        }
        UserEntity userEntity = userContactEntity.getUser();
        return org.springframework.security.core.userdetails.User
                .builder()
                .username(userEntity.getHash())
                .password(userEntity.getPassword())
                .roles(userEntity.getRole())
                .build();
    }

    public void addUser(UserEntity userEntity) {
        userRepository.save(userEntity);
    }

    public UserEntity getUserEntityByHash(String hash) {
        return userRepository.findUserEntityByHash(hash);
    }

    public String generateAnonymousToken(Claims claims) {
        return Jwts
                .builder()
                .setClaims(claims)
                .setExpiration(Timestamp.valueOf(LocalDateTime.now().plusMonths(1)))
                .compact();
    }

    public String generateUserToken(Claims claims) {
        return Jwts
                .builder()
                .setClaims(claims)
                .setExpiration(Timestamp.valueOf(LocalDateTime.now().plusMonths(1)))
                .signWith(SignatureAlgorithm.HS256, jwtKey)
                .compact();
    }

    public Claims extractAnonymousData(String token) {
        try {
            return Jwts
                    .parser()
                    .parseClaimsJwt(token)
                    .getBody();
        } catch (JwtException exception) {
            return null;
        }
    }

    public Claims extractUserData(String token) {
        try {
            return Jwts
                    .parser()
                    .setSigningKey(jwtKey)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException exception) {
            return null;
        }
    }

    public Book2UserEntity getBook2UserByBookIdAndUserId(int bookId, int userId) {
        return book2UserRepository.findBook2UserEntityByBookIdAndUserId(bookId, userId);
    }

    public List<Book2UserEntity> getBook2UsersByUserId(int userId) {
        return book2UserRepository.findBook2UserEntitiesByUserId(userId);
    }

    public void addBook2User(Book2UserEntity book2UserEntity) {
        book2UserRepository.save(book2UserEntity);
    }

    public void deleteBook2User(Book2UserEntity book2UserEntity) {
        book2UserRepository.delete(book2UserEntity);
    }

    public Book2UserTypeEntity getBook2EntityTypeByName(String name) {
        return book2UserTypeRepository.findBook2UserTypeEntityByName(name);
    }

}
