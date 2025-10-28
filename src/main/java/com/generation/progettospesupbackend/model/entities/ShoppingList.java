package com.generation.progettospesupbackend.model.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


import java.util.HashSet;

import java.util.Set;

@Entity
@Getter
@Setter
public class ShoppingList extends BaseEntity
{
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "shoppingList_to_product",
            joinColumns = @JoinColumn(name = "shoppingList_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private Set<Product> productsInList = new HashSet<>();
    private boolean cart;

    @ManyToOne(fetch = FetchType.EAGER)
    private User user;
}
