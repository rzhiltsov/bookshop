package com.example.MyBookShopApp.services;

import com.example.MyBookShopApp.dto.Transaction;
import com.example.MyBookShopApp.entities.payments.BalanceTransactionEntity;
import com.example.MyBookShopApp.entities.user.UserEntity;
import com.example.MyBookShopApp.repositories.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Service
public class TransactionService {

    private final String robokassaShopId;
    private final String robokassaFirstPassword;
    private final String robokassaSecondPassword;
    private final TransactionRepository transactionRepository;
    private final UserService userService;

    @Autowired
    public TransactionService(@Value("${robokassa.shop-id}") String robokassaShopId,
                              @Value("${robokassa.first-password}") String robokassaFirstPassword,
                              @Value("${robokassa.second-password}") String robokassaSecondPassword,
                              TransactionRepository transactionRepository, UserService userService) {
        this.robokassaShopId = robokassaShopId;
        this.robokassaFirstPassword = robokassaFirstPassword;
        this.robokassaSecondPassword = robokassaSecondPassword;
        this.transactionRepository = transactionRepository;
        this.userService = userService;
    }

    public Transaction createTransaction(BalanceTransactionEntity transactionEntity) {
        if (transactionEntity == null) return null;
        Transaction transaction = new Transaction();
        transaction.setValue(transactionEntity.getValue());
        transaction.setDescription(transactionEntity.getDescription());
        transaction.setTime(transactionEntity.getTime());
        return transaction;
    }

    public void addTransaction(BalanceTransactionEntity balanceTransactionEntity) {
        transactionRepository.save(balanceTransactionEntity);
    }

    public void addTransactions(List<BalanceTransactionEntity> balanceTransactionEntities) {
        transactionRepository.saveAll(balanceTransactionEntities);
    }

    public String buildTopUpUrl(String sum, String userIp, String userHash, String userMail) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        messageDigest.update((robokassaShopId + ":" + sum + "::" + userIp + ":" + robokassaFirstPassword + ":Shp_user=" + userHash).getBytes());
        String signature = DatatypeConverter.printHexBinary(messageDigest.digest());
        return "https://auth.robokassa.ru/Merchant/Index.aspx" +
                "?MerchantLogin=" + robokassaShopId +
                "&OutSum=" + sum +
                "&UserIp=" + userIp +
                "&Email=" + userMail +
                "&Shp_user=" + userHash +
                "&Description=Пополнение баланса" +
                "&SignatureValue=" + signature +
                "&IsTest=1";
    }

    public boolean validatePayment(String sum, String invoiceId, String userHash, String signature) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        messageDigest.update((sum + ":" + invoiceId + ":" + robokassaSecondPassword + ":Shp_user=" + userHash).getBytes());
        String result = DatatypeConverter.printHexBinary(messageDigest.digest());
        return result.equalsIgnoreCase(signature);
    }

    public List<Transaction> getTransactions(int offset, int limit) {
        String userHash = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity userEntity = userService.getUserEntityByHash(userHash);
        PageRequest pageRequest = PageRequest.of(offset, limit, Sort.by("time").descending().and(Sort.by("id")));
        return transactionRepository.findBalanceTransactionEntitiesByUser(userEntity, pageRequest).stream().map(this::createTransaction).toList();
    }

}
