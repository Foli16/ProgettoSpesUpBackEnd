package com.generation.progettospesupbackend.model.repositories;

import com.generation.progettospesupbackend.model.entities.ShoppingList;
import com.generation.progettospesupbackend.model.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ShoppingListRepository extends JpaRepository<ShoppingList, UUID>
{
	Optional<ShoppingList> findShoppingListByUserAndCart(User user, boolean cart);
}
