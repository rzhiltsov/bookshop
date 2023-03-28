package skbx.example.struct.payments;

import skbx.example.struct.book.BookEntity;
import jakarta.persistence.*;
import skbx.example.struct.user.UserEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "balance_transaction")
public class BalanceTransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @Column(name = "user_id", columnDefinition = "INT NOT NULL")
    private UserEntity user;

    @Column(columnDefinition = "DATETIME NOT NULL")
    private LocalDateTime time;

    @Column(columnDefinition = "INT NOT NULL DEFAULT 0")
    private int value;

    @ManyToOne
    @Column(name = "book_id", columnDefinition = "INT NOT NULL")
    private BookEntity book;

    @Column(columnDefinition = "TEXT NOT NULL")
    private String description;

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

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public BookEntity getBook() {
        return book;
    }

    public void setBook(BookEntity book) {
        this.book = book;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
