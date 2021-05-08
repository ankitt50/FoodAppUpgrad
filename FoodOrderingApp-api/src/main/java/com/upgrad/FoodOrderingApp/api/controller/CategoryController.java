package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.CategoriesListResponse;
import com.upgrad.FoodOrderingApp.api.model.CategoryDetailsResponse;
import com.upgrad.FoodOrderingApp.api.model.CategoryListResponse;
import com.upgrad.FoodOrderingApp.api.model.ItemList;
import com.upgrad.FoodOrderingApp.service.businness.CategoryService;
import com.upgrad.FoodOrderingApp.service.entity.CategoryEntity;
import com.upgrad.FoodOrderingApp.service.exception.CategoryNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api")      /* Setting base path to "/api" */
public class CategoryController {

    @Autowired
    CategoryService categoryService;

    @GetMapping(path = "/category", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CategoriesListResponse> getAll() {
        List<CategoryEntity> categories = categoryService.getAll();
        CategoriesListResponse categoryList = new CategoriesListResponse();     /* Response entity created by Swagger plugin */

        for(CategoryEntity categoryEntity: categories) {
            CategoryListResponse category = new CategoryListResponse();
            category.setId(UUID.fromString(categoryEntity.getUuid()));
            category.setCategoryName(categoryEntity.getCategoryName());
            categoryList.addCategoriesItem(category);
        }

        return new ResponseEntity<>(categoryList, HttpStatus.OK);
    }

    @GetMapping(path="/category/{category_id}", produces=MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CategoryDetailsResponse> getCategoryById(@PathVariable(name="category_id") final String categoryId) throws CategoryNotFoundException {
        if (categoryId.isEmpty()) throw new CategoryNotFoundException("CNF-001", "Category id field should not be empty");

        CategoryEntity category = categoryService.getCategoryById(categoryId);
        if (category == null) throw new CategoryNotFoundException("CNF-002", "No category by this id");

        CategoryDetailsResponse response = new CategoryDetailsResponse();   /* Response entity created by Swagger plugin */
        response.setId(UUID.fromString(category.getUuid()));
        response.setCategoryName(category.getCategoryName());
        category.getItems().forEach(item -> {
            ItemList itemList = new ItemList();
            itemList.setId(UUID.fromString(item.getUuid()));
            itemList.setItemName(item.getItemName());
            itemList.setPrice(item.getPrice());
            itemList.setItemType(ItemList.ItemTypeEnum.fromValue(item.getType()));
            response.addItemListItem(itemList);
        });

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
