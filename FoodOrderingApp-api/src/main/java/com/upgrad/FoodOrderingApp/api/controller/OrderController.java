package com.upgrad.FoodOrderingApp.api.controller;


import com.upgrad.FoodOrderingApp.api.model.*;
import com.upgrad.FoodOrderingApp.service.businness.CustomerBusinessService;
import com.upgrad.FoodOrderingApp.service.businness.OrderService;
import com.upgrad.FoodOrderingApp.service.entity.*;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.CouponNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping(path = "/api")
public class OrderController {
    @Autowired
    private CustomerBusinessService customerBusinessService;
    @Autowired
    private OrderService orderService;

    @GetMapping(path = "/order/coupon/{coupon_name}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CouponDetailsResponse> getCouponByCouponName(@RequestHeader(name = "authorization") final String authToken, @PathVariable(name = "coupon_name") final String couponName) throws AuthorizationFailedException, CouponNotFoundException {
        String token = getToken(authToken);
        CustomerEntity customerEntity = customerBusinessService.checkAuthToken(token, "/order/coupon/{coupon_name}");
        if(couponName == null || couponName.equals("")) {
            throw new CouponNotFoundException("CPF-002","Coupon name field should not be empty");
        }
        CouponEntity couponEntity = orderService.getCouponByCouponName(couponName);
        if(couponEntity == null) {
            throw new CouponNotFoundException("CPF-001","No coupon by this name");
        }
        return new ResponseEntity<CouponDetailsResponse>(new CouponDetailsResponse().id(UUID.fromString(couponEntity.getUuid())).couponName(couponEntity.getCouponName()).percent(couponEntity.getPercent()), HttpStatus.OK);
    }

    @GetMapping(path = "/order", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<OrderList>> getPastOrdersOfUsers(@RequestHeader(name = "authorization") final String authToken) throws AuthorizationFailedException {
        String token = getToken(authToken);
        CustomerEntity customerEntity = customerBusinessService.checkAuthToken(token, "/order");
        List<OrderEntity> ordersForCustomer = orderService.getPastOrdersOfUsers(customerEntity);

        if(ordersForCustomer == null || ordersForCustomer.isEmpty()) {
            return new ResponseEntity<List<OrderList>>(new ArrayList<OrderList>(),HttpStatus.OK);
        }
        
        List<OrderList> responseOrderList = new ArrayList<OrderList>();

        Collections.sort(ordersForCustomer, new Comparator<OrderEntity>() {
            @Override
            public int compare(OrderEntity o1, OrderEntity o2) {
                return o1.getDate().compareTo(o2.getDate());
            }
        });

        for (OrderEntity e:
             ordersForCustomer) {
            UUID uuid = UUID.fromString(e.getUuid());
            BigDecimal bill = BigDecimal.valueOf(e.getBill());
            OrderListCoupon coupon = new OrderListCoupon().id(UUID.fromString(e.getCoupon().getUuid()))
                    .couponName(e.getCoupon().getCouponName())
                    .percent(e.getCoupon().getPercent());
            BigDecimal discount = BigDecimal.valueOf(e.getDiscount());
            String date = e.getDate().toString();
            OrderListPayment payment = new OrderListPayment().id(UUID.fromString(e.getPayment().getUuid()))
                    .paymentName(e.getPayment().getPaymentName());
            OrderListCustomer customer = new OrderListCustomer().id(UUID.fromString(e.getCustomer().getUuid()))
                    .firstName(e.getCustomer().getFirstName())
                    .lastName(e.getCustomer().getLastName())
                    .emailAddress(e.getCustomer().getEmail())
                    .contactNumber(e.getCustomer().getContactNumber());
            OrderListAddress address = new OrderListAddress().id(UUID.fromString(e.getAddress().getUuid()))
                    .flatBuildingName(e.getAddress().getFlatBuildNumber())
                    .locality(e.getAddress().getLocality())
                    .city(e.getAddress().getCity())
                    .pincode(e.getAddress().getPincode())
                    .state(new OrderListAddressState().id(UUID.fromString(e.getAddress().getState().getUuid()))
                            .stateName(e.getAddress().getState().getStateName()));

            List<ItemQuantityResponse> itemQuantities = new ArrayList<ItemQuantityResponse>();
            for (OrderItemEntity ie:
                 e.getOrderItemEntity()) {
                itemQuantities.add(new ItemQuantityResponse().item(new ItemQuantityResponseItem()
                        .id(UUID.fromString(ie.getItem().getUuid()))
                        .itemName(ie.getItem().getItemName())
                        .itemPrice(ie.getItem().getPrice())
                ).price(ie.getPrice()).quantity(ie.getQuantity()));
            }

            OrderList temp = new OrderList().id(uuid).bill(bill).coupon(coupon)
                    .discount(discount)
                    .date(date)
                    .payment(payment)
                    .customer(customer)
                    .address(address)
                    .itemQuantities(itemQuantities);

            responseOrderList.add(temp);
        }

        return new ResponseEntity<List<OrderList>>(responseOrderList, HttpStatus.OK);

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
