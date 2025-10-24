package com.generation.progettospesupbackend.model.repositories;

import com.generation.progettospesupbackend.model.entities.Supermarket;
import net.bytebuddy.implementation.bind.annotation.Super;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SupermarketRepository extends JpaRepository<Supermarket, UUID>
{
}
