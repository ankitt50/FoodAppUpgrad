package com.upgrad.FoodOrderingApp.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.upgrad.FoodOrderingApp.api.model.RestaurantList;
import com.upgrad.FoodOrderingApp.api.model.RestaurantListResponse;
import com.upgrad.FoodOrderingApp.service.businness.CategoryService;
import com.upgrad.FoodOrderingApp.service.businness.CustomerBusinessService;
//import com.upgrad.FoodOrderingApp.service.businness.CustomerService;
import com.upgrad.FoodOrderingApp.service.businness.ItemService;
import com.upgrad.FoodOrderingApp.service.businness.RestaurantService;
import com.upgrad.FoodOrderingApp.service.entity.*;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.CategoryNotFoundException;
import com.upgrad.FoodOrderingApp.service.exception.InvalidRatingException;
import com.upgrad.FoodOrderingApp.service.exception.RestaurantNotFoundException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

//import static com.upgrad.FoodOrderingApp.service.common.ItemType.NON_VEG;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.Assert.*;

// This class contains all the test cases regarding the restaurant controller
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class RestaurantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RestaurantService mockRestaurantService;

    @MockBean
    private ItemService mockItemService;

    @MockBean
    private CategoryService mockCategoryService;

    @MockBean
    private CustomerBusinessService mockCustomerService;

    // ------------------------------------------ GET /restaurant/{restaurant_id} ------------------------------------------

    //This test case passes when you get restaurant details based on restaurant id.
    @Test
    public void shouldGetRestaurantDetailsForCorrectRestaurantId() throws Exception {
        final RestaurantEntity restaurantEntity = getRestaurantEntity();
        when(mockRestaurantService.getRestaurantByUuid("someRestaurantId"))
                .thenReturn(restaurantEntity);

//        final CategoryEntity categoryEntity = getCategoryEntity();
//        when(mockCategoryService.getCategoriesByRestaurant("someRestaurantId"))
//                .thenReturn(Collections.singletonList(categoryEntity));
//
//        final ItemEntity itemEntity = getItemEntity();
//        when(mockItemService.getItemsByCategoryAndRestaurant("someRestaurantId", categoryEntity.getUuid()))
//                .thenReturn(Collections.singletonList(itemEntity));

        mockMvc
                .perform(get("/restaurant/someRestaurantId").contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(restaurantEntity.getUuid()))
                .andExpect(jsonPath("restaurant_name").value("Famous Restaurant"))
                .andExpect(jsonPath("customer_rating").value(3.4))
                .andExpect(jsonPath("number_customers_rated").value(200));
        verify(mockRestaurantService, times(1)).getRestaurantByUuid("someRestaurantId");
    }
//
//    //This test case passes when you have handled the exception of trying to fetch any restaurant but your restaurant id
//    // field is empty.
    @Test
    public void shouldNotGetRestaurantidIfRestaurantIdIsEmpty() throws Exception {
        when(mockRestaurantService.getRestaurantByUuid(anyString())).thenReturn(null);

        mockMvc
                .perform(get("/restaurant/emptyString").contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("code").value("RNF-001"));
        verify(mockRestaurantService, times(1)).getRestaurantByUuid(anyString());
    }
//
//    //This test case passes when you have handled the exception of trying to fetch restaurant details while there are
//    // no restaurants with that restaurant id.
    @Test
    public void shouldNotGetRestaurantDetailsIfRestaurantNotFoundForGivenRestaurantId() throws Exception {
        when(mockRestaurantService.getRestaurantByUuid(anyString())).thenReturn(null);

        mockMvc
                .perform(get("/restaurant/someRestaurantId").contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("code").value("RNF-001"));
        verify(mockRestaurantService, times(1)).getRestaurantByUuid(anyString());
    }
