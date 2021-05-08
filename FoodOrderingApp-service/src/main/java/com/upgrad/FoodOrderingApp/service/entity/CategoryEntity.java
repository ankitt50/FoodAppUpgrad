package com.upgrad.FoodOrderingApp.service.entity;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="category")
@NamedQueries({@NamedQuery(name = "getCategoryByUuid", query = "select c from CategoryEntity c where c.uuid=:uuid "),
               @NamedQuery(name="getAll", query="select c from CategoryEntity c")})
public class CategoryEntity implements Comparable<CategoryEntity> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "uuid")
    private String uuid;

    @Column(name = "category_name")
    private String categoryName;

    @ManyToMany(mappedBy = "categoryList")
    private List<RestaurantEntity> restaurants;

    @ManyToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
    @JoinTable(
            name = "category_item",
            joinColumns = { @JoinColumn(name = "category_id") },
            inverseJoinColumns = { @JoinColumn(name = "item_id") }
    )
    private List<ItemEntity> items;

    @Override
    public String toString() {
        return categoryName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public List<RestaurantEntity> getRestaurants() {
        return restaurants;
    }

    public void setRestaurants(List<RestaurantEntity> restaurants) {
        this.restaurants = restaurants;
    }

    public List<ItemEntity> getItems() {
        return items;
    }

    public void setItems(List<ItemEntity> items) {
        this.items = items;
    }

    @Override
    public int compareTo(CategoryEntity other) {
        return categoryName.compareTo(other.categoryName);
    }
}
