package com.generation.progettospesupbackend.controllers;

import com.generation.progettospesupbackend.model.dtos.SupermarketNameDto;
import com.generation.progettospesupbackend.model.dtos.ProductDto;
import com.generation.progettospesupbackend.services.ProductService;
import com.generation.progettospesupbackend.services.SupermarketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/search")
public class SearchController
{
	@Autowired
	private SupermarketService supServ;
	@Autowired
	private ProductService prodServ;

	@GetMapping("allstores")
	public List<SupermarketNameDto> getAllSupermarkets()
	{
		return supServ.getSupermarketNames();
	}

	@PostMapping("selectedstores")
	public List<ProductDto> getSelectedSupermarketProducts(@RequestBody List<String> names)
	{
		return supServ.getProductsBySupermarket(names);
	}

	@PostMapping("{name}")
	public List<ProductDto> getProductsByName(@PathVariable String name, @RequestBody List<String> supermarketNames)
	{
		return prodServ.getProductsByName(name, supermarketNames);
	}

	@PostMapping("/category/{cat}")
	public List<ProductDto> getProductsByCategory(@PathVariable String cat, @RequestBody List<String> supermarketNames)
	{
		return prodServ.getProductsByCategory(cat, supermarketNames);
	}
}
