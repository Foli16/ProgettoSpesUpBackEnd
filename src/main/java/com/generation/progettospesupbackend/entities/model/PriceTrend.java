package com.generation.progettospesupbackend.entities.model;

import java.time.LocalDate;

public class PriceTrend extends BaseEntity
{
	private double price;
	private double originalPrice;
	private double pricePerType;
	private LocalDate startDate;
	private LocalDate endDate;
}
