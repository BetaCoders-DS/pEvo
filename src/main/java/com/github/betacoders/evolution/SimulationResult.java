package com.github.betacoders.evolution;

public record SimulationResult(
    int generation,
    int individualId,
    int wavesCompleted,
    int enemiesKilled,
    long survivalTicks,
    double initialEdgeLength) {

  private static final double WAVE_WEIGHT = 10000.0;
  private static final double EDGE_WEIGHT = 800.0;
  private static final double EDGE_REF = 1000.0;
  private static final double SURVIVAL_WEIGHT = 60.0;
  private static final double TICKS_PER_SECOND = 30.0;
  private static final double KILL_WEIGHT = 20.0;

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
    double edgeTerm = EDGE_WEIGHT * EDGE_REF / (initialEdgeLength + EDGE_REF);
    double survivalTerm = SURVIVAL_WEIGHT * Math.log1p(survivalTicks / TICKS_PER_SECOND);
    double killTerm = KILL_WEIGHT * Math.log1p(enemiesKilled);

    return WAVE_WEIGHT * wavesCompleted
        + edgeTerm
        + survivalTerm
        + killTerm;
  }
}
