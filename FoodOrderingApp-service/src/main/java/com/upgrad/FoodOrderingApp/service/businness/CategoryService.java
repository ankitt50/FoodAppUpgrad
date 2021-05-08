package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.CategoryDao;
import com.upgrad.FoodOrderingApp.service.entity.CategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {
    @Autowired
    CategoryDao categoryDao;

    @Transactional
    public List<CategoryEntity> getAll() {
        return categoryDao.getAll();
    }

    @Transactional
    public CategoryEntity getCategoryById(String uuid) {
        return categoryDao.getCategoryByUuid(uuid);
    }
}
