package com.generation.progettospesupbackend.services;

import com.generation.progettospesupbackend.exceptions.InvalidCredentials;
import com.generation.progettospesupbackend.model.dtos.LoginDto;
import com.generation.progettospesupbackend.model.dtos.ProductDto;
import com.generation.progettospesupbackend.model.dtos.RegisterDto;
import com.generation.progettospesupbackend.model.dtos.UserDto;
import com.generation.progettospesupbackend.model.entities.Product;
import com.generation.progettospesupbackend.model.entities.User;
import com.generation.progettospesupbackend.model.repositories.ProductRepository;
import com.generation.progettospesupbackend.model.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService
{
    @Autowired
    private UserRepository repo;
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private ProductRepository prodRepo;

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

    public void addFavouriteProduct (String token, UUID id) {
        User u = findUserByToken(token);
        Optional<Product> op = prodRepo.findById(id);
        if(op.isEmpty())
            throw new RuntimeException();

        Product p = op.get();
        u.getFavouriteProducts().add(p);
        repo.save(u);
    }

    public List<ProductDto> getFavouriteProducts (String token) {
        User u = findUserByToken(token);
        List<ProductDto> favList = u.getFavouriteProducts().stream().map(p -> convertProductToDto(p)).toList();
        return favList;
    }

    private ProductDto convertProductToDto (Product p) {
        ProductDto dto = new ProductDto();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setDescription(p.getDescription());
        dto.setCategory(p.getCategory().toString());
        dto.setImgUrl(p.getImgUrl());
        dto.setPriceTrends(p.getPriceTrends());

        return dto;
    }

}
