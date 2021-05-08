package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.CouponEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.entity.OrderEntity;
import com.upgrad.FoodOrderingApp.service.entity.RestaurantEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class OrderDao {
    @PersistenceContext
    private EntityManager entityManager;

    public List<OrderEntity> getOrdersByRestaurant(RestaurantEntity restaurant) {
        try {
            return entityManager.createNamedQuery("getOrdersByRestaurant", OrderEntity.class).setParameter("restaurant", restaurant).getResultList();
        }
        catch (NoResultException exception) {
            return null;
        }
    }

    public List<OrderEntity> getPastOrdersOfUsers(CustomerEntity customerEntity) {
        try {
            return entityManager.createNamedQuery("getPastOrdersOfUsers", OrderEntity.class).setParameter("customer", customerEntity).getResultList();
        }
        catch (NoResultException exception) {
            return null;
        }
    }

    public CouponEntity getCouponByCouponName(String couponName) {
        try {
            return entityManager.createNamedQuery("getCouponByCouponName", CouponEntity.class).setParameter("couponName", couponName).getSingleResult();
        }
        catch (NoResultException exception) {
            return null;
        }
    }

}
