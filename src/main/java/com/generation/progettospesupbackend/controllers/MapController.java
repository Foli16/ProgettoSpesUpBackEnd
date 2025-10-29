package com.generation.progettospesupbackend.controllers;

import com.generation.progettospesupbackend.services.GoogleMapsService;
import com.google.maps.model.PlacesSearchResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MapController {

    private final GoogleMapsService googleMapsService;

    public MapController(GoogleMapsService googleMapsService) {
        this.googleMapsService = googleMapsService;
    }

    @GetMapping("/api/supermarkets")
    public List<PlacesSearchResult> getSupermarkets(@RequestParam String address) throws Exception {
        return googleMapsService.findSupermarkets(address);
    }
}