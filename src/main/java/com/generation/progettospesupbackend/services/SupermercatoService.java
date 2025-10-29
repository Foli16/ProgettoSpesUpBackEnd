package com.generation.progettospesupbackend.services;

import com.generation.progettospesupbackend.model.dtos.SupermercatoDto;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class SupermercatoService {

    public List<SupermercatoDto> cercaSupermercatiVicini(String indirizzo) {
        double[] coord = geocode(indirizzo);
        return querySupermercati(coord[0], coord[1]);
    }

    private double[] geocode(String indirizzo) {
        try {
            String url = "https://nominatim.openstreetmap.org/search?q=" +
                    URLEncoder.encode(indirizzo, StandardCharsets.UTF_8) +
                    "&format=json&limit=1";
            JSONArray arr = new JSONArray(new String(new URL(url).openStream().readAllBytes()));
            if (arr.length() > 0) {
                JSONObject obj = arr.getJSONObject(0);
                return new double[]{obj.getDouble("lat"), obj.getDouble("lon")};
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new double[]{0, 0};
    }

    private List<SupermercatoDto> querySupermercati(double lat, double lon) {
        List<SupermercatoDto> list = new ArrayList<>();
        String query = String.format("""
            [out:json];
            node["shop"="supermarket"](around:3000,%f,%f);
            out;
        """, lat, lon);

        try {
            HttpURLConnection conn = (HttpURLConnection) new URL("https://overpass-api.de/api/interpreter").openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            try (OutputStream os = conn.getOutputStream()) {
                os.write(("data=" + URLEncoder.encode(query, StandardCharsets.UTF_8)).getBytes());
            }

            String json = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            JSONArray elements = new JSONObject(json).getJSONArray("elements");

            for (int i = 0; i < elements.length(); i++) {
                JSONObject el = elements.getJSONObject(i);
                JSONObject tags = el.optJSONObject("tags");
                String name = tags != null ? tags.optString("name", "Supermercato") : "Supermercato";
                list.add(new SupermercatoDto(name, el.getDouble("lat"), el.getDouble("lon")));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }
}
