package org.ifpe.benchmark;

import org.ifpe.client.PokeApiClient;
import org.ifpe.services.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class BenchmarkRunner {

    private static final int[] QUANTITIES = {100, 500, 1000};
    private static final int[] THREAD_COUNTS = {2, 4, 8};
    private static final int RUNS = 10;
    private static final int DELAY_BETWEEN_RUNS_MS = 3000;
    private static final Path BASE_OUTPUT = Path.of("output");

    private final PokeApiClient client;

    public BenchmarkRunner(PokeApiClient client) {
        this.client = client;
    }

    public void run() throws IOException, InterruptedException {
        Map<String, Double> results = new LinkedHashMap<>();

        for (int quantity : QUANTITIES) {
            System.out.println("\n========================================");
            System.out.println("Quantidade: " + quantity + " pokémons");
            System.out.println("========================================");

            String seqKey = buildKey("Sequential", quantity, 1);
            double seqAvg = runBenchmark(
                    new SequentialService(client, BASE_OUTPUT.resolve("sequential")),
                    quantity,
                    seqKey
            );
            results.put(seqKey, seqAvg);

            for (int threads : THREAD_COUNTS) {

                String esKey = buildKey("ExecutorService", quantity, threads);
                double esAvg = runBenchmark(
                        new ParallelExecutorService(client, BASE_OUTPUT.resolve("executor_" + threads), threads),
                        quantity,
                        esKey
                );
                results.put(esKey, esAvg);

                String cfKey = buildKey("CompletableFuture", quantity, threads);
                double cfAvg = runBenchmark(
                        new ParallelCompletableFutureService(client, BASE_OUTPUT.resolve("completable_" + threads), threads),
                        quantity,
                        cfKey
                );
                results.put(cfKey, cfAvg);

                String rtKey = buildKey("RawThread", quantity, threads);
                double rtAvg = runBenchmark(
                        new ParallelThreadService(client, BASE_OUTPUT.resolve("rawthread_" + threads), threads),
                        quantity,
                        rtKey
                );
                results.put(rtKey, rtAvg);
            }
        }

        printSummary(results);
    }

    private double runBenchmark(DownloadService service, int quantity, String label) throws InterruptedException, IOException {
        System.out.println("\n→ " + label);

        System.out.println("   [WARM-UP] Rodando sem medir...");
        clearOutputDirectory(service.getOutputDirectory());
        service.download(quantity);

        Thread.sleep(DELAY_BETWEEN_RUNS_MS);

        long total = 0;

        for (int run = 1; run <= RUNS; run++) {
            try {
                clearOutputDirectory(service.getOutputDirectory());
            } catch (IOException e) {
                System.err.println("Erro ao limpar diretório: " + e.getMessage());
            }

            long elapsed = service.download(quantity);
            total += elapsed;
            System.out.printf("   Run %02d: %dms%n", run, elapsed);

            if (run < RUNS) {
                System.out.println("   Aguardando " + DELAY_BETWEEN_RUNS_MS / 1000 + "s...");
                Thread.sleep(DELAY_BETWEEN_RUNS_MS);
            }
        }

        double average = (double) total / RUNS;
        System.out.printf("   Média: %.2fms%n", average);
        return average;
    }

    private void clearOutputDirectory(Path directory) throws IOException {
        if (!directory.toFile().exists()) return;

        Files.walk(directory)
                .filter(Files::isRegularFile)
                .forEach(file -> {
                    try {
                        Files.delete(file);
                    } catch (IOException e) {
                        System.err.println("Erro ao deletar: " + file);
                    }
                });
    }

    private String buildKey(String approach, int quantity, int threads) {
        if (threads == 1) {
            return String.format("%-20s | quantity=%4d | threads=--", approach, quantity);
        }
        return String.format("%-20s | quantity=%4d | threads=%d", approach, quantity, threads);
    }

    private void printSummary(Map<String, Double> results) {
        System.out.println("\n╔══════════════════════════════════════════════════════════╗");
        System.out.println("║                    RESUMO FINAL                         ║");
        System.out.println("╠══════════════════════════════════════════════════════════╣");
        System.out.printf("║ %-44s │ %10s ║%n", "Abordagem", "Média (ms)");
        System.out.println("╠══════════════════════════════════════════════════════════╣");

        results.forEach((key, avg) ->
                System.out.printf("║ %-44s │ %10.2f ║%n", key, avg)
        );

        System.out.println("╚══════════════════════════════════════════════════════════╝");
    }
}