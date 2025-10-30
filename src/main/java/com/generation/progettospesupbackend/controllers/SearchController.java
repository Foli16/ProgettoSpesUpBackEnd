package com.generation.progettospesupbackend.controllers;

import com.generation.progettospesupbackend.model.dtos.SupermarketNameDto;
import com.generation.progettospesupbackend.model.dtos.ProductDto;
import com.generation.progettospesupbackend.services.SupermarketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/search")
public class SearchController
{
	@Autowired
	private SupermarketService supServ;

	@GetMapping("allstores")
	public List<SupermarketNameDto> getAllSupermarkets()
	{
		return supServ.getSupermarketNames();
	}

	@GetMapping("selectedstores")
	public List<ProductDto> getSelectedSupermarketProducts(@RequestBody List<String> names)
	{
		return supServ.getProductsBySupermarket(names);
	}

}
