package com.upgrad.FoodOrderingApp.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.upgrad.FoodOrderingApp.api.model.AddressList;
import com.upgrad.FoodOrderingApp.api.model.AddressListResponse;
import com.upgrad.FoodOrderingApp.api.model.StatesList;
import com.upgrad.FoodOrderingApp.api.model.StatesListResponse;
import com.upgrad.FoodOrderingApp.service.businness.AddressBusinessService;
//import com.upgrad.FoodOrderingApp.service.businness.AddressService;
import com.upgrad.FoodOrderingApp.service.businness.CustomerBusinessService;
//import com.upgrad.FoodOrderingApp.service.businness.CustomerService;
import com.upgrad.FoodOrderingApp.service.entity.AddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.entity.StateEntity;
import com.upgrad.FoodOrderingApp.service.exception.AddressNotFoundException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SaveAddressException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// This class contains all the test cases regarding the address controller
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class AddressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AddressBusinessService mockAddressService;

    @MockBean
    private CustomerBusinessService mockCustomerService;

    // ------------------------------------------ POST /address ------------------------------------------

    //This test case passes when the address is successfully saved.
    @Test
    public void shouldSaveAddress() throws Exception {
        when(mockCustomerService.checkAuthToken(anyString(),anyString())).thenReturn(new CustomerEntity());
        when(mockAddressService.getStateByUuid(anyString())).thenReturn(new StateEntity());

        final AddressEntity addressEntity = new AddressEntity();
        addressEntity.setUuid("randomUuid001");
        when(mockAddressService.saveAddress(any(), any())).thenReturn(addressEntity);
        when(mockAddressService.isPincodeValid(anyString())).thenReturn(true);

        mockMvc
                .perform(post("/address")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .header("authorization", "Bearer database_accesstoken2")
                        .content("{\"flat_building_name\":\"xyz\", \"locality\":\"abc\", \"city\":\"pqr\", \"pincode\":\"282005\", \"state_uuid\":\"testUUID\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").value("randomUuid001"))
                .andExpect(jsonPath("status").value("ADDRESS SUCCESSFULLY REGISTERED"));
        verify(mockCustomerService, times(1)).checkAuthToken(anyString(),anyString());
        verify(mockAddressService, times(1)).getStateByUuid(anyString());
        verify(mockAddressService, times(1)).saveAddress(any(), any());
    }
//
//    //This test case passes when you have handled the exception of trying to save an address with non existing access-token.
    @Test
    public void shouldNotSaveAddressWithNonExistingAccessToken() throws Exception {
        when(mockCustomerService.checkAuthToken(anyString(),anyString()))
                .thenThrow(new AuthorizationFailedException("ATHR-001", "Customer is not Logged in."));

        mockMvc
                .perform(post("/address")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .header("authorization", "Bearer non_existing_access_token").
                                content("{\"flat_building_name\":\"xyz\", \"locality\":\"abc\", \"city\":\"pqr\", \"pincode\":\"282005\", \"state_uuid\":\"testUUID\"}")
                ).andExpect(status().isForbidden())
                .andExpect(jsonPath("code").value("ATHR-001"));
        verify(mockCustomerService, times(1)).checkAuthToken(anyString(),anyString());
    }
//
//    //This test case passes when you have handled the exception of trying to save an address with signed out user.
    @Test
    public void shouldNotSaveAddressWithSignedOutUser() throws Exception {
        when(mockCustomerService.checkAuthToken(anyString(),anyString()))
                .thenThrow(new AuthorizationFailedException("ATHR-002", "Customer is logged out. Log in again to access this endpoint."));

        mockMvc
                .perform(post("/address")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .header("authorization", "Bearer database_accesstoken")
                        .content("{\"flat_building_name\":\"xyz\", \"locality\":\"abc\", \"city\":\"pqr\", \"pincode\":\"282005\", \"state_uuid\":\"testUUID\"}")
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("code").value("ATHR-002"));
        verify(mockCustomerService, times(1)).checkAuthToken(anyString(),anyString());
    }
