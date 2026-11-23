package com.github.betacoders.evolution;

public record SimulationResult(
    int generation,
    int individualId,
    int wavesCompleted,
    int enemiesKilled,
    long survivalTicks,
    double initialEdgeLength,
    int initialCellCount
) {
    public SimulationResult {
        if (generation < 0
                || individualId < 0
                || wavesCompleted < 0
                || enemiesKilled < 0
                || survivalTicks < 0
                || initialCellCount < 1
                || !Double.isFinite(initialEdgeLength)
                || initialEdgeLength < 0) {
            throw new IllegalArgumentException(
                "Invalid simulation result."
            );
        }
    }

    public double score() {
        return 10000.0 * wavesCompleted
            + 800.0 / (1.0 + initialEdgeLength / 1000.0)
            + 60.0 * Math.log1p(survivalTicks / 30.0)
            + 20.0 * Math.log1p(enemiesKilled)
            + 200.0 / (1.0 + initialCellCount / 10.0);
    }
}