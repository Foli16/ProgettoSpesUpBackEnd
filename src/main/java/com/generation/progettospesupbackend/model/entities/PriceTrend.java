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
	private Double price;
	@PositiveOrZero
	private Double originalPrice;
	private String pricePerType;
	private LocalDate startDate;
	private LocalDate endDate;
	//private boolean active;

	@ManyToOne(fetch = FetchType.EAGER)
	private Product product;
	@ManyToOne(fetch = FetchType.EAGER)
	private Supermarket supermarket;

	//setter specifici nelle classi per fare le conversioni
	public void convertAndSetPrice(String price)
	{
		if(price == null)
		{
			this.price = null;
			return;
		}
		String[] splittato = price.split(",");
		this.price = Double.parseDouble(splittato[0] +"."+ splittato[1].substring(0,2));
	}

	public void convertAndSetOriginalPrice(String originalPrice)
	{
		if(originalPrice == null)
		{
			this.originalPrice = null;
			return;
		}
		String[] splittato = originalPrice.split(",");
		this.originalPrice = Double.parseDouble(splittato[0] +"."+ splittato[1].substring(0,2));
	}

	public boolean isActive()
	{
		return endDate == null;
	}
}
