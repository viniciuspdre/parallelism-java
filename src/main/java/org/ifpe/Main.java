package org.ifpe;

import org.ifpe.client.PokeApiClient;
import org.ifpe.services.ParallelCompletableFutureService;
import org.ifpe.services.ParallelExecutorService;
import org.ifpe.services.SequentialService;

import java.io.IOException;
import java.nio.file.Path;

public class Main {
    static void main() throws IOException {
        PokeApiClient client = new PokeApiClient();
        SequentialService sequentialService = new SequentialService(client, Path.of("output/sequential"));

        long elapsedTime = sequentialService.download(100);
        System.out.println("Tempo total sequencial: " + elapsedTime + "ms");

        System.out.println("\n==============================================================\n");

        ParallelExecutorService parallelExecutorService = new ParallelExecutorService(client, Path.of("output/executor"), 4);
        elapsedTime = parallelExecutorService.download(100);
        System.out.println("Tempo total paralelo com ExecutorService: " + elapsedTime + "ms");

        System.out.println("\n==============================================================\n");

        ParallelCompletableFutureService parallelCompletableFutureService = new ParallelCompletableFutureService(client, Path.of("output/cf"), 4);
        elapsedTime = parallelCompletableFutureService.download(100);
        System.out.println("Tempo total paralelo com CompletableFutureService: " + elapsedTime + "ms");
    }
}
