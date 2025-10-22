package com.generation.progettospesupbackend.model.repositories;

import com.generation.progettospesupbackend.model.entities.PriceTrend;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PriceTrendRepository extends JpaRepository<PriceTrend, UUID>
{
}
