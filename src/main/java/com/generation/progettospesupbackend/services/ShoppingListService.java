package com.generation.progettospesupbackend.services;

import com.generation.progettospesupbackend.model.dtos.ProductDto;
import com.generation.progettospesupbackend.model.entities.PriceTrend;
import com.generation.progettospesupbackend.model.entities.ShoppingList;
import com.generation.progettospesupbackend.model.entities.User;
import com.generation.progettospesupbackend.model.repositories.PriceTrendRepository;
import com.generation.progettospesupbackend.model.repositories.ShoppingListRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ShoppingListService
{
	@Autowired
	PriceTrendRepository ptRepo;
	@Autowired
	ShoppingListRepository shRepo;
	@Autowired
	UserService uServ;

	public void addToCart(String token, UUID priceTrendId)
	{
		Optional<PriceTrend> op = ptRepo.findById(priceTrendId);
		if(op.isEmpty())
			return;
		PriceTrend pt = op.get();
		User u = uServ.findUserByToken(token);
		Optional<ShoppingList> sop = shRepo.findShoppingListByUserAndCart(u, true);
		if(sop.isEmpty())
		{
			ShoppingList list = new ShoppingList();
			list.setCart(true);
			list.setUser(u);
			u.getShoppingLists().add(list);
			list.getProductsInList().add(pt);
			shRepo.save(list);
		}
		else {
			ShoppingList sl = sop.get();
			sl.getProductsInList().add(pt);
			shRepo.save(sl);
		}
	}

	public void removeFromCart(String token, UUID priceTrendId)
	{
		User u = uServ.findUserByToken(token);
		Optional<ShoppingList> sop = shRepo.findShoppingListByUserAndCart(u, true);
		if(sop.isEmpty())
			return;
		ShoppingList sl = sop.get();
		for(PriceTrend trend : sl.getProductsInList())
			if(trend.getId().equals(priceTrendId))
			{
				sl.getProductsInList().remove(trend);
				shRepo.save(sl);
			}
		if(sl.getProductsInList().isEmpty())
			shRepo.delete(sl);
	}

}
