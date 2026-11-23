package com.github.betacoders.evolution;

import java.util.Objects;

import com.github.betacoders.collections.Graph;
import com.github.betacoders.collections.HashSet;
import com.github.betacoders.collections.LinkedList;
import com.github.betacoders.collections.Set;
import com.github.betacoders.world.Cells;
import com.github.betacoders.world.Link;
import com.github.betacoders.world.Simulation;
import com.github.betacoders.world.World;

public final class GenerationEvaluation {
    private final int generation;
    private final Simulation[] simulations;
    private final World[] worlds;
    private final double[] initialLengths;
    private final int[] initialCounts;
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

        Objects.requireNonNull(simulations);

        if (simulations.length == 0) {
            throw new IllegalArgumentException(
                "Population must not be empty."
            );
        }

        this.generation = generation;
        this.simulations = simulations.clone();

        worlds = new World[simulations.length];
        initialLengths = new double[simulations.length];
        initialCounts = new int[simulations.length];
        results = new SimulationResult[simulations.length];

        Set<World> uniqueWorlds = new HashSet<>();

        Set<Graph<Cells, Link>> uniqueGraphs =
            new HashSet<>();

        Set<Cells> uniqueCells = new HashSet<>();

        for (int i = 0; i < simulations.length; i++) {
            Simulation simulation =
                Objects.requireNonNull(this.simulations[i]);

            World world =
                Objects.requireNonNull(simulation.world());

            if (world.over()
                    || world.ticks() != 0
                    || world.enemiesKilled() != 0
                    || world.wavesCompleted() != 0) {
                throw new IllegalArgumentException(
                    "Fresh worlds are required."
                );
            }

            if (!uniqueWorlds.add(world)
                    || !uniqueGraphs.add(world.graph())) {
                throw new IllegalArgumentException(
                    "Worlds and graphs must be independent."
                );
            }

            for (Cells cell : world.graph()) {
                if (!uniqueCells.add(cell)) {
                    throw new IllegalArgumentException(
                        "Worlds must not share cells."
                    );
                }
            }

            double length = world.totalEdgeLength();

            if (!Double.isFinite(length) || length < 0) {
                throw new IllegalArgumentException(
                    "Invalid initial edge length."
                );
            }

            worlds[i] = world;
            initialLengths[i] = length;
            initialCounts[i] = world.cellCount();
        }
    }

    public void step() {
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
                        "Each step must advance one world tick."
                    );
                }
            }

            if (world.over()) {
                results[i] = new SimulationResult(
                    generation,
                    i,
                    world.wavesCompleted(),
                    world.enemiesKilled(),
                    world.ticks(),
                    initialLengths[i],
                    initialCounts[i]
                );

                defeatsRegistered++;
            }
        }
    }

    private void ensureSameWorld(int index) {
        if (simulations[index].world() != worlds[index]) {
            throw new IllegalStateException(
                "World changed during evaluation."
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
                "Ranking requires all worlds to finish."
            );
        }

        LinkedList<SimulationResult> ordered =
            new LinkedList<>();

        for (SimulationResult result : results) {
            ordered.addLast(result);
        }

        ordered.sort(
            GenerationEvaluation::compareResults
        );

        SimulationResult[] ranking =
            new SimulationResult[results.length];

        int index = 0;

        for (SimulationResult result : ordered) {
            ranking[index++] = result;
        }

        return ranking;
    }

    public SimulationResult[] selectParents(int count) {
        if (count <= 0 || count > simulations.length) {
            throw new IllegalArgumentException(
                "Invalid parent count."
            );
        }

        SimulationResult[] selected =
            new SimulationResult[count];

        System.arraycopy(
            ranking(), 0, selected, 0, count
        );

        return selected;
    }

    private static int compareResults(
            SimulationResult a,
            SimulationResult b) {

        int comparison = Integer.compare(
            b.wavesCompleted(),
            a.wavesCompleted()
        );

        if (comparison != 0) {
            return comparison;
        }

        comparison = Double.compare(
            b.score(),
            a.score()
        );

        return comparison != 0
            ? comparison
            : Integer.compare(
                a.individualId(),
                b.individualId()
            );
    }
}