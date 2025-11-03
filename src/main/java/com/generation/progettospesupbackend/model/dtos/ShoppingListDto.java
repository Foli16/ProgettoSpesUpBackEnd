package com.generation.progettospesupbackend.model.dtos;

import com.generation.progettospesupbackend.model.entities.Product;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class ShoppingListDto
{
    private Set<ProductDto> productList = new HashSet<>();
    private boolean cart;
    private double total;


}