//
//    //This test case passes when you have handled the exception of trying to save an address with user whose session is
//    // expired.
    @Test
    public void shouldNotSaveAddressWithExpiredSessionUser() throws Exception {
        when(mockCustomerService.checkAuthToken(anyString(),anyString()))
                .thenThrow(new AuthorizationFailedException("ATHR-003", "Your session is expired. Log in again to access this endpoint."));

        mockMvc
                .perform(post("/address")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .header("authorization", "Bearer database_accesstoken1")
                        .content("{\"flat_building_name\":\"xyz\", \"locality\":\"abc\", \"city\":\"pqr\", \"pincode\":\"282005\", \"state_uuid\":\"testUUID\"}")
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("code").value("ATHR-003"));
        verify(mockCustomerService, times(1)).checkAuthToken(anyString(),anyString());
    }
//
//    //This test case passes when you have handled the exception of trying to save an address with incorrect state uuid.
    @Test
    public void shouldNotSaveAddressWithIncorrectStateId() throws Exception {
        when(mockCustomerService.checkAuthToken(anyString(),anyString())).thenReturn(new CustomerEntity());
        when(mockAddressService.isPincodeValid(anyString())).thenReturn(true);
        when(mockAddressService.getStateByUuid(anyString())).thenReturn(null);

        mockMvc
                .perform(post("/address")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .header("authorization", "Bearer database_accesstoken2")
                        .content("{\"flat_building_name\":\"xyz\", \"locality\":\"abc\", \"city\":\"pqr\", \"pincode\":\"100000\", \"state_uuid\":\"testUUID\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("code").value("ANF-002"));
        verify(mockCustomerService, times(1)).checkAuthToken(anyString(),anyString());
        verify(mockAddressService, times(1)).getStateByUuid(anyString());
        verify(mockAddressService, times(0)).saveAddress(any(), any());
    }
//
//    //This test case passes when you have handled the exception of trying to save an address with empty address field.
    @Test
    public void shouldNotSaveAddressWithEmptyAddressField() throws Exception {
        when(mockCustomerService.checkAuthToken(anyString(),anyString())).thenReturn(new CustomerEntity());
        when(mockAddressService.getStateByUuid(anyString())).thenReturn(new StateEntity());

        mockMvc
                .perform(post("/address")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .header("authorization", "Bearer database_accesstoken2")
                        .content("{\"flat_building_name\":\"xyz\", \"locality\":\"abc\", \"city\":\"\", \"pincode\":\"123456\", \"state_uuid\":\"testUUID\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("code").value("SAR-001"));
        verify(mockCustomerService, times(1)).checkAuthToken(anyString(),anyString());
        verify(mockAddressService, times(1)).getStateByUuid(anyString());
        verify(mockAddressService, times(0)).saveAddress(any(), any());
    }
//
//    //This test case passes when you have handled the exception of trying to save an address with incorrect pincode.
    @Test
    public void shouldNotSaveAddressWithEmptyWrongPinCode() throws Exception {
        when(mockCustomerService.checkAuthToken(anyString(),anyString())).thenReturn(new CustomerEntity());
        when(mockAddressService.getStateByUuid(anyString())).thenReturn(new StateEntity());

        mockMvc
                .perform(post("/address")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .header("authorization", "Bearer database_accesstoken2")
                        .content("{\"flat_building_name\":\"xyz\", \"locality\":\"abc\", \"city\":\"pqr\", \"pincode\":\"\", \"state_uuid\":\"testUUID\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("code").value("SAR-001"));
        verify(mockCustomerService, times(1)).checkAuthToken(anyString(),anyString());
        verify(mockAddressService, times(1)).getStateByUuid(anyString());
        verify(mockAddressService, times(0)).saveAddress(any(), any());
    }
//
//
//    // ------------------------------------------ DELETE /address/{address_id} ------------------------------------------
//
//    //This test case passes when you can successfully delete an address.
    @Test
    public void shouldDeleteAddress() throws Exception {
        final CustomerEntity customerEntity = new CustomerEntity();
        customerEntity.setContactNumber("1234567890");
        when(mockCustomerService.checkAuthToken(anyString(),anyString())).thenReturn(customerEntity);

        final AddressEntity addressEntity = new AddressEntity();
        List<CustomerEntity> customers = new ArrayList<CustomerEntity>();
        customers.add(customerEntity);
        addressEntity.setCustomers(customers);
        when(mockAddressService.getAddressByUuid(anyString())).thenReturn(addressEntity);

        final AddressEntity deletedAddressEntity = new AddressEntity();
        final String uuid = UUID.randomUUID().toString();
        deletedAddressEntity.setUuid(uuid);
        when(mockAddressService.deleteAddress(addressEntity, customerEntity)).thenReturn(deletedAddressEntity);

        mockMvc
                .perform(delete("/address/82849cd5-106e-4b34-b9bf-94954c6ff527")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .header("authorization", "Bearer database_accesstoken2")
                        .content("{\"flat_building_name\":\"xyz\", \"locality\":\"abc\", \"city\":\"pqr\", \"pincode\":\"100000\", \"state_uuid\":\"c860e78a-a29b-11e8-9a3a-720006ceb890\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(uuid))
                .andExpect(jsonPath("status").value("ADDRESS DELETED SUCCESSFULLY"));
        verify(mockCustomerService, times(1)).checkAuthToken(anyString(),anyString());
        verify(mockAddressService, times(1)).getAddressByUuid(anyString());
        verify(mockAddressService, times(1)).deleteAddress(addressEntity,customerEntity);
    }
//
//    //This test case passes when you have handled the exception of trying to delete an address with non existing access-token.
    @Test
    public void shouldNotDeleteAddressWithNonExistingAccessToken() throws Exception {
        when(mockCustomerService.checkAuthToken(anyString(),anyString()))
                .thenThrow(new AuthorizationFailedException("ATHR-001", "Customer is not Logged in."));

        mockMvc
                .perform(delete("/address/address_id")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .header("authorization", "Bearer non_existing_access_token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("code").value("ATHR-001"));
        verify(mockCustomerService, times(1)).checkAuthToken(anyString(),anyString());
    }
//
//    //This test case passes when you have handled the exception of trying to delete an address with a signed out user.
    @Test
    public void shouldNotDeleteAddressWithSignedOutUser() throws Exception {
        when(mockCustomerService.checkAuthToken(anyString(),anyString()))
                .thenThrow(new AuthorizationFailedException("ATHR-002", "Customer is logged out. Log in again to access this endpoint."));

        mockMvc
                .perform(delete("/address/address_id")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .header("authorization", "Bearer database_accesstoken"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("code").value("ATHR-002"));
        verify(mockCustomerService, times(1)).checkAuthToken(anyString(),anyString());
    }
//
//    //This test case passes when you have handled the exception of trying to delete an address with expired session user.
    @Test
    public void shouldNotDeleteAddressWithExpiredSessionUser() throws Exception {
        when(mockCustomerService.checkAuthToken(anyString(),anyString()))
                .thenThrow(new AuthorizationFailedException("ATHR-003", "Your session is expired. Log in again to access this endpoint."));

        mockMvc
                .perform(delete("/address/address_id")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .header("authorization", "Bearer database_accesstoken1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("code").value("ATHR-003"));
        verify(mockCustomerService, times(1)).checkAuthToken(anyString(),anyString());
    }
//
//    //This test case passes when you have handled the exception of trying to delete an address with by providing an
//    // address id that does not exist in the database.
    @Test
    public void shouldNotDeleteAddressIfNoAddressPresentAgainstGivenAddressId() throws Exception {
        final CustomerEntity customerEntity = new CustomerEntity();
        when(mockCustomerService.checkAuthToken(anyString(),anyString())).thenReturn(customerEntity);
        when(mockAddressService.getAddressByUuid("82849cd5-106e-4b34-b9bf-94954c6ff527")).thenReturn(null);

        mockMvc
                .perform(delete("/address/82849cd5-106e-4b34-b9bf-94954c6ff527")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .header("authorization", "Bearer database_accesstoken2")
                        .content("{\"flat_building_name\":\"xyz\", \"locality\":\"abc\", \"city\":\"pqr\", \"pincode\":\"100000\", \"state_uuid\":\"c860e78a-a29b-11e8-9a3a-720006ceb890\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("code").value("ANF-003"));
        verify(mockCustomerService, times(1)).checkAuthToken(anyString(),anyString());
        verify(mockAddressService, times(1)).getAddressByUuid("82849cd5-106e-4b34-b9bf-94954c6ff527");
    }
//
//    //This test case passes when you have handled the exception of trying to delete an address of a different customer
//    // other than the customer who is currently logged in.
    @Test
    public void shouldNotDeleteAddressForWrongCustomer() throws Exception {
//        final CustomerEntity customerEntity = new CustomerEntity();
//        when(mockCustomerService.checkAuthToken(anyString(),anyString())).thenReturn(customerEntity);
//        when(mockAddressService.getAddressByUuid("82849cd5-106e-4b34-b9bf-94954c6ff527")).thenReturn(new AddressEntity())

        final CustomerEntity customerEntity = new CustomerEntity();
        customerEntity.setContactNumber("1234567890");
        when(mockCustomerService.checkAuthToken(anyString(),anyString())).thenReturn(customerEntity);

        final AddressEntity addressEntity = new AddressEntity();
        List<CustomerEntity> customers = new ArrayList<CustomerEntity>();
        final CustomerEntity customer = new CustomerEntity();
        customer.setContactNumber("1234569999");
        customers.add(customer);
        addressEntity.setCustomers(customers);
        when(mockAddressService.getAddressByUuid(anyString())).thenReturn(addressEntity);

        final AddressEntity deletedAddressEntity = new AddressEntity();
        final String uuid = UUID.randomUUID().toString();
        deletedAddressEntity.setUuid(uuid);
        when(mockAddressService.deleteAddress(addressEntity, customerEntity)).thenReturn(deletedAddressEntity);

        mockMvc
                .perform(delete("/address/82849cd5-106e-4b34-b9bf-94954c6ff527")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .header("authorization", "Bearer database_accesstoken2")
                        .content("{\"flat_building_name\":\"xyz\", \"locality\":\"abc\", \"city\":\"pqr\", \"pincode\":\"100000\", \"state_uuid\":\"c860e78a-a29b-11e8-9a3a-720006ceb890\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("code").value("ATHR-004"));
        verify(mockCustomerService, times(1)).checkAuthToken(anyString(),anyString());
        verify(mockAddressService, times(1)).getAddressByUuid(anyString());
        verify(mockAddressService, times(0)).deleteAddress(addressEntity, customerEntity);
    }
//
//    // ------------------------------------------ GET /address/customer ------------------------------------------
//
//    //This test case passes when you are able to retrieve all the saved address of a customer.
    @Test
    public void shouldGetAllAddresses() throws Exception {
        final CustomerEntity customerEntity = new CustomerEntity();

        final AddressEntity addressEntity = new AddressEntity();
        final String addressUuid = UUID.randomUUID().toString();
        addressEntity.setUuid(addressUuid);
        addressEntity.setPincode("100000");
        addressEntity.setCity("city");
        addressEntity.setLocality("locality");
        addressEntity.setFlatBuildNumber("flatBuildNo");
        final String stateUuid = UUID.randomUUID().toString();
        StateEntity stateEntity = new StateEntity();
        stateEntity.setUuid(stateUuid);
        stateEntity.setStateName("state");
        addressEntity.setState(stateEntity);

        List<AddressEntity> addressEntityList = new ArrayList<AddressEntity>();
        addressEntityList.add(addressEntity);
        customerEntity.setAddresses(addressEntityList);

        when(mockCustomerService.checkAuthToken(anyString(),anyString())).thenReturn(customerEntity);



        final String response = mockMvc
                .perform(get("/address/customer")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .header("authorization", "Bearer database_accesstoken2"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        final AddressListResponse addressLists = new ObjectMapper().readValue(response, AddressListResponse.class);
        assertEquals(addressLists.getAddresses().size(), 1);

        final AddressList addressList = addressLists.getAddresses().get(0);
        assertEquals(addressList.getFlatBuildingName(), "flatBuildNo");
        assertEquals(addressList.getLocality(), "locality");
        assertEquals(addressList.getPincode(), "100000");
        assertEquals(addressList.getCity(), "city");
        assertEquals(addressList.getState().getId().toString(), stateUuid);
        assertEquals(addressList.getState().getStateName(), "state");
        assertEquals(addressList.getId().toString(), addressUuid);

        verify(mockCustomerService, times(1)).checkAuthToken(anyString(),anyString());
    }
//
//    //This test case passes when you have handled the exception of trying to fetch addresses for any customer with non existing access-token.
    @Test
    public void shouldNotGetAllAddressesWithNonExistingAccessToken() throws Exception {
        when(mockCustomerService.checkAuthToken(anyString(),anyString()))
                .thenThrow(new AuthorizationFailedException("ATHR-001", "Customer is not Logged in."));

        mockMvc
                .perform(get("/address/customer")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .header("authorization", "Bearer non_existing_access_token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("code").value("ATHR-001"));
        verify(mockCustomerService, times(1)).checkAuthToken(anyString(),anyString());
    }
//
//    //This test case passes when you have handled the exception of trying to fetch addresses for any customer with while
//    // the customer is currently signed out.
    @Test
    public void shouldNotGetAllAddressesWithSignedOutUser() throws Exception {
        when(mockCustomerService.checkAuthToken(anyString(),anyString()))
                .thenThrow(new AuthorizationFailedException("ATHR-002", "Customer is logged out. Log in again to access this endpoint."));

        mockMvc
                .perform(get("/address/customer")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .header("authorization", "Bearer database_accesstoken"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("code").value("ATHR-002"));
        verify(mockCustomerService, times(1)).checkAuthToken(anyString(),anyString());
    }
//
//    //This test case passes when you have handled the exception of trying to fetch addresses for any customer while
//    // the session of that customer is already expired.
    @Test
    public void shouldNotGetAllAddressesWithExpiredSessionUser() throws Exception {
        when(mockCustomerService.checkAuthToken(anyString(),anyString()))
                .thenThrow(new AuthorizationFailedException("ATHR-003", "Your session is expired. Log in again to access this endpoint."));

        mockMvc
                .perform(delete("/address/customer")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .header("authorization", "Bearer database_accesstoken1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("code").value("ATHR-003"));
        verify(mockCustomerService, times(1)).checkAuthToken(anyString(),anyString());
    }
//
//    // ------------------------------------------ GET /states ------------------------------------------
//
//    //This test case passes when you are able to fetch the list of all available states.
    @Test
    public void shouldGetAllStates() throws Exception {
        final String stateUuid = UUID.randomUUID().toString();
        final StateEntity stateEntity = new StateEntity();
        stateEntity.setUuid(stateUuid);
        stateEntity.setStateName("stateName");
//        addressEntity.setState(stateEntity);
//        final StateEntity stateEntity = new StateEntity(stateUuid, "stateName");
        when(mockAddressService.getAllStates()).thenReturn(Collections.singletonList(stateEntity));

        final String response = mockMvc
                .perform(get("/states").contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        final StatesListResponse statesLists = new ObjectMapper().readValue(response, StatesListResponse.class);
        assertEquals(statesLists.getStates().size(), 1);

        final StatesList statesList = statesLists.getStates().get(0);
        assertEquals(statesList.getId().toString(), stateUuid);
        assertEquals(statesList.getStateName(), "stateName");
    }
//
//    //This test case passes when you are not able to retrive any states if there are no states saved in the database.
    @Test
    public void shouldNotGetAllStatesIfNonePresentInDb() throws Exception {
        when(mockAddressService.getAllStates()).thenReturn(Collections.emptyList());

        final String response = mockMvc
                .perform(get("/states").contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        final StatesListResponse statesLists = new ObjectMapper().readValue(response, StatesListResponse.class);
        assertEquals(Collections.emptyList(),statesLists.getStates());
    }
}