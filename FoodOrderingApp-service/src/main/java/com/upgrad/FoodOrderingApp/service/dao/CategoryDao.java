package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.CategoryEntity;
import com.upgrad.FoodOrderingApp.service.entity.RestaurantEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class CategoryDao {
    @PersistenceContext
    private EntityManager entityManager;

    public CategoryEntity getCategoryByUuid(String uuid) {
        try {
            return entityManager.createNamedQuery("getCategoryByUuid", CategoryEntity.class).setParameter("uuid", uuid).getSingleResult();
        }
        catch (NoResultException exception) {
            return null;
        }
    }

    public List<CategoryEntity> getAll() {
        try {
            return entityManager.createNamedQuery("getAll", CategoryEntity.class).getResultList();
        }
        catch (NoResultException exception) {
            return null;
        }
    }
}
