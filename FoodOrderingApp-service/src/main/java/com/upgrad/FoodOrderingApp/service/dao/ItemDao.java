package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.CouponEntity;
import com.upgrad.FoodOrderingApp.service.entity.ItemEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

@Repository
public class ItemDao {


    @PersistenceContext
    private EntityManager entityManager;


    public ItemEntity getItemByUuid(String uuid) {
        try {
            return entityManager.createNamedQuery("getItemByUuid", ItemEntity.class).setParameter("uuid", uuid).getSingleResult();
        }
        catch (NoResultException exception) {
            return null;
        }
    }

}
