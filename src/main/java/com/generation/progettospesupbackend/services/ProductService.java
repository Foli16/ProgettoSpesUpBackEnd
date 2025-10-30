package com.generation.progettospesupbackend.services;

import com.generation.progettospesupbackend.model.dtos.ProductDto;
import com.generation.progettospesupbackend.model.entities.PriceTrend;
import com.generation.progettospesupbackend.model.entities.Supermarket;
import com.generation.progettospesupbackend.model.repositories.SupermarketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService
{
	@Autowired
	private SupermarketRepository supRepo;
	@Autowired
	private SupermarketService supServ;

	public List<ProductDto> getProductsByName(String searchedName, List<String> supermarketNames)
	{
		List<Supermarket> supermarkets = new ArrayList<>();
		for(String name : supermarketNames)
		{
			for(Supermarket sup : supRepo.findAll())
			{
				if(sup.getName().equals(name))
				{
					supermarkets.add(sup);
				}
			}
		}
		List<ProductDto> dtos = new ArrayList<>();
		for(Supermarket s : supermarkets)
		{
			for(PriceTrend pt : s.getPriceTrends())
			{
				if(pt.getProduct().getName().toLowerCase().contains(searchedName.toLowerCase()))
					dtos.add(supServ.convertEntireProductToDto(pt));
			}
		}
		return dtos;
	}

	public List<ProductDto> getProductsByCategory(String selectedCategory, List<String> supermarketNames)
	{
		List<Supermarket> supermarkets = new ArrayList<>();
		for(String name : supermarketNames)
		{
			for(Supermarket sup : supRepo.findAll())
			{
				if(sup.getName().equals(name))
				{
					supermarkets.add(sup);
				}
			}
		}
		List<ProductDto> dtos = new ArrayList<>();
		for(Supermarket s : supermarkets)
		{
			for(PriceTrend pt : s.getPriceTrends())
			{
				if(pt.getProduct().getCategory().toString().equals(selectedCategory))
					dtos.add(supServ.convertEntireProductToDto(pt));
			}
		}
		return dtos;
	}
}
