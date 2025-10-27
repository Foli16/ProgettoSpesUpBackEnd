package com.generation.progettospesupbackend.model.repositories;

import com.generation.progettospesupbackend.model.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID>
{

	boolean existsByImgUrlAndName(String imgUrl,String name);
	Product findByImgUrlAndName(String imgUrl,String name);
}
