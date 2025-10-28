package com.generation.progettospesupbackend.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig
{
    @Autowired
    UserFilter filtro;

//    @Bean
//    public SecurityFilterChain sicurezza(HttpSecurity http) throws Exception
//    {
//        http
//        .csrf(csrf -> csrf.disable())
//        .authorizeHttpRequests(auth->
//
//                auth
//                .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()//tutti, no controlli
//                .requestMatchers("/api/auth/userinformation").hasRole("STANDARD")
//                .requestMatchers("/api/adminController/**").hasRole("ADMIN")//tutti i metodi che hanno come prefisso /api/adminController/** li puoi richiamare solo se sei loggato come admin
//                .requestMatchers(HttpMethod.POST).authenticated()
//                .requestMatchers(HttpMethod.PUT).authenticated()
//                .requestMatchers(HttpMethod.DELETE).authenticated()
//                .anyRequest().permitAll()//devi essere loggato
//
//        ).addFilterBefore(filtro, UsernamePasswordAuthenticationFilter.class);//dove viene fatto, sempre uguale
//
//        return http.build();
//    }

    //creo bean del criptatore da autowirare
    @Bean
    public PasswordEncoder getCypher()
    {
        return new BCryptPasswordEncoder();
    }
}
