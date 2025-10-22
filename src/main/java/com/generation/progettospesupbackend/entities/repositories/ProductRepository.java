package com.generation.progettospesupbackend.entities.repositories;

import com.generation.progettospesupbackend.entities.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID>
{
}
