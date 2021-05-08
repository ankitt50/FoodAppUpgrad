package com.upgrad.FoodOrderingApp.service.dao;


import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;

@Repository
public class CustomerDao {


    @PersistenceContext
    private EntityManager entityManager;

    public CustomerEntity customerSignup(CustomerEntity newCustomer) {
        entityManager.persist(newCustomer);
        return newCustomer;
    }

    public CustomerEntity getUserByContactNumber(String contactNumber) {

        try {
            return entityManager.createNamedQuery("ContactNumber", CustomerEntity.class).
                    setParameter("contactNumber", contactNumber).getSingleResult();
        }
        catch (NoResultException exception) {
            return null;
        }

    }

    public CustomerAuthEntity saveLoginInfo(CustomerAuthEntity customerAuthEntity) {
        entityManager.persist(customerAuthEntity);
        return customerAuthEntity;
    }

    public CustomerAuthEntity checkAuthToken(String authToken) {
        try {
            return entityManager.createNamedQuery("CheckAuthToken", CustomerAuthEntity.class).
                    setParameter("accessToken", authToken).getSingleResult();
        }
        catch (NoResultException exc) {
            return null;
        }
    }

    public CustomerEntity signOutCustomer(CustomerAuthEntity customerAuthEntity) {
        LocalDateTime logoutTime = LocalDateTime.now();
        customerAuthEntity.setLogoutTime(logoutTime);
        entityManager.persist(customerAuthEntity);
        return customerAuthEntity.getCustomer();
    }

    public CustomerEntity updateCustomerDetails(CustomerEntity customerEntity) {
        entityManager.merge(customerEntity);
        return customerEntity;
    }

}
