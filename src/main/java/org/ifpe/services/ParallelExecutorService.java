package org.ifpe.services;

import org.ifpe.client.PokeApiClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ParallelExecutorService implements DownloadService{

    private final PokeApiClient client;
    private final Path outputDirectory;
    private final int threadQuantity;

    public ParallelExecutorService(
            PokeApiClient client,
            Path outputDirectory,
            int threadQuantity
    ) throws IOException {
        this.client = client;
        this.outputDirectory = outputDirectory;
        this.threadQuantity = threadQuantity;
        Files.createDirectories(this.outputDirectory);
    }

    @Override
    public long download(int quantity) {
        ExecutorService executorService = Executors.newFixedThreadPool(threadQuantity);
        long start = System.currentTimeMillis();

        for  (int id = 1; id < quantity; id++) {
            final int pokemonId = id;
            executorService.execute(() -> {
                try {
                    String spriteUrl = client.fetchSpriteUrl(pokemonId);
                    byte[] spriteBytes = client.downloadImage(spriteUrl);

                    Path destination = outputDirectory.resolve("pokemon_" + pokemonId + ".png");
                    Files.write(destination, spriteBytes);

                    System.out.println("[PARALLEL - EXECUTOR SERVICE] " +
                            Thread.currentThread().getName() +
                            " - Pokémon " + pokemonId + " salvo.");
                } catch (Exception e) {
                    System.err.println("Erro no Download do " + pokemonId);
                }
            });
        }

        executorService.shutdown();

        try {
            boolean finished = executorService.awaitTermination(30, TimeUnit.SECONDS);
            if (!finished) {
                System.err.println("[POOL] Timeout! Nem todos os downloads concluíram.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (!executorService.isTerminated()) {
                executorService.shutdownNow();
            }
        }

        long elapsed = System.currentTimeMillis() - start;
        System.out.println("Concluído em " + elapsed + "ms");
        return elapsed;
    }

    @Override
    public Path getOutputDirectory() {
        return outputDirectory;
    }
}
