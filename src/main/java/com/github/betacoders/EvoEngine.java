package com.github.betacoders;

import java.util.Objects;

import com.github.betacoders.evolution.EvolutionConfig;
import com.github.betacoders.evolution.EvolutionExperiment;
import com.github.betacoders.evolution.SimulationResult;
import com.github.betacoders.world.WaveSim;
import com.github.betacoders.world.WaveSim.WaveConfig;
import com.github.betacoders.world.enemies.EnemyWaveFact;
import com.github.betacoders.world.World;

/**
 * Une a evolução da população aos mundos e às ondas de inimigos.
 * A interface exibe os mundos sem controlar suas regras de evolução.
 */
public final class EvoEngine {
  private static final int DEFAULT_TPS = 60;
  private static final int MIN_TPS = 10;
  private static final int MAX_TPS = 1000;
  private static final int TPS_STEP = 10;
  private static final int MAX_STEPS_PER_FRAME = 16;

  private final EvolutionConfig config;
  private final EvolutionExperiment experiment;

  private int ticksPerSecond = DEFAULT_TPS;
  private double extraBudget;

  public EvoEngine(EvolutionConfig config) {
    this(config, WaveConfig.DEFAULT);
  }

  public EvoEngine(EvolutionConfig config, WaveConfig waveConfig) {
    this.config = Objects.requireNonNull(config);
    Objects.requireNonNull(waveConfig);

    // Mundos independentes recebem a mesma seed de cenário para
    // comparar os genomas sob as mesmas ondas em todas as gerações.
    experiment = new EvolutionExperiment(
        config,
        (world, scenarioSeed) -> new WaveSim(world, scenarioSeed, new EnemyWaveFact(), waveConfig));
  }

  public void step() {
    experiment.step();
  }

  public void advance(double millis) {
    if (ticksPerSecond <= DEFAULT_TPS) {
      extraBudget = 0.;
      return;
    }

    double target = 1000. / ticksPerSecond;
    double elapsed = Math.max(0., Math.min(millis, 250.));

    extraBudget += elapsed / target - 1.;

    int steps = 0;
    while (extraBudget >= 1. && steps < MAX_STEPS_PER_FRAME) {
      experiment.step();
      extraBudget -= 1.;
      ++steps;
    }

    if (extraBudget > MAX_STEPS_PER_FRAME)
      extraBudget = MAX_STEPS_PER_FRAME;
  }

  public int ticksPerSecond() {
    return ticksPerSecond;
  }

  public void increaseSpeed() {
    ticksPerSecond = Math.min(MAX_TPS, ticksPerSecond + TPS_STEP);
    extraBudget = 0.;
  }

  public void decreaseSpeed() {
    ticksPerSecond = Math.max(MIN_TPS, ticksPerSecond - TPS_STEP);
    extraBudget = 0.;
  }

  public World worldAt(int index) {
    if (index < 0 || index >= populationSize()) {
      throw new IndexOutOfBoundsException(
          "Índice de indivíduo inválido: " + index);
    }

    return experiment.simulations()[index].world();
  }

  public EvolutionConfig config() {
    return config;
  }

  public int populationSize() {
    return config.populationSize();
  }

  public int generation() {
    return experiment.generation();
  }

  public boolean generationFinished() {
    return experiment.generationFinished();
  }

  public SimulationResult[] previousRanking() {
    return experiment.previousRanking();
  }

  public long seed() {
    return experiment.seed();
  }

  public long scenarioSeed() {
    return experiment.scenarioSeed();
  }

  public void restart() {
    experiment.restart();
  }

  public void restart(long seed) {
    experiment.restart(seed);
  }
}
