package com.generation.progettospesupbackend.model.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Entity
@Getter
@Setter
public class User extends BaseEntity implements UserDetails
{
    @Column(unique = true)
    private String email;
    private String password;
    private String token;
    @Column(unique = true)
    private String username;


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<UserFavouriteProduct> favouriteProductEntries = new HashSet<>();
//    private Set<Product> favouriteProducts = new HashSet<>();

    @Transient
    public Map<Product, Supermarket> getFavouriteProducts() {
        Map<Product, Supermarket> map = new HashMap<>();
        for (UserFavouriteProduct entry : favouriteProductEntries) {
            map.put(entry.getProduct(), entry.getSupermarket());
        }
        return map;
    }

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER,cascade = CascadeType.ALL)
    private Set<ShoppingList> shoppingLists = new HashSet<>();


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }


}
