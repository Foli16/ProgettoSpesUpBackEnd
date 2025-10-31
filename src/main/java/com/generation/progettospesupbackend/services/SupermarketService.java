package com.generation.progettospesupbackend.services;

import com.generation.progettospesupbackend.model.dtos.SupermarketNameDto;
import com.generation.progettospesupbackend.model.dtos.ProductDto;
import com.generation.progettospesupbackend.model.entities.PriceTrend;
import com.generation.progettospesupbackend.model.entities.Supermarket;
import com.generation.progettospesupbackend.model.repositories.SupermarketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SupermarketService
{
	@Autowired
	private SupermarketRepository supRepo;

	public List<SupermarketNameDto> getSupermarketNames()
	{
		return supRepo.findAll().stream().map(s -> convertEntityToDto(s)).toList();
	}

	public List<ProductDto> getProductsBySupermarket(List<String> supermarketNames)
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
//		for(String name : supermarketNames)
//		{
//			supermarkets = supRepo.findAll().stream().filter(s -> s.getName().equals(name)).toList();
//		}
		List<ProductDto> dtos = new ArrayList<>();
		for(Supermarket s : supermarkets)
		{
			for(PriceTrend pt : s.getPriceTrends())
			{
				dtos.add(convertEntireProductToDto(pt));
			}
		}
//		for(Supermarket s : supermarkets)
//		{
//			dtos = s.getPriceTrends().stream().map(pt -> convertEntireProductToDto(pt)).toList();
//		}
		return dtos;
	}

	private SupermarketNameDto convertEntityToDto(Supermarket s)
	{
		SupermarketNameDto dto = new SupermarketNameDto();
		dto.setName(s.getName());
		return dto;
	}

	public ProductDto convertEntireProductToDto(PriceTrend pt)
	{
		ProductDto dto = new ProductDto();
		dto.setPriceTrendId(pt.getId());
		dto.setPrice(pt.getPrice());
		dto.setOriginalPrice(pt.getOriginalPrice());
		dto.setPricePerType(pt.getPricePerType());
		dto.setStartDate(pt.getStartDate());
		dto.setEndDate(pt.getEndDate());
		dto.setActive(pt.isActive());
		dto.setProductId(pt.getProduct().getId());
		dto.setProductName(pt.getProduct().getName());
		dto.setCategory(pt.getProduct().getCategory().toString());
		dto.setDescription(pt.getProduct().getDescription());
		dto.setImgUrl(pt.getProduct().getImgUrl());
		dto.setSupermarketId(pt.getSupermarket().getId());
		dto.setSupermarketName(pt.getSupermarket().getName());

		return dto;
	}
}
