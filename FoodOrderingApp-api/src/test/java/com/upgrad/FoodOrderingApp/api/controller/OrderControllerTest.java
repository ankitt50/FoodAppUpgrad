package com.upgrad.FoodOrderingApp.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.upgrad.FoodOrderingApp.api.model.CustomerOrderResponse;
import com.upgrad.FoodOrderingApp.api.model.ItemQuantity;
import com.upgrad.FoodOrderingApp.api.model.SaveOrderRequest;
import com.upgrad.FoodOrderingApp.service.businness.*;
import com.upgrad.FoodOrderingApp.service.entity.*;
import com.upgrad.FoodOrderingApp.service.exception.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import javax.persistence.criteria.Order;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.Assert.*;

// This class contains all the test cases regarding the order controller
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class OrderControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService mockOrderService;

    @MockBean
    private CustomerBusinessService mockCustomerService;

    @MockBean
    private PaymentService mockPaymentService;

    @MockBean
    private AddressBusinessService mockAddressService;

    @MockBean
    private RestaurantService mockRestaurantService;

    @MockBean
    private ItemService mockItemService;

    // ------------------------------------------ POST /order ------------------------------------------

    //This test case passes when you are able to save order successfully.
    @Test
    public void shouldSaveOrder() throws Exception {
        final CustomerEntity customerEntity = new CustomerEntity();
        final String customerId = UUID.randomUUID().toString();
        customerEntity.setUuid(customerId);
        customerEntity.setContactNumber("1234567980");
        when(mockCustomerService.checkAuthToken(anyString(),anyString()))
                .thenReturn(customerEntity);

        final SaveOrderRequest saveOrderRequest = getSaveOrderRequest();
        List<PaymentEntity> paymentMethods = new ArrayList<PaymentEntity>();
        PaymentEntity paymentMethod = new PaymentEntity();
        paymentMethod.setUuid(saveOrderRequest.getPaymentId().toString());
        paymentMethods.add(paymentMethod);
        when(mockPaymentService.getAllPaymentMethods())
                .thenReturn(paymentMethods);
        final AddressEntity addressEntity = new AddressEntity();
        List<CustomerEntity> customers = new ArrayList<CustomerEntity>();
        customers.add(customerEntity);
        addressEntity.setCustomers(customers);
        when(mockAddressService.getAddressByUuid(saveOrderRequest.getAddressId()))
                .thenReturn(addressEntity);
        when(mockRestaurantService.getRestaurantByUuid(saveOrderRequest.getRestaurantId().toString()))
                .thenReturn(new RestaurantEntity());
        when(mockOrderService.getCouponByUuid(saveOrderRequest.getCouponId().toString()))
                .thenReturn(new CouponEntity());
        when(mockItemService.getItemByUuid(saveOrderRequest.getItemQuantities().get(0).getItemId().toString()))
                .thenReturn(new ItemEntity());

        final OrderEntity orderEntity = new OrderEntity();
        final String orderId = UUID.randomUUID().toString();
        orderEntity.setUuid(orderId);
//        when(mockOrderService.saveOrder(any())).thenReturn(orderEntity);
        when(mockOrderService.saveOrder(any())).thenReturn(new OrderItemEntity());

        mockMvc
                .perform(post("/order")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .header("authorization", "Bearer database_accesstoken2")
                        .content(new ObjectMapper().writeValueAsString(saveOrderRequest)))
                .andExpect(status().isCreated());
        verify(mockCustomerService, times(1))
                .checkAuthToken(anyString(),anyString());
        verify(mockPaymentService, times(1))
                .getAllPaymentMethods();
        verify(mockAddressService, times(1))
                .getAddressByUuid(saveOrderRequest.getAddressId());
        verify(mockRestaurantService, times(1))
                .getRestaurantByUuid(saveOrderRequest.getRestaurantId().toString());
        verify(mockOrderService, times(1))
                .getCouponByUuid(saveOrderRequest.getCouponId().toString());
    }

    //This test case passes when you have handled the exception of trying to save an order while you are not logged  in.
    @Test
    public void shouldNotSaveOrderIfCustomerIsNotLoggedIn() throws Exception {
        when(mockCustomerService.checkAuthToken(anyString(),anyString()))
                .thenThrow(new AuthorizationFailedException("ATHR-001", "Customer is not Logged in."));

        mockMvc
                .perform(post("/order")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .header("authorization", "Bearer invalid_auth")
                        .content(new ObjectMapper().writeValueAsString(getSaveOrderRequest())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("code").value("ATHR-001"));

        verify(mockCustomerService, times(1)).checkAuthToken(anyString(),anyString());
        verify(mockPaymentService, times(0)).getAllPaymentMethods();
        verify(mockAddressService, times(0)).getAddressByUuid(anyString());
        verify(mockRestaurantService, times(0)).getRestaurantByUuid(anyString());
        verify(mockOrderService, times(0)).getCouponByUuid(anyString());
        verify(mockOrderService, times(0)).saveOrder(any());
    }
//
//    //This test case passes when you have handled the exception of trying to save an order while you are already logged out.
    @Test
    public void shouldNotSaveOrderIfCustomerIsLoggedOut() throws Exception {
        when(mockCustomerService.checkAuthToken(anyString(),anyString()))
                .thenThrow(new AuthorizationFailedException("ATHR-002", "Customer is logged out. Log in again to access this endpoint."));
        mockMvc
                .perform(post("/order")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .header("authorization", "Bearer invalid_auth")
                        .content(new ObjectMapper().writeValueAsString(getSaveOrderRequest())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("code").value("ATHR-002"));

        verify(mockCustomerService, times(1)).checkAuthToken(anyString(),anyString());
        verify(mockPaymentService, times(0)).getAllPaymentMethods();
        verify(mockAddressService, times(0)).getAddressByUuid(anyString());
        verify(mockRestaurantService, times(0)).getRestaurantByUuid(anyString());
        verify(mockOrderService, times(0)).getCouponByUuid(anyString());
        verify(mockOrderService, times(0)).saveOrder(any());
    }
//
//    //This test case passes when you have handled the exception of trying to save an order while your session is
//    // already expired.
    @Test
    public void shouldNotSaveOrderIfCustomerSessionIsExpired() throws Exception {
        when(mockCustomerService.checkAuthToken(anyString(),anyString()))
                .thenThrow(new AuthorizationFailedException("ATHR-003", "Your session is expired. Log in again to access this endpoint."));
        mockMvc
                .perform(post("/order")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .header("authorization", "Bearer invalid_auth")
                        .content(new ObjectMapper().writeValueAsString(getSaveOrderRequest())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("code").value("ATHR-003"));

        verify(mockCustomerService, times(1)).checkAuthToken(anyString(),anyString());
        verify(mockPaymentService, times(0)).getAllPaymentMethods();
        verify(mockAddressService, times(0)).getAddressByUuid(anyString());
        verify(mockRestaurantService, times(0)).getRestaurantByUuid(anyString());
        verify(mockOrderService, times(0)).getCouponByUuid(anyString());
        verify(mockOrderService, times(0)).saveOrder(any());
    }
//
//    //This test case passes when you have handled the exception of trying to save an order while the payment id you gave
//    // for making the payment does not exist in the database.
    @Test
    public void shouldNotSaveOrderIfPaymentMethodDoesNotExists() throws Exception {

        final CustomerEntity customerEntity = new CustomerEntity();
        final String customerId = UUID.randomUUID().toString();
        customerEntity.setUuid(customerId);
        customerEntity.setContactNumber("1234567980");
        when(mockCustomerService.checkAuthToken(anyString(),anyString()))
                .thenReturn(customerEntity);

        final SaveOrderRequest saveOrderRequest = getSaveOrderRequest();
        List<PaymentEntity> paymentMethods = new ArrayList<PaymentEntity>();
        PaymentEntity paymentMethod = new PaymentEntity();
        paymentMethod.setUuid(saveOrderRequest.getPaymentId().toString());
        paymentMethods.add(paymentMethod);
        when(mockPaymentService.getAllPaymentMethods())
                .thenReturn(Collections.emptyList());
        final AddressEntity addressEntity = new AddressEntity();
        List<CustomerEntity> customers = new ArrayList<CustomerEntity>();
        customers.add(customerEntity);
        addressEntity.setCustomers(customers);
        when(mockAddressService.getAddressByUuid(saveOrderRequest.getAddressId()))
                .thenReturn(addressEntity);
        when(mockRestaurantService.getRestaurantByUuid(saveOrderRequest.getRestaurantId().toString()))
                .thenReturn(new RestaurantEntity());
        when(mockOrderService.getCouponByUuid(saveOrderRequest.getCouponId().toString()))
                .thenReturn(new CouponEntity());
        when(mockItemService.getItemByUuid(saveOrderRequest.getItemQuantities().get(0).getItemId().toString()))
                .thenReturn(new ItemEntity());

        final OrderEntity orderEntity = new OrderEntity();
        final String orderId = UUID.randomUUID().toString();
        orderEntity.setUuid(orderId);
//        when(mockOrderService.saveOrder(any())).thenReturn(orderEntity);
        when(mockOrderService.saveOrder(any())).thenReturn(new OrderItemEntity());












//        final SaveOrderRequest saveOrderRequest = getSaveOrderRequest();
//        when(mockPaymentService.getPaymentByUUID(saveOrderRequest.getPaymentId().toString()))
//                .thenThrow(new PaymentMethodNotFoundException("PNF-002", "No payment method found by this id"));

        mockMvc
                .perform(post("/order")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .header("authorization", "Bearer database_accesstoken2")
                        .content(new ObjectMapper().writeValueAsString(saveOrderRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("code").value("PNF-002"));
        verify(mockCustomerService, times(1))
                .checkAuthToken(anyString(),anyString());
        verify(mockPaymentService, times(1))
                .getAllPaymentMethods();
    }
//
//    //This test case passes when you have handled the exception of trying to save an order while the address id you
//    // gave to deliver the order does not exist in the database.
    @Test
    public void shouldNotSaveOrderIfAddressNotFound() throws Exception {





        final CustomerEntity customerEntity = new CustomerEntity();
        final String customerId = UUID.randomUUID().toString();
        customerEntity.setUuid(customerId);
        customerEntity.setContactNumber("1234567980");
        when(mockCustomerService.checkAuthToken(anyString(),anyString()))
                .thenReturn(customerEntity);

        final SaveOrderRequest saveOrderRequest = getSaveOrderRequest();
        List<PaymentEntity> paymentMethods = new ArrayList<PaymentEntity>();
        PaymentEntity paymentMethod = new PaymentEntity();
        paymentMethod.setUuid(saveOrderRequest.getPaymentId().toString());
        paymentMethods.add(paymentMethod);
        when(mockPaymentService.getAllPaymentMethods())
                .thenReturn(paymentMethods);
        final AddressEntity addressEntity = new AddressEntity();
        List<CustomerEntity> customers = new ArrayList<CustomerEntity>();
        customers.add(customerEntity);
        addressEntity.setCustomers(customers);
        when(mockAddressService.getAddressByUuid(saveOrderRequest.getAddressId()))
                .thenReturn(null);
        when(mockRestaurantService.getRestaurantByUuid(saveOrderRequest.getRestaurantId().toString()))
                .thenReturn(new RestaurantEntity());
        when(mockOrderService.getCouponByUuid(saveOrderRequest.getCouponId().toString()))
                .thenReturn(new CouponEntity());
        when(mockItemService.getItemByUuid(saveOrderRequest.getItemQuantities().get(0).getItemId().toString()))
                .thenReturn(new ItemEntity());

        final OrderEntity orderEntity = new OrderEntity();
        final String orderId = UUID.randomUUID().toString();
        orderEntity.setUuid(orderId);
//        when(mockOrderService.saveOrder(any())).thenReturn(orderEntity);
        when(mockOrderService.saveOrder(any())).thenReturn(new OrderItemEntity());






//        final CustomerEntity customerEntity = new CustomerEntity();
//        final String customerId = UUID.randomUUID().toString();
//        customerEntity.setUuid(customerId);
//        when(mockCustomerService.getCustomer("database_accesstoken2"))
//                .thenReturn(customerEntity);
//
//        final SaveOrderRequest saveOrderRequest = getSaveOrderRequest();
//        when(mockPaymentService.getPaymentByUUID(saveOrderRequest.getPaymentId().toString()))
//                .thenReturn(new PaymentEntity());
//        when(mockAddressService.getAddressByUUID(saveOrderRequest.getAddressId(), customerEntity))
//                .thenThrow(new AddressNotFoundException("ANF-003", "No address by this id"));

        mockMvc
                .perform(post("/order")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .header("authorization", "Bearer database_accesstoken2")
                        .content(new ObjectMapper().writeValueAsString(saveOrderRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("code").value("ANF-003"));
        verify(mockCustomerService, times(1))
                .checkAuthToken(anyString(),anyString());
        verify(mockAddressService, times(1))
                .getAddressByUuid(saveOrderRequest.getAddressId());
    }
//
//    //This test case passes when you have handled the exception of trying to save an order while the address if you
//    // have given to deliver the order belongs to a different customer.
    @Test
    public void shouldNotSaveOrderIfUserUnauthorizedToChangeAddress() throws Exception {
//        final CustomerEntity customerEntity = new CustomerEntity();
//        final String customerId = UUID.randomUUID().toString();
//        customerEntity.setUuid(customerId);
//        when(mockCustomerService.getCustomer("database_accesstoken2"))
//                .thenReturn(customerEntity);
//
//        final SaveOrderRequest saveOrderRequest = getSaveOrderRequest();
//        when(mockPaymentService.getPaymentByUUID(saveOrderRequest.getPaymentId().toString()))
//                .thenReturn(new PaymentEntity());
//        when(mockAddressService.getAddressByUUID(saveOrderRequest.getAddressId(), customerEntity))
//                .thenThrow(new AuthorizationFailedException("ATHR-004", "You are not authorized to view/update/delete any one else's address"));





        final CustomerEntity customerEntity = new CustomerEntity();
        final String customerId = UUID.randomUUID().toString();
        customerEntity.setUuid(customerId);
        customerEntity.setContactNumber("1234567980");
        when(mockCustomerService.checkAuthToken(anyString(),anyString()))
                .thenReturn(customerEntity);

        final SaveOrderRequest saveOrderRequest = getSaveOrderRequest();
        List<PaymentEntity> paymentMethods = new ArrayList<PaymentEntity>();
        PaymentEntity paymentMethod = new PaymentEntity();
        paymentMethod.setUuid(saveOrderRequest.getPaymentId().toString());
        paymentMethods.add(paymentMethod);
        when(mockPaymentService.getAllPaymentMethods())
                .thenReturn(paymentMethods);
        final AddressEntity addressEntity = new AddressEntity();
        addressEntity.setCustomers(Collections.emptyList());
        when(mockAddressService.getAddressByUuid(saveOrderRequest.getAddressId()))
                .thenReturn(addressEntity);
        when(mockRestaurantService.getRestaurantByUuid(saveOrderRequest.getRestaurantId().toString()))
                .thenReturn(new RestaurantEntity());
        when(mockOrderService.getCouponByUuid(saveOrderRequest.getCouponId().toString()))
                .thenReturn(new CouponEntity());
        when(mockItemService.getItemByUuid(saveOrderRequest.getItemQuantities().get(0).getItemId().toString()))
                .thenReturn(new ItemEntity());

        final OrderEntity orderEntity = new OrderEntity();
        final String orderId = UUID.randomUUID().toString();
        orderEntity.setUuid(orderId);
//        when(mockOrderService.saveOrder(any())).thenReturn(orderEntity);
        when(mockOrderService.saveOrder(any())).thenReturn(new OrderItemEntity());









        mockMvc
                .perform(post("/order")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .header("authorization", "Bearer database_accesstoken2")
                        .content(new ObjectMapper().writeValueAsString(saveOrderRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("code").value("ATHR-004"));
        verify(mockCustomerService, times(1))
                .checkAuthToken(anyString(),anyString());
        verify(mockAddressService, times(1))
                .getAddressByUuid(saveOrderRequest.getAddressId());
    }
//
//    //This test case passes when you have handled the exception of trying to save an order while the restaurant id
//    // you gave does not exist in the database.
    @Test
    public void shouldNotSaveOrderIfRestaurantDoesNotExists() throws Exception {
//        final CustomerEntity customerEntity = new CustomerEntity();
//        final String customerId = UUID.randomUUID().toString();
//        customerEntity.setUuid(customerId);
//        when(mockCustomerService.getCustomer("database_accesstoken2"))
//                .thenReturn(customerEntity);
//
//        final SaveOrderRequest saveOrderRequest = getSaveOrderRequest();
//        when(mockPaymentService.getPaymentByUUID(saveOrderRequest.getPaymentId().toString()))
//                .thenReturn(new PaymentEntity());
//        when(mockAddressService.getAddressByUUID(saveOrderRequest.getAddressId(), customerEntity))
//                .thenReturn(new AddressEntity());
//        when(mockRestaurantService.restaurantByUUID(saveOrderRequest.getRestaurantId().toString()))
//                .thenThrow(new RestaurantNotFoundException("RNF-001", "No restaurant by this id"));







        final CustomerEntity customerEntity = new CustomerEntity();
        final String customerId = UUID.randomUUID().toString();
        customerEntity.setUuid(customerId);
        customerEntity.setContactNumber("1234567980");
        when(mockCustomerService.checkAuthToken(anyString(),anyString()))
                .thenReturn(customerEntity);

        final SaveOrderRequest saveOrderRequest = getSaveOrderRequest();
        List<PaymentEntity> paymentMethods = new ArrayList<PaymentEntity>();
        PaymentEntity paymentMethod = new PaymentEntity();
        paymentMethod.setUuid(saveOrderRequest.getPaymentId().toString());
        paymentMethods.add(paymentMethod);
        when(mockPaymentService.getAllPaymentMethods())
                .thenReturn(paymentMethods);
        final AddressEntity addressEntity = new AddressEntity();
        List<CustomerEntity> customers = new ArrayList<CustomerEntity>();
        customers.add(customerEntity);
        addressEntity.setCustomers(customers);
        when(mockAddressService.getAddressByUuid(saveOrderRequest.getAddressId()))
                .thenReturn(addressEntity);
        when(mockRestaurantService.getRestaurantByUuid(saveOrderRequest.getRestaurantId().toString()))
                .thenReturn(null);
        when(mockOrderService.getCouponByUuid(saveOrderRequest.getCouponId().toString()))
                .thenReturn(new CouponEntity());
        when(mockItemService.getItemByUuid(saveOrderRequest.getItemQuantities().get(0).getItemId().toString()))
                .thenReturn(new ItemEntity());

        final OrderEntity orderEntity = new OrderEntity();
        final String orderId = UUID.randomUUID().toString();
        orderEntity.setUuid(orderId);
//        when(mockOrderService.saveOrder(any())).thenReturn(orderEntity);
        when(mockOrderService.saveOrder(any())).thenReturn(new OrderItemEntity());









        mockMvc
                .perform(post("/order")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .header("authorization", "Bearer database_accesstoken2")
                        .content(new ObjectMapper().writeValueAsString(saveOrderRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("code").value("RNF-001"));
        verify(mockCustomerService, times(1)).checkAuthToken(anyString(),anyString());
        verify(mockRestaurantService, times(1)).getRestaurantByUuid(anyString());

    }
//
//    //This test case passes when you have handled the exception of trying to save an order while the coupon name
//    // you gave does not exist in the database.
    @Test
    public void shouldNotSaveOrderIfCouponNotFound() throws Exception {
//        final CustomerEntity customerEntity = new CustomerEntity();
//        final String customerId = UUID.randomUUID().toString();
//        customerEntity.setUuid(customerId);
//        when(mockCustomerService.getCustomer("database_accesstoken2"))
//                .thenReturn(customerEntity);
//
//        final SaveOrderRequest saveOrderRequest = getSaveOrderRequest();
//        when(mockPaymentService.getPaymentByUUID(saveOrderRequest.getPaymentId().toString()))
//                .thenReturn(new PaymentEntity());
//        when(mockAddressService.getAddressByUUID(saveOrderRequest.getAddressId(), customerEntity))
//                .thenReturn(new AddressEntity());
//        when(mockRestaurantService.restaurantByUUID(saveOrderRequest.getRestaurantId().toString()))
//                .thenReturn(new RestaurantEntity());
//        when(mockOrderService.getCouponByCouponId(saveOrderRequest.getCouponId().toString()))
//                .thenThrow(new CouponNotFoundException("CPF-002", "No coupon by this id"));







        final CustomerEntity customerEntity = new CustomerEntity();
        final String customerId = UUID.randomUUID().toString();
        customerEntity.setUuid(customerId);
        customerEntity.setContactNumber("1234567980");
        when(mockCustomerService.checkAuthToken(anyString(),anyString()))
                .thenReturn(customerEntity);

        final SaveOrderRequest saveOrderRequest = getSaveOrderRequest();
        List<PaymentEntity> paymentMethods = new ArrayList<PaymentEntity>();
        PaymentEntity paymentMethod = new PaymentEntity();
        paymentMethod.setUuid(saveOrderRequest.getPaymentId().toString());
        paymentMethods.add(paymentMethod);
        when(mockPaymentService.getAllPaymentMethods())
                .thenReturn(paymentMethods);
        final AddressEntity addressEntity = new AddressEntity();
        List<CustomerEntity> customers = new ArrayList<CustomerEntity>();
        customers.add(customerEntity);
        addressEntity.setCustomers(customers);
        when(mockAddressService.getAddressByUuid(saveOrderRequest.getAddressId()))
                .thenReturn(addressEntity);
        when(mockRestaurantService.getRestaurantByUuid(saveOrderRequest.getRestaurantId().toString()))
                .thenReturn(new RestaurantEntity());
        when(mockOrderService.getCouponByUuid(saveOrderRequest.getCouponId().toString()))
                .thenReturn(null);
        when(mockItemService.getItemByUuid(saveOrderRequest.getItemQuantities().get(0).getItemId().toString()))
                .thenReturn(new ItemEntity());

        final OrderEntity orderEntity = new OrderEntity();
        final String orderId = UUID.randomUUID().toString();
        orderEntity.setUuid(orderId);
//        when(mockOrderService.saveOrder(any())).thenReturn(orderEntity);
        when(mockOrderService.saveOrder(any())).thenReturn(new OrderItemEntity());












        mockMvc
                .perform(post("/order")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .header("authorization", "Bearer database_accesstoken2")
                        .content(new ObjectMapper().writeValueAsString(saveOrderRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("code").value("CPF-002"));
        verify(mockCustomerService, times(1))
                .checkAuthToken(anyString(),anyString());

    }
//
//    // ------------------------------------------ GET /order ------------------------------------------
//
//    //This test case passes when you are able to retrieve all past orders placed by you
    @Test
    public void shouldGetPlacedOrderDetails() throws Exception {
        final CustomerEntity customerEntity = new CustomerEntity();
        final String customerId = UUID.randomUUID().toString();
        customerEntity.setUuid(customerId);
        when(mockCustomerService.checkAuthToken(anyString(),anyString()))
                .thenReturn(customerEntity);

        final OrderEntity orderEntity = getOrderEntity(customerEntity);
        when(mockOrderService.getPastOrdersOfUsers(customerEntity))
                .thenReturn(Collections.singletonList(orderEntity));

        final String responseString = mockMvc
                .perform(get("/order")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .header("authorization", "Bearer database_accesstoken2"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        final CustomerOrderResponse customerOrderResponse = new ObjectMapper().readValue(responseString, CustomerOrderResponse.class);
        assertEquals(customerOrderResponse.getOrders().size(), 1);
        assertEquals(customerOrderResponse.getOrders().get(0).getId().toString(), orderEntity.getUuid());
        assertEquals(customerOrderResponse.getOrders().get(0).getId().toString(), orderEntity.getUuid());
        assertEquals(customerOrderResponse.getOrders().get(0).getCustomer().getId().toString(), orderEntity.getCustomer().getUuid());
        assertEquals(customerOrderResponse.getOrders().get(0).getAddress().getId().toString(), orderEntity.getAddress().getUuid());
        assertEquals(customerOrderResponse.getOrders().get(0).getAddress().getState().getId().toString(), orderEntity.getAddress().getState().getUuid());

    }
//
//    //This test case passes when you have handled the exception of trying to fetch placed orders if you are not logged in.
//    @Test
    public void shouldNotGetPlacedOrderDetailsIfCustomerIsNotLoggedIn() throws Exception {
        when(mockCustomerService.checkAuthToken(anyString(),anyString()))
                .thenThrow(new AuthorizationFailedException("ATHR-001", "Customer is not Logged in."));
        mockMvc
                .perform(get("/order")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .header("authorization", "Bearer invalid_auth"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("code").value("ATHR-001"));

        verify(mockCustomerService, times(1)).checkAuthToken(anyString(),anyString());
    }
//
//    //This test case passes when you have handled the exception of trying to fetch placed orders if you are already
//    // logged out.
    @Test
    public void shouldNotGetPlacedOrderDetailsIfCustomerIsLoggedOut() throws Exception {
        when(mockCustomerService.checkAuthToken(anyString(),anyString()))
                .thenThrow(new AuthorizationFailedException("ATHR-002", "Customer is logged out. Log in again to access this endpoint."));
        mockMvc
                .perform(get("/order")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .header("authorization", "Bearer invalid_auth"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("code").value("ATHR-002"));

        verify(mockCustomerService, times(1)).checkAuthToken(anyString(),anyString());
    }
//
//    //This test case passes when you have handled the exception of trying to fetch placed orders if your session is
//    // already expired.
    @Test
    public void shouldNotGetPlacedOrderDetailsIfCustomerSessionIsExpired() throws Exception {
        when(mockCustomerService.checkAuthToken(anyString(),anyString()))
                .thenThrow(new AuthorizationFailedException("ATHR-003", "Your session is expired. Log in again to access this endpoint."));
        mockMvc
                .perform(get("/order")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .header("authorization", "Bearer invalid_auth"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("code").value("ATHR-003"));

        verify(mockCustomerService, times(1)).checkAuthToken(anyString(),anyString());
    }
//
//    // ------------------------------------------ GET /order/coupon/{coupon_name} ------------------------------------------
//
//    //This test case passes when you are able to retrieve coupon details by coupon name.
    @Test
    public void shouldGetCouponByName() throws Exception {
        when(mockCustomerService.checkAuthToken(anyString(),anyString()))
                .thenReturn(new CustomerEntity());

        final String couponId = UUID.randomUUID().toString();
        //couponId, "myCoupon", 10
        final CouponEntity couponEntity = new CouponEntity();
        couponEntity.setCouponName("myCoupon");
        couponEntity.setUuid(couponId);
        couponEntity.setPercent(10);
        when(mockOrderService.getCouponByCouponName("myCoupon")).thenReturn(couponEntity);

        mockMvc
                .perform(get("/order/coupon/myCoupon")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .header("authorization", "Bearer database_accesstoken2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(couponId))
                .andExpect(jsonPath("coupon_name").value("myCoupon"));
        verify(mockCustomerService, times(1)).checkAuthToken(anyString(),anyString());
        verify(mockOrderService, times(1)).getCouponByCouponName("myCoupon");
    }
//
//    //This test case passes when you have handled the exception of trying to fetch coupon details if you are not logged in.
    @Test
    public void shouldNotGetCouponByNameIfCustomerIsNotLoggedIn() throws Exception {
        when(mockCustomerService.checkAuthToken(anyString(),anyString())).thenThrow(new AuthorizationFailedException("ATHR-001", "Customer is not Logged in."));
        mockMvc
                .perform(get("/order/coupon/myCoupon")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .header("authorization", "Bearer invalid_auth"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("code").value("ATHR-001"));

        verify(mockCustomerService, times(1)).checkAuthToken(anyString(),anyString());
        verify(mockOrderService, times(0)).getCouponByCouponName(anyString());
    }
//
//    //This test case passes when you have handled the exception of trying to fetch placed orders while you are already
//    // logged out.
    @Test
    public void shouldNotGetCouponByNameIfCustomerIsLoggedOut() throws Exception {
        when(mockCustomerService.checkAuthToken(anyString(),anyString()))
                .thenThrow(new AuthorizationFailedException("ATHR-002", "Customer is logged out. Log in again to access this endpoint."));
        mockMvc
                .perform(get("/order/coupon/myCoupon")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .header("authorization", "Bearer invalid_auth"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("code").value("ATHR-002"));

        verify(mockCustomerService, times(1)).checkAuthToken(anyString(),anyString());
        verify(mockOrderService, times(0)).getCouponByCouponName(anyString());
    }
//
//    //This test case passes when you have handled the exception of trying to fetch placed orders if your session is
//    // already expired.
    @Test
    public void shouldNotGetCouponByNameIfCustomerSessionIsExpired() throws Exception {
        when(mockCustomerService.checkAuthToken(anyString(),anyString()))
                .thenThrow(new AuthorizationFailedException("ATHR-003", "Your session is expired. Log in again to access this endpoint."));
        mockMvc
                .perform(get("/order/coupon/myCoupon")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .header("authorization", "Bearer invalid_auth"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("code").value("ATHR-003"));

        verify(mockCustomerService, times(1)).checkAuthToken(anyString(),anyString());
        verify(mockOrderService, times(0)).getCouponByCouponName(anyString());
    }
//
//    //This test case passes when you have handled the exception of trying to fetch any coupon but your coupon name
//    // field is empty.
    @Test
    public void shouldNotGetCouponByNameIfCouponNameFieldIsEmpty() throws Exception {
        when(mockCustomerService.checkAuthToken(anyString(),anyString()))
                .thenReturn(new CustomerEntity());

        when(mockOrderService.getCouponByCouponName(anyString())).thenReturn(null);

        mockMvc
                .perform(get("/order/coupon/emptyString")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .header("authorization", "Bearer database_accesstoken2"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("code").value("CPF-001"));
        verify(mockCustomerService, times(1)).checkAuthToken(anyString(),anyString());
        verify(mockOrderService, times(1)).getCouponByCouponName(anyString());
    }
//
//    //This test case passes when you have handled the exception of trying to fetch coupon details while there are no
//    // coupon by the name you provided in the database.
    @Test
    public void shouldNotGetCouponByNameIfItDoesNotExists() throws Exception {
        when(mockCustomerService.checkAuthToken(anyString(),anyString()))
                .thenReturn(new CustomerEntity());

        when(mockOrderService.getCouponByCouponName("myCoupon")).thenReturn(null);

        mockMvc
                .perform(get("/order/coupon/myCoupon")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .header("authorization", "Bearer database_accesstoken2"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("code").value("CPF-001"));
        verify(mockCustomerService, times(1)).checkAuthToken(anyString(),anyString());
        verify(mockOrderService, times(1)).getCouponByCouponName("myCoupon");
    }
//
//    // ------------------------------------------ POJO Builder ------------------------------------------
//
    private SaveOrderRequest getSaveOrderRequest() {
        final SaveOrderRequest request = new SaveOrderRequest();

        request.setBill(BigDecimal.valueOf(786.69));
        request.setDiscount(BigDecimal.valueOf(1));

        final UUID restaurantId = UUID.randomUUID();
        request.setRestaurantId(restaurantId);

        final String addressId = UUID.randomUUID().toString();
        request.setAddressId(addressId);

        final UUID paymentId = UUID.randomUUID();
        request.setPaymentId(paymentId);

        final UUID couponId = UUID.randomUUID();
        request.setCouponId(couponId);

        final ItemQuantity itemQuantity = new ItemQuantity();
        itemQuantity.setPrice(786);
        itemQuantity.setQuantity(1);
        final UUID itemId = UUID.randomUUID();
        itemQuantity.setItemId(itemId);

        request.setItemQuantities(Collections.singletonList(itemQuantity));

        return request;
    }
//
    private OrderEntity getOrderEntity(final CustomerEntity customerEntity) {
        final String stateId = UUID.randomUUID().toString();
        //stateId, "someState"
        final StateEntity stateEntity = new StateEntity();
        stateEntity.setStateName("someState");
        stateEntity.setUuid(stateId);

        final String addressId = UUID.randomUUID().toString();
        //addressId, "a/b/c",
        //                "someLocality", "someCity", "100000", stateEntity
        final AddressEntity addressEntity = new AddressEntity();
        addressEntity.setUuid(addressId);
        addressEntity.setFlatBuildNumber("a/b/c");
        addressEntity.setCity("someCity");
        addressEntity.setLocality("someLocality");
        addressEntity.setPincode("100000");
        addressEntity.setState(stateEntity);


        final String couponId = UUID.randomUUID().toString();
        //couponId, "someCoupon", 10
        final CouponEntity couponEntity = new CouponEntity();
        couponEntity.setUuid(couponId);
        couponEntity.setCouponName("someCoupon");
        couponEntity.setPercent(10);

        final String paymentId = UUID.randomUUID().toString();
        final PaymentEntity paymentEntity = new PaymentEntity(paymentId, "spmePayment");

        final RestaurantEntity restaurantEntity = new RestaurantEntity();
        final String restaurantId = UUID.randomUUID().toString();
        restaurantEntity.setUuid(restaurantId);
        restaurantEntity.setAddress(addressEntity);
        restaurantEntity.setAveragePriceForTwo(123);
        restaurantEntity.setCustomerRating(3.4);
        restaurantEntity.setNumberOfCustomersRated(200);
        restaurantEntity.setPhotoUrl("someurl");
        restaurantEntity.setRestaurantName("Famous Restaurant");


        final String orderId = UUID.randomUUID().toString();
        //orderId, 200.50, couponEntity, 10.0,
        //                orderDate, paymentEntity, customerEntity, addressEntity, restaurantEntity)
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setUuid(orderId);
        orderEntity.setBill(200);
        orderEntity.setCoupon(couponEntity);
        orderEntity.setDiscount(10);
        orderEntity.setDate(LocalDateTime.now());
        orderEntity.setPayment(paymentEntity);
        orderEntity.setCustomer(customerEntity);
        orderEntity.setAddress(addressEntity);
        orderEntity.setRestaurant(restaurantEntity);

        return orderEntity;
    }

}