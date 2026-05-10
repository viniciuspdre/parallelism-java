package org.ifpe.services;

import org.ifpe.client.PokeApiClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ParallelThreadService implements DownloadService {

    private final PokeApiClient client;
    private final Path outputDirectory;
    private final int threadQuantity;

    public ParallelThreadService(
            PokeApiClient client,
            Path outputDirectory,
            int threadQuantity
    ) throws IOException {
        this.client = client;
        this.outputDirectory = outputDirectory;
        this.threadQuantity = threadQuantity;
        Files.createDirectories(outputDirectory);
    }

    @Override
    public long download(int quantity) {
        long start = System.currentTimeMillis();

        int batchSize = (int) Math.ceil((double) quantity / threadQuantity);

        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < threadQuantity; i++) {
            int startId = i * batchSize + 1;
            int endId = Math.min(startId + batchSize - 1, quantity);

            if (startId > quantity) break;

            Thread thread = new Thread(() -> {
                for (int id = startId; id <= endId; id++) {
                    try {
                        String spriteUrl = client.fetchSpriteUrl(id);
                        byte[] imageBytes = client.downloadImage(spriteUrl);
                        Path destination = outputDirectory.resolve("pokemon_" + id + ".png");
                        Files.write(destination, imageBytes);
                        System.out.println("[PARALLEL - RAW] "  + Thread.currentThread().getName() + " - Pokémon "+ id + " salvo. Thread: ");
                    } catch (Exception e) {
                        System.err.println("[PARALLEL - RAW] Falha no pokémon " + id + ": " + e.getMessage());
                    }
                }
            });

            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        long elapsed = System.currentTimeMillis() - start;
        System.out.println("[RAW] Concluído em " + elapsed + "ms");
        return elapsed;
    }
}
