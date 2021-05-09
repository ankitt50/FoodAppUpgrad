package com.upgrad.FoodOrderingApp.api.controller;


import com.upgrad.FoodOrderingApp.api.model.*;
import com.upgrad.FoodOrderingApp.service.businness.*;
import com.upgrad.FoodOrderingApp.service.entity.*;
import com.upgrad.FoodOrderingApp.service.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@RestController
public class OrderController {
    @Autowired
    private CustomerBusinessService customerBusinessService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private AddressBusinessService addressBusinessService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private RestaurantService restaurantService;
    @Autowired
    private ItemService itemService;

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
    public ResponseEntity<CustomerOrderResponse> getPastOrdersOfUsers(@RequestHeader(name = "authorization") final String authToken) throws AuthorizationFailedException {
        String token = getToken(authToken);
        CustomerEntity customerEntity = customerBusinessService.checkAuthToken(token, "/order");
        List<OrderEntity> ordersForCustomer = orderService.getPastOrdersOfUsers(customerEntity);

        if(ordersForCustomer == null || ordersForCustomer.isEmpty()) {
            return new ResponseEntity<CustomerOrderResponse>(new CustomerOrderResponse().orders(new ArrayList<OrderList>()),HttpStatus.OK);
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

        return new ResponseEntity<CustomerOrderResponse>(new CustomerOrderResponse().orders(responseOrderList), HttpStatus.OK);

    }

    @PostMapping(path = "/order", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)

    public ResponseEntity<SaveOrderResponse> saveOrder(@RequestHeader(name = "authorization") final String authToken, @RequestBody final SaveOrderRequest saveOrderRequest) throws AuthorizationFailedException, CouponNotFoundException, AddressNotFoundException, PaymentMethodNotFoundException, RestaurantNotFoundException, ItemNotFoundException {
        String token = getToken(authToken);
        CustomerEntity customerEntity = customerBusinessService.checkAuthToken(token, "/order");
        CouponEntity couponEntity = null;
        AddressEntity addressEntity = null;
        PaymentEntity paymentEntity = null;
        RestaurantEntity restaurant = null;
        if(saveOrderRequest.getCouponId()!=null && !saveOrderRequest.getCouponId().toString().equals("")) {
            couponEntity = orderService.getCouponByUuid(saveOrderRequest.getCouponId().toString());
            if(couponEntity == null) {
                throw new CouponNotFoundException("CPF-002","No coupon by this id");
            }
        }
        else {
            throw new CouponNotFoundException("CPF-002","No coupon by this id");
        }

        if(saveOrderRequest.getAddressId()!=null && !saveOrderRequest.getAddressId().equals("")) {

            addressEntity = addressBusinessService.getAddressByUuid(saveOrderRequest.getAddressId());

            if(addressEntity==null) {
                throw new AddressNotFoundException("ANF-003","No address by this id");
            }


            // check if address belongs to the user

            boolean anyUserMatched = false;
            if(addressEntity.getCustomers().isEmpty()) {
                throw new AuthorizationFailedException("ATHR-004","You are not authorized to view/update/delete any one else's address");
            }
            else {
                for (CustomerEntity e:
                        addressEntity.getCustomers()) {
                    if(customerEntity.getContactNumber().equals(e.getContactNumber())) {
                        anyUserMatched = true;
                    }
                }
                if (anyUserMatched) {

                }
                else {
                    throw new AuthorizationFailedException("ATHR-004","You are not authorized to view/update/delete any one else's address");
                }
            }
        }
        else {
            throw new AddressNotFoundException("ANF-003","No address by this id");
        }

        if(saveOrderRequest.getPaymentId()!=null && !saveOrderRequest.getPaymentId().toString().equals("")) {
            List<PaymentEntity> paymentMethods = paymentService.getAllPaymentMethods();
            for (PaymentEntity paymentMethod : paymentMethods) {
                if(paymentMethod.getUuid().toString().equals(saveOrderRequest.getPaymentId().toString())) {
                    paymentEntity = paymentMethod;
                }
            }
            if(paymentEntity==null) {
                throw new PaymentMethodNotFoundException("PNF-002","No payment method found by this id");
            }
        }
        else {
            throw new PaymentMethodNotFoundException("PNF-002","No payment method found by this id");
        }

        if(saveOrderRequest.getRestaurantId()!=null && !saveOrderRequest.getRestaurantId().toString().equals("")){
            restaurant = restaurantService.getRestaurantByUuid(saveOrderRequest.getRestaurantId().toString());
            if (restaurant==null) {
                throw new RestaurantNotFoundException("RNF-001","No restaurant by this id");
            }
        }
        else {
            throw new RestaurantNotFoundException("RNF-001","No restaurant by this id");
        }

        OrderEntity newOrderToBeSaved = new OrderEntity();
        newOrderToBeSaved.setUuid(UUID.randomUUID().toString());
        newOrderToBeSaved.setCoupon(couponEntity);
        newOrderToBeSaved.setDate(LocalDateTime.now());
        newOrderToBeSaved.setCustomer(customerEntity);
        newOrderToBeSaved.setAddress(addressEntity);
        newOrderToBeSaved.setRestaurant(restaurant);
        newOrderToBeSaved.setPayment(paymentEntity);


        int bill = 0;
        if (saveOrderRequest.getItemQuantities()!=null && !saveOrderRequest.getItemQuantities().isEmpty()) {
            for (ItemQuantity q:
                    saveOrderRequest.getItemQuantities()) {
                bill = bill+(q.getPrice()*q.getQuantity());
            }
        }
        else {
        }

        newOrderToBeSaved.setDiscount((bill*couponEntity.getPercent())/100);
        newOrderToBeSaved.setBill(bill);

        List<OrderItemEntity> orderItemObjects = new ArrayList<OrderItemEntity>();
        if (saveOrderRequest.getItemQuantities()!=null && !saveOrderRequest.getItemQuantities().isEmpty()) {
            for (ItemQuantity q:
                 saveOrderRequest.getItemQuantities()) {
                if(q.getItemId().toString()==null || q.getItemId().toString().equals("")) {
                    throw new ItemNotFoundException("INF-003","No item by this id exist");
                }
                ItemEntity item = itemService.getItemByUuid(q.getItemId().toString());
                if(item==null) {
                    throw new ItemNotFoundException("INF-003","No item by this id exist");
                }
                OrderItemEntity orderItemObject = new OrderItemEntity();
                orderItemObject.setQuantity(q.getQuantity());
                orderItemObject.setPrice(q.getPrice());
                orderItemObject.setOrder(newOrderToBeSaved);
                orderItemObject.setItem(item);

                orderItemObjects.add(orderItemObject);
            }
        }
        else {
            throw new ItemNotFoundException("INF-003","No item by this id exist");
        }

        newOrderToBeSaved.setOrderItemEntity(orderItemObjects);

        for (OrderItemEntity e:
             orderItemObjects) {
            OrderItemEntity orderItemEntityAfterSaving = orderService.saveOrder(e);
        }

        return new ResponseEntity<SaveOrderResponse>(new SaveOrderResponse().id(newOrderToBeSaved.getUuid()).status("ORDER SUCCESSFULLY PLACED"),HttpStatus.CREATED);

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
