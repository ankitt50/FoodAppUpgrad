package com.upgrad.FoodOrderingApp.service.entity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "item")
@NamedQueries({
        @NamedQuery(name = "getItemByUuid", query = "select i from ItemEntity i where i.uuid =:uuid ")
})
public class ItemEntity implements Comparable<ItemEntity> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name="uuid")
    private String uuid;

    @Column(name="item_name")
    private String itemName;

    @Column(name="price")
    private int price;

    @Column(name="type")
    private String type;

    @ManyToMany(mappedBy = "itemList")
    private List<RestaurantEntity> restaurants;

    @ManyToMany(mappedBy = "items")
    private List<CategoryEntity> categories;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL)
    private List<OrderItemEntity> orderItemEntity = new ArrayList<>();

    public ItemEntity() {
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

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<RestaurantEntity> getRestaurants() {
        return restaurants;
    }

    public void setRestaurants(List<RestaurantEntity> restaurants) {
        this.restaurants = restaurants;
    }

    public List<CategoryEntity> getCategories() {
        return categories;
    }

    public void setCategories(List<CategoryEntity> categories) {
        this.categories = categories;
    }

    public List<OrderItemEntity> getOrderItemEntity() {
        return orderItemEntity;
    }

    public void setOrderItemEntity(List<OrderItemEntity> orderItemEntity) {
        this.orderItemEntity = orderItemEntity;
    }

    @Override
    public int compareTo(ItemEntity other) {
        return other.getOrderItemEntity().size() - this.getOrderItemEntity().size();
    }
}
