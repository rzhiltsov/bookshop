package com.example.MyBookShopApp.entities.user;

import com.example.MyBookShopApp.entities.enums.ContactType;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_contact")
public class UserContactEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne
    @JoinColumn(name = "user_id", columnDefinition = "INT")
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContactType type;

    @Column(columnDefinition = "BOOLEAN NOT NULL")
    private boolean approved;

    @Column(columnDefinition = "VARCHAR(255)")
    private String code;

    @Column(columnDefinition = "SMALLINT NOT NULL DEFAULT 0")
    private short codeTrails;

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime codeTime;

    @Column(columnDefinition = "VARCHAR(255) NOT NULL", unique = true)
    private String contact;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public ContactType getType() {
        return type;
    }

    public void setType(ContactType type) {
        this.type = type;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public short getCodeTrails() {
        return codeTrails;
    }

    public void setCodeTrails(short codeTrails) {
        this.codeTrails = codeTrails;
    }

    public LocalDateTime getCodeTime() {
        return codeTime;
    }

    public void setCodeTime(LocalDateTime codeTime) {
        this.codeTime = codeTime;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }
}
