package com.generation.progettospesupbackend.security;

import com.generation.progettospesupbackend.model.entities.User;
import com.generation.progettospesupbackend.services.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class UserFilter extends OncePerRequestFilter
{
    @Autowired
    private UserService serv;

    @Override
    protected void doFilterInternal
            (HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException
    {
        Cookie[] cookies = request.getCookies();
        String token =extractToken(cookies);//token come cookie o null

        if(token != null)//se ho token
        {

            User u = serv.findUserByToken(token);
            //creo un utente utilizzabile da spring security
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(u, null, u.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);//lo mettiamo in memoria di Spring Security
        }
        filterChain.doFilter(request, response);//aggiungiamo questa automazione alla lista di automazioni eseguite quando arriva
        //una request
    }

    private String extractToken(Cookie[] cookies)
    {
        if(cookies == null)
            return null;
        for(Cookie cookie : cookies)
            if(cookie.getName().equals("token"))
                return cookie.getValue();

        return null;
    }
}
