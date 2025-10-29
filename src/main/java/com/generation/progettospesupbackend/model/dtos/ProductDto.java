package com.generation.progettospesupbackend.model.dtos;


import com.generation.progettospesupbackend.model.entities.PriceTrend;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
public class ProductDto
{
    private UUID id;
    private String name;
    private String category;
    private String description;
    private String imgUrl;
    private Set<PriceTrend> priceTrends;
}
