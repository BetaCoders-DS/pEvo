package com.github.betacoders.evolution;

import java.util.Arrays;
import java.util.Objects;

import com.github.betacoders.world.Base;
import com.github.betacoders.world.Simulation;

public final class GenerationEvaluation {
    private final int generation;
    private final Simulation[] simulations;
    private final Base[] bases;
    private final double[] initialEdgeLengths;
    private final SimulationResult[] results;

    private int defeatsRegistered;

    public GenerationEvaluation(
            int generation,
            Simulation[] simulations) {

        if (generation < 0) {
            throw new IllegalArgumentException(
                "Generation must not be negative."
            );
        }

        Objects.requireNonNull(
            simulations, "Simulations are required."
        );

        if (simulations.length == 0) {
            throw new IllegalArgumentException(
                "Population must not be empty."
            );
        }

        this.generation = generation;
        this.simulations = simulations.clone();
        this.bases = new Base[simulations.length];
        this.initialEdgeLengths = new double[simulations.length];
        this.results = new SimulationResult[simulations.length];

        for (int i = 0; i < simulations.length; i++) {
            Simulation simulation = Objects.requireNonNull(
                this.simulations[i], "Simulation must not be null."
            );

            Base base = Objects.requireNonNull(
                simulation.base(), "Simulation must provide a base."
            );

            if (base.over()
                    || base.ticks() != 0
                    || base.enemiesKilled() != 0
                    || base.wavesCompleted() != 0) {
                throw new IllegalArgumentException(
                    "Generation requires fresh simulations."
                );
            }

            for (int j = 0; j < i; j++) {
                if (bases[j] == base
                        || bases[j].graph() == base.graph()) {
                    throw new IllegalArgumentException(
                        "Individuals must use separate bases and graphs."
                    );
                }
            }

            double length = base.totalEdgeLength();

            if (!Double.isFinite(length) || length < 0.0) {
                throw new IllegalArgumentException(
                    "Initial edge length is invalid."
                );
            }

            bases[i] = base;
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

            ensureSameBase(i);

            Base base = bases[i];

            if (!base.over()) {
                int previousTicks = base.ticks();

                simulations[i].tick();

                ensureSameBase(i);

                if (base.ticks() != previousTicks + 1) {
                    throw new IllegalStateException(
                        "Simulation must advance exactly one base tick."
                    );
                }
            }

            if (base.over()) {
                results[i] = new SimulationResult(
                    generation,
                    i,
                    base.wavesCompleted(),
                    base.enemiesKilled(),
                    base.survivalSeconds(),
                    initialEdgeLengths[i]
                );

                defeatsRegistered++;
            }
        }
    }

    private void ensureSameBase(int index) {
        if (simulations[index].base() != bases[index]) {
            throw new IllegalStateException(
                "Simulation replaced its base during evaluation."
            );
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
                "All bases must be defeated before ranking."
            );
        }

        SimulationResult[] ordered =
            Arrays.copyOf(results, results.length);

        Arrays.sort(ordered, GenerationEvaluation::compareResults);

        return ordered;
    }

    public SimulationResult[] selectParents(int count) {
        if (count <= 0 || count > simulations.length) {
            throw new IllegalArgumentException(
                "Parent count must be within population bounds."
            );
        }

        return Arrays.copyOf(ranking(), count);
    }

    private static int compareResults(
            SimulationResult a,
            SimulationResult b) {

        int comparison = Integer.compare(
            b.wavesCompleted(), a.wavesCompleted()
        );

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