package com.github.betacoders.evolution;

import java.util.Arrays;
import java.util.Objects;

import com.github.betacoders.world.Simulation;
import com.github.betacoders.world.World;

public final class GenerationEvaluation {
  private final int generation;
  private final Simulation[] simulations;
  private final World[] worlds;
  private final double[] initialEdgeLengths;
  private final SimulationResult[] results;

  private int defeatsRegistered;

  public GenerationEvaluation(
      int generation,
      Simulation[] simulations) {

    if (generation < 0) {
      throw new IllegalArgumentException(
          "Generation must not be negative.");
    }

    Objects.requireNonNull(
        simulations, "Simulations are required.");

    if (simulations.length == 0) {
      throw new IllegalArgumentException(
          "Population must not be empty.");
    }

    this.generation = generation;
    this.simulations = simulations.clone();
    this.worlds = new World[simulations.length];
    this.initialEdgeLengths = new double[simulations.length];
    this.results = new SimulationResult[simulations.length];

    for (int i = 0; i < simulations.length; i++) {
      Simulation simulation = Objects.requireNonNull(
          this.simulations[i], "Simulation must not be null.");

      World world = Objects.requireNonNull(
          simulation.world(), "Simulation must provide a world.");

      if (world.over()
          || world.ticks() != 0
          || world.enemiesKilled() != 0
          || world.wavesCompleted() != 0) {
        throw new IllegalArgumentException(
            "Generation requires fresh simulations.");
      }

      if (world.base() == null) {
        throw new IllegalArgumentException(
            "World must provide a base.");
      }

      for (int j = 0; j < i; j++) {
        if (worlds[j] == world
            || worlds[j].base() == world.base()) {
          throw new IllegalArgumentException(
              "Individuals must use separate bases and graphs.");
        }
      }

      double length = world.base().totalEdgeLength();

      if (!Double.isFinite(length) || length < 0.0) {
        throw new IllegalArgumentException(
            "Initial edge length is invalid.");
      }

      worlds[i] = world;
      initialEdgeLengths[i] = length;
    }
  }

  public void step() {
    if (allDead()) {
      return;
    }

    for (int i = 0; i < simulations.length; i++) {
      if (results[i] != null) {
        continue;
      }

      ensureSameWorld(i);

      World world = worlds[i];

      if (!world.over()) {
        int previousTicks = world.ticks();

        simulations[i].tick();

        ensureSameWorld(i);

        if (world.ticks() != previousTicks + 1) {
          throw new IllegalStateException(
              "Simulation must advance exactly one world tick.");
        }
      }

      if (world.over()) {
        results[i] = new SimulationResult(
            generation,
            i,
            world.wavesCompleted(),
            world.enemiesKilled(),
            world.ticks(),
            initialEdgeLengths[i]);

        defeatsRegistered++;
      }
    }
  }

  private void ensureSameWorld(int index) {
    World current = simulations[index].world();
    if (current != worlds[index]
        || current.base() != worlds[index].base()) {
      throw new IllegalStateException(
          "Simulation replaced its world during evaluation.");
    }
  }

  public boolean allDead() {
    return defeatsRegistered == simulations.length;
  }

  public int defeatsRegistered() {
    return defeatsRegistered;
  }

  public int populationSize() {
    return simulations.length;
  }

  public SimulationResult[] ranking() {
    if (!allDead()) {
      throw new IllegalStateException(
          "All bases must be defeated before ranking.");
    }

    SimulationResult[] ordered = Arrays.copyOf(results, results.length);

    Arrays.sort(ordered, GenerationEvaluation::compareResults);

    return ordered;
  }

  public SimulationResult[] selectParents(int count) {
    if (count <= 0 || count > simulations.length) {
      throw new IllegalArgumentException(
          "Parent count must be within population bounds.");
    }

    return Arrays.copyOf(ranking(), count);
  }

  private static int compareResults(
      SimulationResult a,
      SimulationResult b) {

    int comparison = Integer.compare(
        b.wavesCompleted(), a.wavesCompleted());

    if (comparison != 0) {
      return comparison;
    }

    comparison = Double.compare(b.score(), a.score());

    if (comparison != 0) {
      return comparison;
    }

    return Integer.compare(a.individualId(), b.individualId());
  }
}
