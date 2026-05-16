package org.ifpe;

import org.ifpe.benchmark.BenchmarkRunner;
import org.ifpe.client.PokeApiClient;
import org.ifpe.services.ParallelCompletableFutureService;
import org.ifpe.services.ParallelExecutorService;
import org.ifpe.services.ParallelThreadService;
import org.ifpe.services.SequentialService;

import java.io.IOException;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws Exception {
        PokeApiClient client = new PokeApiClient();
        BenchmarkRunner runner = new BenchmarkRunner(client);
        runner.run();
    }
}