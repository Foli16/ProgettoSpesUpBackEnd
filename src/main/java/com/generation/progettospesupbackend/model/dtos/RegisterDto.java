package com.generation.progettospesupbackend.model.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterDto
{
    private String email;
    private String password;
    private String username;
}
