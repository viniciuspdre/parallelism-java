package org.ifpe.services;

import org.ifpe.client.PokeApiClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class ParallelCompletableFutureService implements DownloadService {

    private final PokeApiClient client;
    private final Path outputDirectory;
    private final int threadQuantity;

    public ParallelCompletableFutureService(
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
        ExecutorService executorService = Executors.newFixedThreadPool(threadQuantity);
        long start = System.currentTimeMillis();

        List<CompletableFuture<Void>> futures = IntStream.rangeClosed(1, quantity)
                .mapToObj(pokemonId ->
                        CompletableFuture.supplyAsync(() -> {
                            try {
                                return client.fetchSpriteUrl(pokemonId);
                            } catch (Exception e) {
                                throw new RuntimeException("Erro ao buscar sprite " + pokemonId, e);
                            }
                        }, executorService)
                    .thenApply(spriteUrl -> {
                        try {
                            return client.downloadImage(spriteUrl);
                        } catch (Exception e) {
                            throw new RuntimeException("Erro ao baixar imagem " + pokemonId, e);
                        }
                    })
                    .thenAccept(imageBytes -> {
                        try {
                            Path destination = outputDirectory.resolve("pokemon_" + pokemonId + ".png");
                            Files.write(destination, imageBytes);
                            System.out.println("[PARALLEL - CF] " + Thread.currentThread().getName() + " - Pokémon " + pokemonId + " salvo.");
                        } catch (IOException e) {
                            throw new RuntimeException("Erro ao salvar " + pokemonId, e);
                        }
                    })
                    .exceptionally(e -> {
                        System.err.println("[PARALLEL - CF] Falha no pokémon " + pokemonId + ": " + e.getMessage());
                        return null;
                    })
                ).toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        executorService.shutdown();
        try {
            boolean finished = executorService.awaitTermination(30, TimeUnit.SECONDS);
            if (!finished) {
                System.err.println("[PARALLEL - CF] Timeout!");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (!executorService.isTerminated()) {
                executorService.shutdownNow();
            }
        }

        long elapsed = System.currentTimeMillis() - start;
        System.out.println("[CF] Concluído em " + elapsed + "ms");
        return elapsed;
    }

    @Override
    public Path getOutputDirectory() {
        return outputDirectory;
    }
}
