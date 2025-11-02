package com.generation.progettospesupbackend.controllers;

import com.generation.progettospesupbackend.services.ShoppingListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("api/shoppinglist")
public class ShoppingListController
{
	@Autowired
	ShoppingListService serv;

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
}