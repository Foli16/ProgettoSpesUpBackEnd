package com.generation.progettospesupbackend.controllers;

import com.generation.progettospesupbackend.model.dtos.ProductDto;
import com.generation.progettospesupbackend.model.dtos.ShoppingListDto;
import com.generation.progettospesupbackend.services.ShoppingListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("api/shoppinglist")
public class ShoppingListController
{
	@Autowired
	ShoppingListService serv;

	@Autowired
	private ShoppingListService shoppingListService;

	@PostMapping("/add/{priceTrendId}")
	public void add(@CookieValue(required = false) String token, @PathVariable UUID priceTrendId)
	{
		serv.addToCart(token, priceTrendId);
	}

	@DeleteMapping("/remove/{priceTrendId}")
	public void remove(@CookieValue(required = false) String token, @PathVariable UUID priceTrendId)
	{
		serv.removeFromCart(token, priceTrendId);
	}

	//Aggiunto
	@GetMapping("/cart")
	public ShoppingListDto getCart(@CookieValue(required = false) String token) {
		return serv.getCart(token);
	}


	@GetMapping("/compare")
	public Map<String, Set<ProductDto>> compareCart(
			@RequestHeader("Authorization") String token) {
		// su postman in headers key = authorization e value il token utente
		return shoppingListService.compareCartWithOtherSm(token);
	}

	@GetMapping("/totals")
	public Map<String, Double> getTotalsOfCart(@CookieValue(required = false) String token) {
		return shoppingListService.getTotalsOfCart(token);
	}

}