package com.generation.progettospesupbackend.model.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class ProductDto
{
	private UUID priceTrendId;
	private Double price;
	private Double originalPrice;
	private String pricePerType;
	private LocalDate startDate;
	private LocalDate endDate;
	private boolean active;
	private UUID productId;
	private String productName;
	private String category;
	private String description;
	private String imgUrl;
	private UUID supermarketId;
	private String supermarketName;
	private Boolean found;
}
