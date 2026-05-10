package org.ifpe.services;

import org.ifpe.client.PokeApiClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SequentialService implements DownloadService {

    private final PokeApiClient client;
    private final Path outputDirectory;

    public SequentialService(PokeApiClient client, Path outputDirectory) throws IOException {
        this.client = client;
        this.outputDirectory = outputDirectory;
        Files.createDirectories(this.outputDirectory);
    }

    @Override
    public long download(int quantity) {
        long start = System.currentTimeMillis();

        for (int id = 1; id <= quantity; id++) {
            try {
                String spriteUrl = client.fetchSpriteUrl(id);
                byte[] imageBytes = client.downloadImage(spriteUrl);

                Path destination = outputDirectory.resolve("pokemon_" + id + ".png");
                Files.write(destination, imageBytes);

                System.out.println("[SEQ] Pokémon " + id + " salvo.");
            } catch (Exception e) {
                System.err.println("[SEQ] Falha no pokémon " + id + ": " + e.getMessage());
            }
        }

        long elapsed = System.currentTimeMillis() - start;
        System.out.println("[SEQ] Concluído em " + elapsed + "ms");
        return elapsed;
    }
}
