package com.generation.progettospesupbackend.model.entities;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@MappedSuperclass
public abstract class BaseEntity
{
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;
}
