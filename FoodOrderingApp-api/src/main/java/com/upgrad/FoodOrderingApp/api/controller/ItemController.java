package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.ItemList;
import com.upgrad.FoodOrderingApp.api.model.ItemListResponse;
import com.upgrad.FoodOrderingApp.service.businness.RestaurantService;
import com.upgrad.FoodOrderingApp.service.entity.ItemEntity;
import com.upgrad.FoodOrderingApp.service.entity.RestaurantEntity;
import com.upgrad.FoodOrderingApp.service.exception.RestaurantNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api")      /* Setting base path to "/api" */
public class ItemController {

    @Autowired
    RestaurantService restaurantService;

    @GetMapping(path = "/item/restaurant/{restaurantId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<ItemListResponse> getItemsByPopularity(@PathVariable(name = "restaurantId") final String restaurantUuid) throws RestaurantNotFoundException {
        if (restaurantUuid.isEmpty()) throw new RestaurantNotFoundException("RNF-002", "Restaurant id field should not be empty");

        RestaurantEntity restaurant = restaurantService.getRestaurantByUuid(restaurantUuid);
        if (restaurant == null) throw new RestaurantNotFoundException("RNF-001", "No restaurant by this id");

        List<ItemEntity> itemList = restaurant.getItemList();

        Collections.sort(itemList);
        ItemListResponse response = new ItemListResponse();
        for(int i = 0; i < 5; i++) {    /* returning the top 5 items */
            ItemEntity item = itemList.get(i);
            ItemList itemL = new ItemList();
            itemL.setId(UUID.fromString(item.getUuid()));
            itemL.setItemName(item.getItemName());
            itemL.setPrice(item.getPrice());
            itemL.setItemType(ItemList.ItemTypeEnum.fromValue(item.getType()));
            response.add(itemL);
        }

        return new ResponseEntity<>(response, HttpStatus.OK);

    }
}
