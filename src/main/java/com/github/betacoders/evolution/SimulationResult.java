package com.github.betacoders.evolution;

public record SimulationResult(
    int generation,
    int individualId,
    int wavesCompleted,
    int enemiesKilled,
    long survivalTicks,
    double initialEdgeLength) {

  public SimulationResult {
    if (generation < 0 || individualId < 0) {
      throw new IllegalArgumentException(
          "Generation and individual ID must not be negative.");
    }

    if (wavesCompleted < 0 || enemiesKilled < 0) {
      throw new IllegalArgumentException(
          "Counters must not be negative.");
    }

    if (!Double.isFinite(survivalTicks)
        || survivalTicks < 0.0) {
      throw new IllegalArgumentException(
          "Survival time must be finite and non-negative.");
    }

    if (!Double.isFinite(initialEdgeLength)
        || initialEdgeLength < 0.0) {
      throw new IllegalArgumentException(
          "Edge length must be finite and non-negative.");
    }
  }

  public double score() {
    double killBonus = 600.0 * (enemiesKilled / (enemiesKilled + 10.0));

    double survivalBonus = 250.0 * (survivalTicks / (survivalTicks + 60.0));

    double compactnessBonus = 150.0 / (1.0 + initialEdgeLength / 1000.0);

    return 1000.0 * wavesCompleted
        + killBonus
        + survivalBonus
        + compactnessBonus;
  }
}