//
//    // ------------------------------------------ GET /restaurant/name/{restaurant_name} ------------------------------------------
//
//    //This test case passes when you are able to fetch restaurants by the name you provided.
    @Test
    public void shouldGetRestaurantDetailsByGivenName() throws Exception {
        RestaurantEntity restaurantEntity = getRestaurantEntity();
        restaurantEntity.setRestaurantName("someRestaurantName");
        List<RestaurantEntity> restaurantEntityList = new ArrayList<RestaurantEntity>();
        restaurantEntityList.add(restaurantEntity);
        when(mockRestaurantService.getAllRestaurants())
                .thenReturn(restaurantEntityList);

//        final CategoryEntity categoryEntity = getCategoryEntity();
//        when(mockCategoryService.getCategoriesByRestaurant(restaurantEntity.getUuid()))
//                .thenReturn(Collections.singletonList(categoryEntity));

        final String responseString = mockMvc
                .perform(get("/restaurant/name/someRestaurantName").contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        final RestaurantListResponse restaurantListResponse = new ObjectMapper().readValue(responseString, RestaurantListResponse.class);
        assertEquals(restaurantListResponse.getRestaurants().size(), 1);

        final RestaurantList restaurantList = restaurantListResponse.getRestaurants().get(0);
        assertEquals(restaurantList.getId().toString(), restaurantEntity.getUuid());
        assertEquals(restaurantList.getAddress().getId().toString(), restaurantEntity.getAddress().getUuid());
        assertEquals(restaurantList.getAddress().getState().getId().toString(), restaurantEntity.getAddress().getState().getUuid());

        verify(mockRestaurantService, times(1)).getAllRestaurants();
    }
//
//    //This test case passes when you have handled the exception of trying to fetch any restaurants but your restaurant name
//    // field is empty.
//    @Test
    public void shouldNotGetRestaurantByNameIfNameIsEmpty() throws Exception {
        when(mockRestaurantService.getRestaurantByUuid(anyString())).thenReturn(null);

        mockMvc
                .perform(get("/restaurant/name/"+"").contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("code").value("RNF-003"));
        verify(mockRestaurantService, times(1)).getRestaurantByUuid(anyString());
    }
//
//
//    // ------------------------------------------ GET /restaurant/category/{category_id} ------------------------------------------
//
//    //This test case passes when you are able to retrieve restaurant belonging to any particular categories.
    @Test
    public void shouldGetRestaurantDetailsByGivenCategoryId() throws Exception {
        final RestaurantEntity restaurantEntity = getRestaurantEntity();
        CategoryEntity categoryEntity = getCategoryEntity();
        categoryEntity.setRestaurants(Collections.singletonList(restaurantEntity));
        when(mockRestaurantService.getCategoryByUuid("someCategoryId"))
                .thenReturn(categoryEntity);

//        final CategoryEntity categoryEntity = getCategoryEntity();
//        when(mockCategoryService.getCategoriesByRestaurant(restaurantEntity.getUuid()))
//                .thenReturn(Collections.singletonList(categoryEntity));

        final String responseString = mockMvc
                .perform(get("/restaurant/category/someCategoryId").contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        final RestaurantListResponse restaurantListResponse = new ObjectMapper().readValue(responseString, RestaurantListResponse.class);
        assertEquals(restaurantListResponse.getRestaurants().size(), 1);

        final RestaurantList restaurantList = restaurantListResponse.getRestaurants().get(0);
        assertEquals(restaurantList.getId().toString(), restaurantEntity.getUuid());
        assertEquals(restaurantList.getAddress().getId().toString(), restaurantEntity.getAddress().getUuid());
        assertEquals(restaurantList.getAddress().getState().getId().toString(), restaurantEntity.getAddress().getState().getUuid());

        verify(mockRestaurantService, times(1)).getCategoryByUuid("someCategoryId");
    }
//
//    //This test case passes when you have handled the exception of trying to fetch any restaurants but your category id
//    // field is empty.
    @Test
    public void shouldNotGetRestaurantByCategoryidIfCategoryIdIsEmpty() throws Exception {
        when(mockRestaurantService.getCategoryByUuid(anyString())).thenReturn(null);

        mockMvc
                .perform(get("/restaurant/category/emptyString").contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("code").value("CNF-002"));
        verify(mockRestaurantService, times(1)).getCategoryByUuid(anyString());
    }
//
//    //This test case passes when you have handled the exception of trying to fetch any restaurant by its category id, while there
//    // is not category by that id in the database
    @Test
    public void shouldNotGetRestaurantsByCategoryIdIfCategoryDoesNotExistAgainstGivenId() throws Exception {
        when(mockRestaurantService.getCategoryByUuid("someCategoryId")).thenReturn(null);

        mockMvc
                .perform(get("/restaurant/category/someCategoryId").contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("code").value("CNF-002"));
        verify(mockRestaurantService, times(1)).getCategoryByUuid("someCategoryId");
    }
//
//
//    // ------------------------------------------ GET /restaurant ------------------------------------------
//
//    //This test case passes when you able to fetch the list of all restaurants.
    @Test
    public void shouldGetAllRestaurantDetails() throws Exception {
        final RestaurantEntity restaurantEntity = getRestaurantEntity();
        when(mockRestaurantService.getAllRestaurants()).thenReturn(Collections.singletonList(restaurantEntity));

//        final CategoryEntity categoryEntity = getCategoryEntity();
//        when(mockCategoryService.getCategoriesByRestaurant(restaurantEntity.getUuid()))
//                .thenReturn(Collections.singletonList(categoryEntity));

        final String responseString = mockMvc
                .perform(get("/restaurant").contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        final RestaurantListResponse restaurantListResponse = new ObjectMapper().readValue(responseString, RestaurantListResponse.class);
        assertEquals(restaurantListResponse.getRestaurants().size(), 1);

        final RestaurantList restaurantList = restaurantListResponse.getRestaurants().get(0);
        assertEquals(restaurantList.getId().toString(), restaurantEntity.getUuid());
        assertEquals(restaurantList.getAddress().getId().toString(), restaurantEntity.getAddress().getUuid());
        assertEquals(restaurantList.getAddress().getState().getId().toString(), restaurantEntity.getAddress().getState().getUuid());

        verify(mockRestaurantService, times(1)).getAllRestaurants();
    }
//
//
//    // ------------------------------------------ PUT /restaurant/{restaurant_id} ------------------------------------------
//
//    //This test case passes when you are able to update restaurant rating successfully.
    @Test
    public void shouldUpdateRestaurantRating() throws Exception {
        final String restaurantId = UUID.randomUUID().toString();

        when(mockCustomerService.checkAuthToken(anyString(),anyString()))
                .thenReturn(new CustomerEntity());

        final RestaurantEntity restaurantEntity = getRestaurantEntity();
        when(mockRestaurantService.getRestaurantByUuid(restaurantId)).thenReturn(restaurantEntity);

        RestaurantEntity restaurantEntity1 = new RestaurantEntity();
        restaurantEntity1.setUuid(UUID.randomUUID().toString());
        when(mockRestaurantService.updateRatingOfRestaurant(restaurantEntity))
                .thenReturn(restaurantEntity1);

        mockMvc
                .perform(put("/restaurant/edit/" + restaurantId + "?customer_rating=4")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .header("authorization", "Bearer database_accesstoken2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(restaurantEntity1.getUuid()));
        verify(mockCustomerService, times(1)).checkAuthToken(anyString(),anyString());
        verify(mockRestaurantService, times(1)).getRestaurantByUuid(restaurantId);
        verify(mockRestaurantService, times(1))
                .updateRatingOfRestaurant(restaurantEntity);
    }
//
//    //This test case passes when you have handled the exception of trying to update restaurant rating while you are
//    // not logged in.
    @Test
    public void shouldNotUpdateRestaurantRatingIfCustomerIsNotLoggedIn() throws Exception {
        when(mockCustomerService.checkAuthToken(anyString(),anyString()))
                .thenThrow(new AuthorizationFailedException("ATHR-001", "Customer is not Logged in."));

        mockMvc
                .perform(put("/restaurant/edit/someRestaurantId/?customer_rating=4")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .header("authorization", "Bearer invalid_auth"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("code").value("ATHR-001"));
        verify(mockCustomerService, times(1)).checkAuthToken(anyString(),anyString());
        verify(mockRestaurantService, times(0)).getRestaurantByUuid(anyString());
        verify(mockRestaurantService, times(0)).updateRatingOfRestaurant(new RestaurantEntity());
    }
//
//    //This test case passes when you have handled the exception of trying to update restaurant rating while you are
//    // already logged out.
    @Test
    public void shouldNotUpdateRestaurantRatingIfCustomerIsLoggedOut() throws Exception {
        when(mockCustomerService.checkAuthToken(anyString(),anyString()))
                .thenThrow(new AuthorizationFailedException("ATHR-002", "Customer is logged out. Log in again to access this endpoint."));

        mockMvc
                .perform(put("/restaurant/edit/someRestaurantId/?customer_rating=4")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .header("authorization", "Bearer invalid_auth"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("code").value("ATHR-002"));
        verify(mockCustomerService, times(1)).checkAuthToken(anyString(),anyString());
        verify(mockRestaurantService, times(0)).getRestaurantByUuid(anyString());
        verify(mockRestaurantService, times(0)).updateRatingOfRestaurant(new RestaurantEntity());
    }
//
//    //This test case passes when you have handled the exception of trying to update restaurant rating while your session
//    // is already expired.
    @Test
    public void shouldNotUpdateRestaurantRatingIfCustomerSessionIsExpired() throws Exception {
        when(mockCustomerService.checkAuthToken(anyString(),anyString()))
                .thenThrow(new AuthorizationFailedException("ATHR-003", "Your session is expired. Log in again to access this endpoint."));

        mockMvc
                .perform(put("/restaurant/edit/someRestaurantId/?customer_rating=4")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .header("authorization", "Bearer invalid_auth"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("code").value("ATHR-003"));
        verify(mockCustomerService, times(1)).checkAuthToken(anyString(),anyString());
        verify(mockRestaurantService, times(0)).getRestaurantByUuid(anyString());
        verify(mockRestaurantService, times(0)).updateRatingOfRestaurant(any());
    }
//
//    //This test case passes when you have handled the exception of trying to update any restaurant but your restaurant id
//    // field is empty.
    @Test
    public void shouldNotUpdateRestaurantIfRestaurantIdIsEmpty() throws Exception {
        when(mockCustomerService.checkAuthToken(anyString(),anyString()))
                .thenReturn(new CustomerEntity());

        when(mockRestaurantService.getRestaurantByUuid(anyString())).thenReturn(null);

        mockMvc
                .perform(get("/restaurant/emptyString").contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .header("authorization", "Bearer database_accesstoken2"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("code").value("RNF-001"));
        verify(mockCustomerService, times(0)).checkAuthToken(anyString(),anyString());
        verify(mockRestaurantService, times(1)).getRestaurantByUuid(anyString());
    }
//
//    //This test case passes when you have handled the exception of trying to update restaurant rating while the
//    // restaurant id you provided does not exist in the database.
    @Test
    public void shouldNotUpdateRestaurantRatingIfRestaurantDoesNotExists() throws Exception {
        final String restaurantId = UUID.randomUUID().toString();

        when(mockCustomerService.checkAuthToken(anyString(),anyString()))
                .thenReturn(new CustomerEntity());

        when(mockRestaurantService.getRestaurantByUuid(restaurantId)).thenReturn(null);

        mockMvc
                .perform(put("/restaurant/edit/" + restaurantId + "?customer_rating=4")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .header("authorization", "Bearer database_accesstoken2"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("code").value("RNF-001"));
        verify(mockCustomerService, times(1)).checkAuthToken(anyString(),anyString());
        verify(mockRestaurantService, times(1)).getRestaurantByUuid(restaurantId);
        verify(mockRestaurantService, times(0))
                .updateRatingOfRestaurant(any());
    }
//
//    //This test case passes when you have handled the exception of trying to update restaurant rating while the rating
//    // you provided is less than 1.
    @Test
    public void shouldNotUpdateRestaurantRatingIfNewRatingIsLessThan1() throws Exception {
        final String restaurantId = UUID.randomUUID().toString();

        when(mockCustomerService.checkAuthToken(anyString(),anyString()))
                .thenReturn(new CustomerEntity());

        final RestaurantEntity restaurantEntity = getRestaurantEntity();
        when(mockRestaurantService.getRestaurantByUuid(restaurantId)).thenReturn(restaurantEntity);

//        when(mockRestaurantService.updateRestaurantRating(restaurantEntity, -5.5))
//                .thenThrow(new InvalidRatingException("IRE-001", "Rating should be in the range of 1 to 5"));

        mockMvc
                .perform(put("/restaurant/edit/" + restaurantId + "?customer_rating=-5")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .header("authorization", "Bearer database_accesstoken2"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("code").value("IRE-001"));
        verify(mockCustomerService, times(1)).checkAuthToken(anyString(),anyString());
        verify(mockRestaurantService, times(1)).getRestaurantByUuid(restaurantId);
    }
//
//    //This test case passes when you have handled the exception of trying to update restaurant rating while the rating
//    // you provided is greater than 5.
    @Test
    public void shouldNotUpdateRestaurantRatingIfNewRatingIsGreaterThan5() throws Exception {
        final String restaurantId = UUID.randomUUID().toString();

        when(mockCustomerService.checkAuthToken(anyString(),anyString()))
                .thenReturn(new CustomerEntity());

        final RestaurantEntity restaurantEntity = getRestaurantEntity();
        when(mockRestaurantService.getRestaurantByUuid(restaurantId)).thenReturn(restaurantEntity);

//        when(mockRestaurantService.updateRestaurantRating(restaurantEntity, 5.5))
//                .thenThrow(new InvalidRatingException("IRE-001", "Rating should be in the range of 1 to 5"));

        mockMvc
                .perform(put("/restaurant/edit/" + restaurantId + "?customer_rating=6")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .header("authorization", "Bearer database_accesstoken2"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("code").value("IRE-001"));
        verify(mockCustomerService, times(1)).checkAuthToken(anyString(),anyString());
        verify(mockRestaurantService, times(1)).getRestaurantByUuid(restaurantId);
    }
//
//    // ------------------------------------------ POJO builders ------------------------------------------
//
    private ItemEntity getItemEntity() {
        final ItemEntity itemEntity = new ItemEntity();
        final String itemId = UUID.randomUUID().toString();
        itemEntity.setUuid(itemId);
        itemEntity.setItemName("someItem");
        itemEntity.setType("NON_VEG");
        itemEntity.setPrice(200);
        return itemEntity;
    }
//
    private CategoryEntity getCategoryEntity() {
        final CategoryEntity categoryEntity = new CategoryEntity();
        final String categoryId = UUID.randomUUID().toString();
        categoryEntity.setUuid(categoryId);
        categoryEntity.setCategoryName("someCategory");
        return categoryEntity;
    }
//
    private RestaurantEntity getRestaurantEntity() {
        final String stateId = UUID.randomUUID().toString();
        //tateId, "someState"
        final StateEntity stateEntity = new StateEntity();
        stateEntity.setUuid(stateId);
        stateEntity.setStateName("someState");
        final String addressId = UUID.randomUUID().toString();
        //addressId, "a/b/c", "someLocality", "someCity", "100000", stateEntity
        final AddressEntity addressEntity = new AddressEntity();
        addressEntity.setUuid(addressId);
        addressEntity.setFlatBuildNumber("a/b/c");
        addressEntity.setCity("someCity");
        addressEntity.setLocality("someLocality");
        addressEntity.setPincode("100000");
        addressEntity.setState(stateEntity);

        final RestaurantEntity restaurantEntity = new RestaurantEntity();
        final String restaurantId = UUID.randomUUID().toString();
        restaurantEntity.setUuid(restaurantId);
        restaurantEntity.setAddress(addressEntity);
        restaurantEntity.setAveragePriceForTwo(123);
        restaurantEntity.setCustomerRating(3.4);
        restaurantEntity.setNumberOfCustomersRated(200);
        restaurantEntity.setPhotoUrl("someurl");
        restaurantEntity.setRestaurantName("Famous Restaurant");
        List<ItemEntity> itemEntityList = new ArrayList<ItemEntity>();
        ItemEntity itemEntity = getItemEntity();
        itemEntityList.add(itemEntity);
        List<CategoryEntity> categoryEntityList = new ArrayList<CategoryEntity>();
        CategoryEntity categoryEntity = getCategoryEntity();
        categoryEntity.setItems(itemEntityList);
        categoryEntityList.add(categoryEntity);
        restaurantEntity.setCategoryList(categoryEntityList);



        return restaurantEntity;
    }
}