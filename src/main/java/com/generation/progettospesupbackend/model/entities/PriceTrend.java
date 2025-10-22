package com.generation.progettospesupbackend.model.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
public class PriceTrend extends BaseEntity
{
	@PositiveOrZero
	private double price;
	@PositiveOrZero
	private double originalPrice;
	@PositiveOrZero
	private double pricePerType;
	private LocalDate startDate;
	private LocalDate endDate;

	@ManyToOne(fetch = FetchType.EAGER,cascade = CascadeType.ALL)
	private Product product;
	@ManyToOne(fetch = FetchType.EAGER,cascade = CascadeType.ALL)
	private Supermarket supermarket;
}
