package com.upgrad.FoodOrderingApp.api.controller;


import com.upgrad.FoodOrderingApp.api.model.*;
import com.upgrad.FoodOrderingApp.service.businness.CustomerBusinessService;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.exception.AuthenticationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SignUpRestrictedException;
import com.upgrad.FoodOrderingApp.service.exception.UpdateCustomerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;

@RestController
@RequestMapping(path = "/api")
public class CustomerController {


    @Autowired
    private CustomerBusinessService customerBusinessService;


    @RequestMapping(method = RequestMethod.POST, path = "/customer/signup", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SignupCustomerResponse> customerSignup(final SignupCustomerRequest signupCustomerRequest) throws SignUpRestrictedException {

        final CustomerEntity newCustomer = new CustomerEntity();


        CustomerEntity existingCustomerEntity = customerBusinessService.getUserByContactNumber(signupCustomerRequest.getContactNumber());

        if (existingCustomerEntity != null) {
            throw new SignUpRestrictedException("SGR-001", "This contact number is already registered! Try other contact number.");
        }

        boolean emptyFieldFound = false;

        if (signupCustomerRequest.getFirstName() != null) {
            newCustomer.setFirstName(signupCustomerRequest.getFirstName());
        }
        else {
            emptyFieldFound = true;
        }

        if (signupCustomerRequest.getLastName() != null) {
            newCustomer.setLastName(signupCustomerRequest.getLastName());
        }
        else {
            newCustomer.setLastName("");
        }

        if(signupCustomerRequest.getEmailAddress() != null) {
            newCustomer.setEmail(signupCustomerRequest.getEmailAddress());
        }
        else {
            emptyFieldFound = true;
        }

        if(signupCustomerRequest.getPassword() != null) {
            newCustomer.setPassword(signupCustomerRequest.getPassword());
        }
        else {
            emptyFieldFound = true;
        }

        if(signupCustomerRequest.getContactNumber() != null) {
            newCustomer.setContactNumber(signupCustomerRequest.getContactNumber());
        }
        else {
            emptyFieldFound = true;
        }

        if (emptyFieldFound) {
            throw new SignUpRestrictedException("SGR-005", "Except last name all fields should be filled");
        }

        if(!customerBusinessService.isEmailValid(signupCustomerRequest.getEmailAddress())) {
            throw new SignUpRestrictedException("SGR-002", "Invalid email-id format!");
        }
        if (!customerBusinessService.isContactNumberValid(signupCustomerRequest.getContactNumber())) {
            throw new SignUpRestrictedException("SGR-003", "Invalid contact number!");
        }
        if(!customerBusinessService.isPasswordValid(signupCustomerRequest.getPassword())) {
            throw new SignUpRestrictedException("SGR-004", "Weak password!");
        }

            CustomerEntity savedCustomer = customerBusinessService.customerSignup(newCustomer);

        SignupCustomerResponse signupCustomerResponse = new SignupCustomerResponse().id(savedCustomer.getUuid()).status("CUSTOMER SUCCESSFULLY REGISTERED");
        return new ResponseEntity<SignupCustomerResponse>(signupCustomerResponse, HttpStatus.CREATED);

    }


    @PostMapping(path = "/customer/login", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<LoginResponse> customerLogin(@RequestHeader(name = "authorization") final String authorization)
            throws AuthenticationFailedException {

        String encodedString = getSignInToken(authorization); // extract base64 encoded username and password

        String contactNumber = "", password = "";

        try {
        byte[] array = Base64.getDecoder().decode(encodedString); // decode username and password
        String usrPsw = new String(array);
        String[] usrPswArray = usrPsw.split(":"); // split username and password
            contactNumber = usrPswArray[0];
            password = usrPswArray[1];
        } catch (Exception ex) {
            throw new AuthenticationFailedException("ATH-003", "Incorrect format of decoded customer name and password"); // throw error if authentication String is incorrect
        }

        CustomerEntity customerWithContactNumber = customerBusinessService.getUserByContactNumber(contactNumber);
        if (customerWithContactNumber == null) {
            throw new AuthenticationFailedException("ATH-001","This contact number has not been registered!"); // throw error if contactNumber does not exist
        }
        else {
            if (customerBusinessService.isPasswordCorrect(password, customerWithContactNumber)) {
                CustomerAuthEntity authEntity = new CustomerAuthEntity();
//                authEntity.setUuid(customerWithContactNumber.getUuid());
                authEntity.setCustomer(customerWithContactNumber);
                CustomerAuthEntity customerAuthEntity = customerBusinessService.saveLoginInfo(authEntity, password);
                CustomerEntity customerEntity = customerAuthEntity.getCustomer();


                HttpHeaders headers = new HttpHeaders();
                headers.add("access-token","Bearer "+customerAuthEntity.getAccessToken()); // add JWT token in header and return

                return new ResponseEntity<LoginResponse>(new LoginResponse().id(customerEntity.getUuid())
                        .message("LOGGED IN SUCCESSFULLY")
                        .firstName(customerEntity.getFirstName())
                        .lastName(customerEntity.getLastName())
                        .emailAddress(customerEntity.getEmail())
                        .contactNumber(customerEntity.getContactNumber()),
                        headers,
                        HttpStatus.OK);
            }
            else {
                throw new AuthenticationFailedException("ATH-002","Invalid Credentials"); // throw error if password is wrong
            }
        }
    }

    // logout customer and update log out time in DB
    @PostMapping(path = "/customer/logout", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<LogoutResponse> customerLogout(@RequestHeader(name = "authorization") final String authToken)
            throws AuthorizationFailedException {

        String token = getToken(authToken);
        CustomerEntity customerEntity =  customerBusinessService.checkAuthToken(token, "/customer/logout");
        return new ResponseEntity<LogoutResponse>(new LogoutResponse().id(customerEntity.getUuid()).message("LOGGED OUT SUCCESSFULLY"), HttpStatus.OK);
    }

    // to update customer details
    @PutMapping(path = "/customer", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UpdateCustomerResponse> updateCustomerDetails(@RequestHeader(name = "authorization") final String authToken, final UpdateCustomerRequest updateCustomerRequest)
            throws UpdateCustomerException, AuthorizationFailedException {

        if(updateCustomerRequest.getFirstName() != null) {
            String token = getToken(authToken);
            CustomerEntity customerEntity = customerBusinessService.checkAuthToken(token, "/customer");
            CustomerEntity updatedCustomerEntity = customerBusinessService.updateCustomerDetails(customerEntity, updateCustomerRequest.getFirstName(), updateCustomerRequest.getLastName());
            return new ResponseEntity<UpdateCustomerResponse>(new UpdateCustomerResponse()
                    .id(updatedCustomerEntity.getUuid())
                    .firstName(updatedCustomerEntity.getFirstName())
                    .lastName(updatedCustomerEntity.getLastName())
                    .status("CUSTOMER DETAILS UPDATED SUCCESSFULLY")
                    , HttpStatus.OK);

        }
        else {
            throw new UpdateCustomerException("UCR-002","First name field should not be empty"); // throw error if first name field is empty
        }

    }

    @PutMapping(path = "/customer/password", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UpdatePasswordResponse> updateCustomerPassword(@RequestHeader(name = "authorization") final String authToken, final UpdatePasswordRequest updatePasswordRequest ) throws AuthorizationFailedException, UpdateCustomerException {

        if(updatePasswordRequest.getNewPassword() == null || updatePasswordRequest.getOldPassword() == null) {
            throw new UpdateCustomerException("UCR-003","No field should be empty");
        }

        String token = getToken(authToken);
        CustomerEntity customerEntity = customerBusinessService.checkAuthToken(token, "/customer/password");

        if(!customerBusinessService.isPasswordValid(updatePasswordRequest.getNewPassword())) {
            throw new UpdateCustomerException("UCR-001","Weak password!");
        }

        if(!customerBusinessService.isPasswordCorrect(updatePasswordRequest.getOldPassword(), customerEntity)) {
            throw new UpdateCustomerException("UCR--004","Incorrect old password!");
        }

        CustomerEntity updatedCustomerEntity = customerBusinessService.updateCustomerPassword(customerEntity, updatePasswordRequest.getNewPassword());

        return new ResponseEntity<UpdatePasswordResponse>(new UpdatePasswordResponse().id(updatedCustomerEntity.getUuid()).status("CUSTOMER PASSWORD UPDATED SUCCESSFULLY"), HttpStatus.OK);

    }

    // this method extracts the token from the base64 encoded authentication String
    private String getSignInToken(String authToken) {
        String token;
        if (authToken.startsWith("Basic ")) {
            String [] basicToken = authToken.split("Basic ");
            token = basicToken[1];
        } else {
            token = authToken;
        }
        return token;
    }

    // this method extracts the token from the JWT token string sent in the Request Header
    private String getToken(String authToken) {
        String token;
        if (authToken.startsWith("Bearer ")) {
            String [] bearerToken = authToken.split("Bearer ");
            token = bearerToken[1];
        } else {
            token = authToken;
        }
        return token;
    }


}

