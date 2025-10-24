package com.generation.progettospesupbackend.model.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Supermarket extends BaseEntity
{
	@NotNull @NotBlank
	private String name;
	@NotNull @NotBlank
	private String locationUrl;
	@NotNull @NotBlank
	private String storeUrl;
	@NotNull
	private Set<String> subcategoryUrls = new HashSet<>();

	@OneToMany(mappedBy = "supermarket",fetch = FetchType.EAGER,cascade = CascadeType.ALL)
	private Set<PriceTrend> priceTrends = new HashSet<>();

	public void addPrice(PriceTrend p)
	{
		priceTrends.add(p);
		p.setSupermarket(this);
	}

}
