package com.generation.progettospesupbackend.model.dtos;


import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class SupermarketNameDto
{
	private UUID id;
	private String name;
}
