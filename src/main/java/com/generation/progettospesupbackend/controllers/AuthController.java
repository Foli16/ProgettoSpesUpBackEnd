package com.generation.progettospesupbackend.controllers;

import com.generation.progettospesupbackend.model.dtos.LoginDto;
import com.generation.progettospesupbackend.model.dtos.RegisterDto;
import com.generation.progettospesupbackend.model.dtos.UserDto;
import com.generation.progettospesupbackend.model.entities.User;
import com.generation.progettospesupbackend.services.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/auth")
public class AuthController
{
    @Autowired
    private UserService userService;

    @PostMapping("register")
    public void register(@RequestBody RegisterDto dto, HttpServletResponse response)
    {
        String tokenUtente = userService.register(dto);

        Cookie cookie = new Cookie("token", tokenUtente);
        cookie.setMaxAge(3600);
        cookie.setPath("/api");
        response.addCookie(cookie);
    }

    @PostMapping("login")
    public void login(@RequestBody LoginDto dto, HttpServletResponse response)
    {
        String tokenUtente = userService.login(dto);

        Cookie cookie = new Cookie("token", tokenUtente);
        cookie.setMaxAge(3600);
        cookie.setPath("/api");
        response.addCookie(cookie);
    }


    @GetMapping("/userinformation")
    //si può prendere utente da SecurityContextHolder così
    public UserDto getUserInfo(@AuthenticationPrincipal User user)
    {
        return userService.convertToUserDto(user);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String gestisciTutto(Exception e)
    {
        return "Operazione fallita, ulteriori dettagli "+e.getMessage();
    }
}
