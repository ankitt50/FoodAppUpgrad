package com.upgrad.FoodOrderingApp.service.businness;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.regex.*;
import com.upgrad.FoodOrderingApp.service.dao.CustomerDao;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CustomerBusinessService {

    @Autowired
    private PasswordCryptographyProvider cryptographyProvider;

    @Autowired
    private CustomerDao customerDao;

    @Transactional
    public CustomerEntity customerSignup(CustomerEntity newCustomer) {


        String[] encryptedPasswordAndSalt = cryptographyProvider.encrypt(newCustomer.getPassword());
        newCustomer.setSalt(encryptedPasswordAndSalt[0]);
        newCustomer.setPassword(encryptedPasswordAndSalt[1]);
        newCustomer.setUuid(UUID.randomUUID().toString());

        return customerDao.customerSignup(newCustomer);
    }

    @Transactional
    public CustomerEntity getUserByContactNumber(String contactNumber) {
        if(contactNumber==null || contactNumber.equals("")) {
            return null;
        }
        CustomerEntity customerEntity =  customerDao.getUserByContactNumber(contactNumber) ;
        return customerEntity;
    }

    public boolean isPasswordCorrect(String password, CustomerEntity customer) {
        String encryptedPassword = cryptographyProvider.encrypt(password, customer.getSalt());
        if (customer.getPassword().equals(encryptedPassword)) {
            return true;
        }
        else {
            return false;
        }
    }

    public boolean isEmailValid(String email) {
        String[] splitEmailFirstTime = email.split("@");
        try {
            if (splitEmailFirstTime[1].length() > 0) {
                String[] splitEmailAgain = splitEmailFirstTime[1].split("\\.");
                if (splitEmailAgain[1].length() > 0) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
        catch (Exception e){
            return false;
        }
    }

    public boolean isContactNumberValid(String contactNumber) {
        if(contactNumber.length()!=10) {
            return false;
        }
        else {
            char[] charsInContactNumber = contactNumber.toCharArray();
            boolean notPureNumber = false;
            for (char c: charsInContactNumber) {
                if (c > 57 || c<48) {
                    notPureNumber = true;
                }
            }
            if (notPureNumber) {
                return false;
            }
            else {
                return true;
            }
        }
    }

    public boolean isPasswordValid(String password) {
        if (password.length()<8) {
            return false;
        }

        // Regex to check valid password.
        String regex = "^(?=.*[0-9])"
                + "(?=.*[A-Z])"
                + "(?=.*[#@$%&*!^]).{8,}$";

        // Compile the ReGex
        Pattern p = Pattern.compile(regex);


        // Pattern class contains matcher() method
        // to find matching between given password
        // and regular expression.
        Matcher m = p.matcher(password);

        // Return if the password
        // matched the ReGex
        return m.matches();



    }

    @Transactional
    public CustomerAuthEntity saveLoginInfo(CustomerAuthEntity customerAuthEntity, String password) {
        JwtTokenProvider tokenProvider = new JwtTokenProvider(password);
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime expiryTime = currentTime.plusHours(8);

        ZonedDateTime currentZonedTime = ZonedDateTime.now();
        ZonedDateTime expiryZonedTime = currentZonedTime.plusHours(8);

        String authToken = tokenProvider.generateToken(customerAuthEntity.getUuid(),currentZonedTime, expiryZonedTime);

        customerAuthEntity.setAccessToken(authToken);
        customerAuthEntity.setExpiryTime(expiryTime);
        customerAuthEntity.setLoginTime(currentTime);
        customerAuthEntity.setUuid(UUID.randomUUID().toString());

        return customerDao.saveLoginInfo(customerAuthEntity);
    }


    // This method checks existing auth token, and then signs out the user if the auth token is found. If the auth token is not found, it displays an error message.
    @Transactional
    public CustomerEntity checkAuthToken(String authToken, String endpoint) throws AuthorizationFailedException {


        CustomerAuthEntity customerAuthEntity = customerDao.checkAuthToken(authToken);
        if (customerAuthEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "Customer is not Logged in.");
        }
        else if (customerAuthEntity.getLogoutTime() != null) {
            throw new AuthorizationFailedException("ATHR-002", "Customer is logged out. Log in again to access this endpoint.");
        }
        else if (customerAuthEntity.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new AuthorizationFailedException("ATHR-003", "Your session is expired. Log in again to access this endpoint.");
        }
        else {
            if (endpoint.equals("/customer/logout")) {
                return customerDao.signOutCustomer(customerAuthEntity);
            }
            else {
                return customerAuthEntity.getCustomer();
            }

        }
    }

    @Transactional
    public CustomerEntity updateCustomerDetails(CustomerEntity customerEntity, String firstName, String lastName) {
        if (lastName == null) {
            customerEntity.setLastName("");
        }
        else {
            customerEntity.setLastName(lastName);
        }

        customerEntity.setFirstName(firstName);

        return customerDao.updateCustomerDetails(customerEntity);
    }

    @Transactional
    public CustomerEntity updateCustomerPassword(CustomerEntity customerEntity, String password) {
        String[] encryptedPasswordAndSalt = cryptographyProvider.encrypt(customerEntity.getPassword());
        customerEntity.setSalt(encryptedPasswordAndSalt[0]);
        customerEntity.setPassword(encryptedPasswordAndSalt[1]);
        return customerDao.updateCustomerDetails(customerEntity);
    }
}
