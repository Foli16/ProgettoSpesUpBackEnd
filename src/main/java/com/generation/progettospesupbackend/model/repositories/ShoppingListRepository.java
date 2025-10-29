package com.generation.progettospesupbackend.model.repositories;

import com.generation.progettospesupbackend.model.entities.ShoppingList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ShoppingListRepository extends JpaRepository<ShoppingList, UUID>
{

}
