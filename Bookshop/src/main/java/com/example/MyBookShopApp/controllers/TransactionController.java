package com.example.MyBookShopApp.controllers;

import com.example.MyBookShopApp.dto.Book;
import com.example.MyBookShopApp.dto.User;
import com.example.MyBookShopApp.entities.book.BookEntity;
import com.example.MyBookShopApp.entities.book.links.Book2UserEntity;
import com.example.MyBookShopApp.entities.payments.BalanceTransactionEntity;
import com.example.MyBookShopApp.entities.user.UserEntity;
import com.example.MyBookShopApp.services.BookService;
import com.example.MyBookShopApp.services.TransactionService;
import com.example.MyBookShopApp.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;

@Controller
public class TransactionController {

    private final TransactionService transactionService;
    private final BookService bookService;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @Autowired
    public TransactionController(TransactionService transactionService, BookService bookService, UserService userService,
                                 ObjectMapper objectMapper) {
        this.transactionService = transactionService;
        this.bookService = bookService;
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/buy")
    @ResponseBody
    public ObjectNode buy(@RequestParam Map<String, String> purchases) {
        if (purchases.get("bookSlugs") == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        ObjectNode result = objectMapper.createObjectNode();
        if (SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken) {
            result.put("result", false);
            result.put("error", "Для покупки необходимо авторизоваться");
            return result;
        }
        List<BookEntity> books = Arrays.stream(purchases.get("bookSlugs").split(", ")).map(bookService::getBookEntityBySlug)
                .filter(Objects::nonNull).toList();
        String userHash = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity userEntity = userService.getUserEntityByHash(userHash);
        books.forEach(bookEntity -> {
            Book2UserEntity book2UserEntity = userService.getBook2UserByBookIdAndUserId(bookEntity.getId(), userEntity.getId());
            if (book2UserEntity == null || !book2UserEntity.getType().getName().equals("CART")) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
        });
        int totalPrice = books.stream().map(bookService::createBook).mapToInt(Book::getDiscountPrice).sum();
        if (totalPrice > userEntity.getBalance()) {
            result.put("result", false);
            result.put("error", "Недостаточно средств для покупки, пополните баланс");
            return result;
        }
        userEntity.setBalance(userEntity.getBalance() - totalPrice);
        List<BalanceTransactionEntity> transactions = new ArrayList<>();
        List<Book2UserEntity> book2UserEntities = new ArrayList<>();
        books.forEach(bookEntity -> {
            BalanceTransactionEntity transaction = new BalanceTransactionEntity();
            Book book = bookService.createBook(bookEntity);
            transaction.setBook(bookEntity);
            transaction.setUser(userEntity);
            transaction.setDescription("Покупка книги " + bookEntity.getTitle());
            transaction.setValue(-book.getDiscountPrice());
            transaction.setTime(LocalDateTime.now());
            transactions.add(transaction);
            Book2UserEntity book2UserEntity = userService.getBook2UserByBookIdAndUserId(bookEntity.getId(), userEntity.getId());
            book2UserEntity.setType(userService.getBook2EntityTypeByName("PAID"));
            book2UserEntity.setTime(LocalDateTime.now());
            book2UserEntities.add(book2UserEntity);
        });
        transactionService.addTransactions(transactions);
        userService.addBook2Users(book2UserEntities);
        userService.addUser(userEntity);
        result.put("result", true);
        return result;
    }

    @GetMapping("/payment")
    @ResponseBody
    public ObjectNode payment(@RequestParam Map<String, String> payment) throws NoSuchAlgorithmException {
        if (payment.get("sum") == null || payment.get("ip") == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        try {
            if (Integer.parseInt(payment.get("sum")) <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        String userHash = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.createUser(userService.getUserEntityByHash(userHash));
        ObjectNode result = objectMapper.createObjectNode();
        String redirect = transactionService.buildTopUpUrl(payment.get("sum"), payment.get("ip"), userHash, user.getMail());
        result.put("result", true);
        result.put("redirect", redirect);
        return result;
    }

    @PostMapping("/submitPayment")
    @ResponseBody
    public String submitPayment(@RequestParam Map<String, String> payment) throws NoSuchAlgorithmException {
        String invoiceId = payment.get("InvId");
        String userHash = payment.get("Shp_user");
        String signature = payment.get("SignatureValue");
        if (payment.get("OutSum") == null || invoiceId == null || userHash == null || signature == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        int sum;
        try {
            sum = (int) Double.parseDouble(payment.get("OutSum"));
            if (sum <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        if (transactionService.validatePayment(payment.get("OutSum"), invoiceId, userHash, signature)) {
            UserEntity userEntity = userService.getUserEntityByHash(userHash);
            BalanceTransactionEntity transaction = new BalanceTransactionEntity();
            transaction.setValue(sum);
            transaction.setUser(userEntity);
            transaction.setTime(LocalDateTime.now());
            transaction.setDescription("Пополнение баланса");
            userEntity.setBalance(userEntity.getBalance() + sum);
            transactionService.addTransaction(transaction);
            userService.addUser(userEntity);
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return "OK" + invoiceId;
    }

    @GetMapping("/successPayment")
    public String successPayment() {
        return "redirect:/profile#transactions";
    }

    @GetMapping("/failPayment")
    public String failPayment() {
        return "redirect:/profile#topup";
    }

    @GetMapping("/transactions")
    @ResponseBody
    public ObjectNode transactions(@RequestParam int offset, @RequestParam int limit) {
        ObjectNode data = objectMapper.createObjectNode();
        ArrayNode transactions = data.putArray("transactions");
        transactionService.getTransactions(offset, limit).forEach(transactions::addPOJO);
        return data;
    }
}
