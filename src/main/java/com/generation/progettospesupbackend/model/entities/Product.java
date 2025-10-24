package com.generation.progettospesupbackend.model.entities;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Product extends BaseEntity
{
	@NotBlank @NotNull
	private String name;
	@NotNull @NotBlank @Enumerated(EnumType.STRING)
	private Category category;
	@NotNull @NotBlank
	private String description;
	private String imgUrl;

	@OneToMany(mappedBy = "product",fetch = FetchType.EAGER,cascade = CascadeType.ALL)
	private Set<PriceTrend> priceTrends = new HashSet<>();

	public void addPrice(PriceTrend p)
	{
		priceTrends.add(p);
		p.setProduct(this);
	}

}
