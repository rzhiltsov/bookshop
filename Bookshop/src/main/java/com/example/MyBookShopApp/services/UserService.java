package com.example.MyBookShopApp.services;

import com.example.MyBookShopApp.dto.User;
import com.example.MyBookShopApp.entities.book.links.Book2UserEntity;
import com.example.MyBookShopApp.entities.book.links.Book2UserTypeEntity;
import com.example.MyBookShopApp.entities.user.UserEntity;
import com.example.MyBookShopApp.repositories.Book2UserRepository;
import com.example.MyBookShopApp.repositories.Book2UserTypeRepository;
import com.example.MyBookShopApp.repositories.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {

    private final String jwtKey;
    private final UserRepository userRepository;
    private final Book2UserRepository book2UserRepository;
    private final Book2UserTypeRepository book2UserTypeRepository;
    private final RoleHierarchy roleHierarchy;

    @Autowired
    public UserService(@Value("$(jwt.key)") String jwtKey, UserRepository userRepository, Book2UserRepository book2UserRepository,
                       Book2UserTypeRepository book2UserTypeRepository) {
        this.jwtKey = jwtKey;
        this.userRepository = userRepository;
        this.book2UserRepository = book2UserRepository;
        this.book2UserTypeRepository = book2UserTypeRepository;
        this.roleHierarchy = initRoleHierarchy();
    }

    private RoleHierarchy initRoleHierarchy() {
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        roleHierarchy.setHierarchy("ROLE_ADMIN > ROLE_USER");
        return roleHierarchy;
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

    public List<String> getAuthorities(String role) {
        return roleHierarchy.getReachableGrantedAuthorities(List.of(new SimpleGrantedAuthority("ROLE_" + role))).stream()
                .map(GrantedAuthority::getAuthority).toList();
    }

}
