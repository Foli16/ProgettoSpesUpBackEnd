package com.generation.progettospesupbackend.entities.repositories;

import com.generation.progettospesupbackend.entities.model.Supermarket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SupermarketRepository extends JpaRepository<Supermarket, UUID>
{
}
