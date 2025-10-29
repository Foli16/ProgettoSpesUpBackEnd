package com.generation.progettospesupbackend.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SupermercatoDto {
    private String nome;
    private double latitudine;
    private double longitudine;
}
