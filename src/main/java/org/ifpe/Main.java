package org.ifpe;

import org.ifpe.benchmark.BenchmarkRunner;
import org.ifpe.client.PokeApiClient;

public class Main {
    public static void main(String[] args) throws Exception {
        PokeApiClient client = new PokeApiClient();
        BenchmarkRunner runner = new BenchmarkRunner(client);
        runner.run();
    }
}