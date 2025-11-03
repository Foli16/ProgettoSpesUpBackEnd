package com.generation.progettospesupbackend.services;

import com.generation.progettospesupbackend.model.dtos.ProductDto;
import com.generation.progettospesupbackend.model.dtos.ShoppingListDto;
import com.generation.progettospesupbackend.model.entities.PriceTrend;
import com.generation.progettospesupbackend.model.entities.Product;
import com.generation.progettospesupbackend.model.entities.ShoppingList;
import com.generation.progettospesupbackend.model.entities.User;
import com.generation.progettospesupbackend.model.repositories.PriceTrendRepository;
import com.generation.progettospesupbackend.model.repositories.ShoppingListRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;


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

	//Aggiunto
	public ShoppingListDto getCart(String token) {
		User u = uServ.findUserByToken(token);
		Optional<ShoppingList> sop = shRepo.findShoppingListByUserAndCart(u, true);

		ShoppingListDto dto = new ShoppingListDto();
		dto.setCart(true);

		if(sop.isPresent()) {
			ShoppingList sl = sop.get();
			Set<ProductDto> products = new HashSet<>();
			double total = 0;

			for(PriceTrend pt : sl.getProductsInList()) {
				if(pt.getProduct() != null && pt.getSupermarket() != null) {
					ProductDto pd = new ProductDto();
					pd.setProductId(pt.getProduct().getId());
					pd.setProductName(pt.getProduct().getName());
					pd.setPrice(pt.getPrice());
					pd.setSupermarketName(pt.getSupermarket().getName());
					products.add(pd);

					// totale del carrello
					total += pt.getPrice();
				}
			}

			dto.setProductList(products);
			dto.setTotal(total);
		}

		return dto;
	}


	//Comparazione
	public Map<String, Set<ProductDto>> compareCartWithOtherSm(String token) {
		User u = uServ.findUserByToken(token);
		Optional<ShoppingList> cartOpt = shRepo.findShoppingListByUserAndCart(u, true);

		Map<String, Set<ProductDto>> comparison = new HashMap<>();

		if(cartOpt.isEmpty()) {
			return comparison;
		}

		ShoppingList cart = cartOpt.get();

		// Creo una lista dei prodotti nel carrello
		List<Product> cartProducts = new ArrayList<>();
		for(PriceTrend pt : cart.getProductsInList()) {
			if(pt.getProduct() != null) {
				cartProducts.add(pt.getProduct());
			}
		}

		//  tutti i prodotti/PriceTrend disponibili
		List<PriceTrend> allPTrends = ptRepo.findAll();

		// confronto dei PriceTrend con i prodotti del carrello
		for(PriceTrend pt : allPTrends) {
			Product product = pt.getProduct();
			if(product != null && cartProducts.contains(product)) {
				String supermarketName = pt.getSupermarket().getName();

				ProductDto pd = new ProductDto();
				pd.setProductId(product.getId());
				pd.setProductName(product.getName());
				pd.setPrice(pt.getPrice());
				pd.setSupermarketName(supermarketName);

				// Aggiungo il prodotto alla mappa
				if(!comparison.containsKey(supermarketName)) {
					comparison.put(supermarketName, new HashSet<>());
				}
				comparison.get(supermarketName).add(pd);
			}
		}

		return comparison;
	}



	public Map<String, Double> getTotalsOfCart(String token) {
		User u = uServ.findUserByToken(token);
		Optional<ShoppingList> cartOpt = shRepo.findShoppingListByUserAndCart(u, true);

		// Mappa supermercato - totale
		Map<String, Double> totals = new HashMap<>();

		if(cartOpt.isEmpty()) {
			return totals; // se il carrello è vuoto
		}

		ShoppingList cart = cartOpt.get();

		// tutti i prodotti del carrello
		List<Product> cartProducts = new ArrayList<>();
		for(PriceTrend pt : cart.getProductsInList()) {
			if(pt.getProduct() != null) {
				cartProducts.add(pt.getProduct());
			}
		}

		List<PriceTrend> allPTrends = ptRepo.findAll();

		//  somma i prezzi per supermercato
		for(PriceTrend pt : allPTrends) {
			if(pt.getProduct() != null && cartProducts.contains(pt.getProduct())) {
				String supermarketName = pt.getSupermarket().getName();
				double price = pt.getPrice();

				// calcolo totale
				if(!totals.containsKey(supermarketName)) {
					totals.put(supermarketName, price);
				} else {
					totals.put(supermarketName, totals.get(supermarketName) + price);
				}
			}
		}

		return totals;
	}




}
