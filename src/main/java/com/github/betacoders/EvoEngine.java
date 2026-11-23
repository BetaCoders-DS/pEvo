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
 * As janelas compartilham este controlador e apenas exibem um mundo cada.
 */
public final class EvoEngine {
  private final EvolutionConfig config;
  private final EvolutionExperiment experiment;

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

  /** Chamado uma vez por quadro, somente pela janela principal. */
  public synchronized void step() {
    experiment.step();
  }

  public synchronized World worldAt(int index) {
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

  public synchronized int generation() {
    return experiment.generation();
  }

  public synchronized boolean generationFinished() {
    return experiment.generationFinished();
  }

  public synchronized SimulationResult[] previousRanking() {
    return experiment.previousRanking();
  }

  public synchronized long seed() {
    return experiment.seed();
  }

  public synchronized long scenarioSeed() {
    return experiment.scenarioSeed();
  }

  public synchronized void restart() {
    experiment.restart();
  }

  public synchronized void restart(long seed) {
    experiment.restart(seed);
  }
}
