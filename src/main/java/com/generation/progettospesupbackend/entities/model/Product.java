package com.generation.progettospesupbackend.entities.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;

import java.util.Set;

public class Product extends BaseEntity
{
	private String name;
	private Category category;
	private String description;
	private String imgUrl;

	@OneToMany(mappedBy = "product",fetch = FetchType.EAGER,cascade = CascadeType.ALL)
	private Set<PriceTrend> priceTrends;

}
