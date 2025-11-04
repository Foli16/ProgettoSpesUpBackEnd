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
		PriceTrend daRim = null;
		for(PriceTrend trend : sl.getProductsInList())
			if(trend.getId().equals(priceTrendId))
			{
//				sl.getProductsInList().remove(trend);
//				shRepo.save(sl);
				daRim = trend;

			}
		sl.getProductsInList().remove(daRim);
		shRepo.save(sl);
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
					pd.setPriceTrendId(pt.getId());
					pd.setImgUrl(pt.getProduct().getImgUrl());
					pd.setPricePerType(pt.getPricePerType());
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
				pd.setPricePerType(pt.getPricePerType());
				pd.setImgUrl(product.getImgUrl());

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

	public Map<String, Object> getBestSupermarketCartForUserSelection(String token, List<String> selectedMarkets) {

		User user = uServ.findUserByToken(token);
		Optional<ShoppingList> cartOpt = shRepo.findShoppingListByUserAndCart(user, true);

		if (cartOpt.isEmpty()) {
			return null;
		}

		ShoppingList cart = cartOpt.get();
		List<PriceTrend> allTrends = ptRepo.findAll();

		// Mappa supermarket -> (set di PriceTrend + totale)
		Map<String, Set<PriceTrend>> productsByMarket = new HashMap<>();
		Map<String, Double> totalByMarket = new HashMap<>();

		for (PriceTrend cartPt : cart.getProductsInList()) {
			Product product = cartPt.getProduct();
			if (product == null) continue;

			for (PriceTrend pt : allTrends) {

				if (pt.getProduct() != null && pt.getProduct().getId().equals(product.getId())) {

					String smName = pt.getSupermarket().getName();

					// consideriamo SOLO i supermercati selezionati dall'utente
					if (!selectedMarkets.contains(smName)) continue;

					productsByMarket.putIfAbsent(smName, new HashSet<>());
					productsByMarket.get(smName).add(pt);

					totalByMarket.put(smName,
							totalByMarket.getOrDefault(smName, 0.0) + pt.getPrice());
				}
			}
		}

		// Trova il supermercato con il totale più basso
		String bestMarket = null;
		double lowest = Double.MAX_VALUE;

		for (Map.Entry<String, Double> entry : totalByMarket.entrySet()) {
			if (entry.getValue() < lowest) {
				lowest = entry.getValue();
				bestMarket = entry.getKey();
			}
		}

		if (bestMarket == null) {
			return null;
		}

		// Trasformo PriceTrend -> ProductDto per output pulito
		Set<ProductDto> productsDto = new HashSet<>();
		for (PriceTrend pt : productsByMarket.get(bestMarket)) {
			ProductDto dto = new ProductDto();
			dto.setProductId(pt.getProduct().getId());
			dto.setProductName(pt.getProduct().getName());
			dto.setPrice(pt.getPrice());
			dto.setSupermarketName(pt.getSupermarket().getName());
			dto.setPriceTrendId(pt.getId());
			productsDto.add(dto);
		}

		Map<String, Object> result = new HashMap<>();
		result.put("bestSupermarket", bestMarket);
		result.put("total", lowest);
		result.put("products", productsDto);

		return result;
	}



}
