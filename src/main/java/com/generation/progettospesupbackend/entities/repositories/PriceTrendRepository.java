package com.generation.progettospesupbackend.entities.repositories;

import com.generation.progettospesupbackend.entities.model.PriceTrend;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PriceTrendRepository extends JpaRepository<PriceTrend, UUID>
{
}
