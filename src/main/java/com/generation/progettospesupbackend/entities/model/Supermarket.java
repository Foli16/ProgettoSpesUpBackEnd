package com.generation.progettospesupbackend.entities.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;

import java.util.Set;

public class Supermarket extends BaseEntity
{
	private String name;

	@OneToMany(mappedBy = "supermarket",fetch = FetchType.EAGER,cascade = CascadeType.ALL)
	private Set<PriceTrend> priceTrends;

}
