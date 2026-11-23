package com.github.betacoders.evolution;

public record EvolutionConfig(
    int populationSize,
    double width,
    double height,
    int initialMinCells,
    int initialMaxCells,
    int growthInterval,
    int minGrowth,
    int maxGrowth,
    int absoluteMaxCells) {
  public EvolutionConfig {
    BaseGenome.validateDimensions(width, height);

    if (populationSize < 1
        || initialMinCells < 1
        || initialMaxCells < initialMinCells
        || absoluteMaxCells < initialMaxCells
        || growthInterval < 1
        || minGrowth < 0
        || maxGrowth < minGrowth) {
      throw new IllegalArgumentException(
          "Invalid evolution configuration.");
    }
  }

  public static EvolutionConfig standard() {
    return new EvolutionConfig(
        2, 1000, 700, 5, 12, 5, 0, 3, 30);
  }

  public int minCells(int generation) {
    return limit(generation, initialMinCells, minGrowth);
  }

  public int maxCells(int generation) {
    return limit(generation, initialMaxCells, maxGrowth);
  }

  private int limit(int generation, int initial, int growth) {
    if (generation < 1) {
      throw new IllegalArgumentException(
          "Generation must be positive.");
    }

    long increases = (generation - 1L) / growthInterval;

    return (int) Math.min(
        absoluteMaxCells,
        initial + increases * growth);
  }
}
