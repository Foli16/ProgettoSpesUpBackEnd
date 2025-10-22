package com.generation.progettospesupbackend.entities.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;

import java.time.LocalDate;

public class PriceTrend extends BaseEntity
{
	private double price;
	private double originalPrice;
	private double pricePerType;
	private LocalDate startDate;
	private LocalDate endDate;

	@ManyToOne(fetch = FetchType.EAGER,cascade = CascadeType.ALL)
	private Product product;
	@ManyToOne(fetch = FetchType.EAGER,cascade = CascadeType.ALL)
	private Supermarket supermarket;
}
