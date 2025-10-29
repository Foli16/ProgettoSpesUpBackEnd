package com.generation.progettospesupbackend.services;

import com.google.maps.GeoApiContext;
import com.google.maps.PlacesApi;
import com.google.maps.GeocodingApi;
import com.google.maps.model.LatLng;
import com.google.maps.model.PlaceType;
import com.google.maps.model.PlacesSearchResponse;
import com.google.maps.model.PlacesSearchResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class GoogleMapsService {

    @Value("${google.api.key}")
    private String apiKey;

    public List<PlacesSearchResult> findSupermarkets(String address) throws Exception {
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey(apiKey)
                .build();

        // 1️⃣ Geocoding: otteniamo lat/lng dall'indirizzo
        LatLng location = GeocodingApi.geocode(context, address).await()[0].geometry.location;

        // 2️⃣ Ricerca supermercati nei dintorni (radius in metri)
        PlacesSearchResponse response = PlacesApi.nearbySearchQuery(context, location)
                .radius(2000)
                .type(PlaceType.SUPERMARKET)
                .await();

        return Arrays.asList(response.results);
    }
}