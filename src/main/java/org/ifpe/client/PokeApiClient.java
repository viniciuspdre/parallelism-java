package org.ifpe.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class PokeApiClient {

    private static final String BASE_URL = "https://pokeapi.co/api/v2/pokemon/";
    private static final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient httpClient;

    public PokeApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public String fetchSpriteUrl(int pokemonId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + pokemonId))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 200) {
            throw new RuntimeException("Erro na API para ID " + pokemonId + ": HTTP " + response.statusCode());
        }

        JsonNode root = mapper.readTree(response.body());
        JsonNode spriteUrl = root.path("sprites").path("front_default");

        if (spriteUrl.isMissingNode() || spriteUrl.isNull()) {
            throw new RuntimeException("Sprite não encontrado para ID " + pokemonId);
        }

        return spriteUrl.asText();
    }

    public byte[] downloadImage(String imageUrl) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(imageUrl))
                .GET()
                .build();

        HttpResponse<byte[]> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofByteArray()
        );

        if (response.statusCode() != 200) {
            throw new RuntimeException("Erro ao baixar imagem: HTTP " + response.statusCode());
        }

        return response.body();
    }

}
