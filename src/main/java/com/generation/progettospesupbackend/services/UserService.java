package com.generation.progettospesupbackend.services;

import com.generation.progettospesupbackend.exceptions.InvalidCredentials;
import com.generation.progettospesupbackend.model.dtos.*;
import com.generation.progettospesupbackend.model.entities.Product;
import com.generation.progettospesupbackend.model.entities.Supermarket;
import com.generation.progettospesupbackend.model.entities.User;
import com.generation.progettospesupbackend.model.repositories.ProductRepository;
import com.generation.progettospesupbackend.model.repositories.SupermarketRepository;
import com.generation.progettospesupbackend.model.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService
{
    @Autowired
    private UserRepository repo;
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private ProductRepository prodRepo;
    @Autowired
    private SupermarketRepository supRepo;

    public String register(RegisterDto registerDto)
    {
        if(!registerDto.getPassword().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"))
            throw new InvalidCredentials("Password not valid");

        User user = new User();
        String hash = encoder.encode(registerDto.getPassword());
        user.setPassword(hash);
        user.setEmail(registerDto.getEmail());
        user.setUsername(registerDto.getUsername());


        System.out.println(hash);
        //genero un token in automatico
        user.setToken(UUID.randomUUID().toString());

        repo.save(user);

        return user.getToken();
    }

    public String login(LoginDto dto)
    {
        Optional<User> op = repo.findByEmail(dto.getEmail());
        if(op.isEmpty())
            throw new InvalidCredentials("Invalid email");

        if(!encoder.matches(dto.getPassword(), op.get().getPassword()))
            throw new InvalidCredentials("Invalid password");

        return op.get().getToken();
    }

    public User findUserByToken(String token)
    {
        Optional<User> op = repo.findByToken(token);

        if(op.isEmpty())
            throw new InvalidCredentials("Invalid token");

        return op.get();
    }

    public UserDto readUserDto(String token)
    {
        User u = findUserByToken(token);
        UserDto dto = convertToUserDto(u);
        return dto;
    }

    public UserDto convertToUserDto(User u) {
        UserDto dto = new UserDto();
        dto.setEmail(u.getEmail());
        dto.setUsername(u.getUsername());
        return dto;
    }

    public void changePassword(String token, RegisterDto dto) {
        User u = findUserByToken(token);

        if(!dto.getPassword().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"))
            throw new InvalidCredentials("Password not valid");
        String hash = encoder.encode(dto.getPassword());
        u.setPassword(hash);
        repo.save(u);
    }

    public void changeUsername(String token, RegisterDto dto) {
        User u = findUserByToken(token);
        u.setUsername(dto.getUsername());
        repo.save(u);

    }

    public void deleteUser (String token) {
        User u = findUserByToken(token);
        repo.delete(u);
    }

    public void addFavouriteProduct (String token, UUID productId, UUID supermarketId) {
        User u = findUserByToken(token);
        Optional<Product> op = prodRepo.findById(productId);
        Optional<Supermarket> sop = supRepo.findById(supermarketId);
        if(op.isEmpty() || sop.isEmpty())
            throw new RuntimeException();

        Supermarket s = sop.get();
        Product p = op.get();
        u.getFavouriteProducts().put(p,s);
        repo.save(u);
    }

    public List<ProductDto> getFavouriteProducts (String token) {
        User u = findUserByToken(token);
//        List<ProductDto> favList = u.getFavouriteProducts().keySet().stream().map(p -> convertProductToDto(p)).toList();
        List<ProductDto> favList = new ArrayList<>();
        for(Product p : u.getFavouriteProducts().keySet())
        {
            favList.add(convertProductToDto(p, u.getFavouriteProducts().get(p)));
        }
        return favList;
    }

    private ProductDto convertProductToDto (Product p, Supermarket s) {
        ProductDto dto = new ProductDto();
        dto.setPriceTrendId(p.getActivePrice(s).getId());
        dto.setPrice(p.getActivePrice(s).getPrice());
        dto.setOriginalPrice(p.getActivePrice(s).getOriginalPrice());
        dto.setPricePerType(p.getActivePrice(s).getPricePerType());
        dto.setStartDate(p.getActivePrice(s).getStartDate());
        dto.setEndDate(p.getActivePrice(s).getEndDate());
        dto.setActive(p.getActivePrice(s).isActive());
        dto.setProductId(p.getId());
        dto.setProductName(p.getName());
        dto.setCategory(p.getCategory().toString());
        dto.setDescription(p.getDescription());
        dto.setImgUrl(p.getImgUrl());
        dto.setSupermarketId(s.getId());
        dto.setSupermarketName(s.getName());

        return dto;
    }

}
