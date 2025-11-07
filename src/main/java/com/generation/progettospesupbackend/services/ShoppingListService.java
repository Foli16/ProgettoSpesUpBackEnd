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
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
public class ShoppingListService
{
	@Autowired
	PriceTrendRepository ptRepo;
	@Autowired
	ShoppingListRepository shRepo;
	@Autowired
	UserService uServ;
	@Autowired
	SupermarketService supServ;

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

	public Map<String, Set<ProductDto>> getListsBySupermarket(String token)
	{
		User u = uServ.findUserByToken(token);
		Optional<ShoppingList> cartOpt = shRepo.findShoppingListByUserAndCart(u, true);

		if(cartOpt.isEmpty())
			return null;

		ShoppingList cart = cartOpt.get();

		Map<String, Set<ProductDto>> separatedLists = new HashMap<>();

		for(PriceTrend pt : cart.getProductsInList())
		{
			String supName = pt.getSupermarket().getName();
			if (!separatedLists.containsKey(supName))
				separatedLists.put(supName, new HashSet<>());

			separatedLists.get(supName).add(supServ.convertEntireProductToDto(pt));
		}

		return separatedLists;
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

	public Map<String, Set<ProductDto>> getComparisonLists(String token) {

		User user = uServ.findUserByToken(token);
		Optional<ShoppingList> cartOpt = shRepo.findShoppingListByUserAndCart(user, true);

		if (cartOpt.isEmpty()) {
			return null;
		}

		ShoppingList cart = cartOpt.get();

		//Questo Set include tutti i nomi dei supermercati che compaiono nel carrello
		//Es: Se non ci sono PriceTrend associati alla "Bennet" nel carrello, il nome non finirà in questo Set
		Set<String> supermarketsInCart = new HashSet<>();
		for(PriceTrend pt : cart.getProductsInList())
			supermarketsInCart.add(pt.getSupermarket().getName());


		//Questa mappa conterrà i PriceTrend (valori) divisi per nome Supermarket (chiave) che sono associati allo stesso singolo prodotto a cui è associato un singolo PT presente nel carrello
		//Es: Se un PT nel carrello è associato al prodotto "Fusilli Barilla", qui dentro finiranno tutti i PT anch'essi associati a questo prodotto, ma solo se appartengono a uno dei supermercati che compaiono nel carrello
		Map<String, Set<PriceTrend>> productsByMarket = new HashMap<>();
		//Questa mappa conterrà i PriceTrend che non hanno avuto un riscontro con quelli nel carrello, che saranno quindi definiti come "non trovati" nella lista di comparazione del sito
		//P.S. la mappa è sempre chiave: nome supermercato, valore: Set di suoi PT
		Map<String, Set<PriceTrend>> productsByMarketNotFound = new HashMap<>();

		//Ciclo tutti i PT del carrello
		for (PriceTrend cartPt : cart.getProductsInList()) {
			Product product = cartPt.getProduct();
			if (product == null) continue;
			//Per ogni PT del carrello ciclo ogni PT di ognuno dei supermercati che compaiono nel carrello
			for (String sName : supermarketsInCart)
			{
				boolean found = false;
				for (PriceTrend pt : ptRepo.findPriceTrendsBySupermarket_Name(sName))
				{
					//Se l'id del prodotto associato al PT nel carrello è lo stesso del prodotto associato al PT che sta venendo iterato in questo momento
					//(che deve essere anche active ndr) allora viene inserito nella mappa productsByMarket (quella dei prodotti trovati)
					//altrimenti, se non c'è alcun prodotto del supermercato in analisi che possiede lo stesso id, finirà nell'altra mappa (quella dei prodotti non trovati)
					if (pt.getProduct() != null && pt.isActive() && pt.getProduct().getId().equals(product.getId()))
					{
						productsByMarket.putIfAbsent(sName, new HashSet<>());
						productsByMarket.get(sName).add(pt);
						found = true;
					}
				}
				if(!found)
				{
					productsByMarketNotFound.putIfAbsent(sName, new HashSet<>());
					productsByMarketNotFound.get(sName).add(cartPt);
				}
			}
		}

		Map<String, Set<ProductDto>> results = new HashMap<>();
		for(String sName : supermarketsInCart)
		{
			//Prendo tutti i PriceTrend dentro la mappa productsByMarket con chiave sName e li converto in dto assegnando true alla proprietà found
			Set<ProductDto> productsFound = productsByMarket.get(sName).stream().map(pt -> {ProductDto dto = supServ.convertEntireProductToDto(pt); dto.setFound(true); return dto;}).collect(Collectors.toSet());
			//Prendo tutti i PriceTrend dentro la mappa productsByMarketNotFound con chiave sName e li converto in dto assegnando false alla proprietà found
			Set<ProductDto> productsNotFound = productsByMarketNotFound.get(sName).stream().map(pt -> {ProductDto dto = supServ.convertEntireProductToDto(pt); dto.setFound(false); return dto;}).collect(Collectors.toSet());
			//Unisco i due set precedenti e li inserisco in una mappa con chiave il nome del supermercato di appartenenza
			results.put(sName, Stream.concat(productsFound.stream(), productsNotFound.stream()).collect(Collectors.toSet()));
		}

		return results;
	}



}
