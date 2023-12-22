package com.example.MyBookShopApp.services;

import com.example.MyBookShopApp.entities.other.FaqEntity;
import com.example.MyBookShopApp.repositories.FaqRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FaqService {

    private final FaqRepository faqRepository;

    @Autowired
    public FaqService(FaqRepository faqRepository) {
        this.faqRepository = faqRepository;
    }

    public List<FaqEntity> getAllFaqEntities() {
        return faqRepository.findAll(Sort.by("sortIndex", "question"));
    }
}
