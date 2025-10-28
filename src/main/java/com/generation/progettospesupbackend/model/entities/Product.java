package com.generation.progettospesupbackend.model.entities;

import jakarta.persistence.*;

import java.time.LocalDate;
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
	@NotNull @Enumerated(EnumType.STRING)
	private Category category;
	@NotNull @NotBlank
	private String description;
	private String imgUrl;
	@ManyToMany(fetch = FetchType.EAGER, mappedBy = "productsInList")
	private Set<ShoppingList> shoppingLists = new HashSet<>();
	@ManyToMany(fetch = FetchType.EAGER, mappedBy = "favouriteProducts")
	private Set<User> users = new HashSet<>();
//	@NotNull @NotBlank
//	private String subCategory;

	@OneToMany(mappedBy = "product",fetch = FetchType.EAGER,cascade = CascadeType.ALL)
	private Set<PriceTrend> priceTrends = new HashSet<>();


	public void addPrice(PriceTrend p)
	{
		priceTrends.add(p);
		p.setProduct(this);
	}

	public PriceTrend getActivePrice(Supermarket sup)
	{
		for (PriceTrend pr : priceTrends)
		{
			if(pr.isActive() && pr.getSupermarket().equals(sup))
				return pr;
		}
		return null;
	}

}
