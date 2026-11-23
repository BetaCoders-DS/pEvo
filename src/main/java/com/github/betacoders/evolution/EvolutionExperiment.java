package com.github.betacoders.evolution;

import java.util.Objects;
import java.util.Random;

import com.github.betacoders.world.Simulation;
import com.github.betacoders.world.World;

public final class EvolutionExperiment {
    private final EvolutionConfig config;
    private final SimulationFactory simulationFactory;
    private final Random seedGenerator = new Random();

    private Random evolutionRandom;
    private long seed;
    private long scenarioSeed;
    private int generation;
    private BaseGenome[] genomes;
    private Simulation[] simulations;
    private GenerationEvaluation evaluation;

    private SimulationResult[] previousRanking =
        new SimulationResult[0];

    public EvolutionExperiment(
            EvolutionConfig config,
            SimulationFactory simulationFactory) {

        this.config = Objects.requireNonNull(config);

        this.simulationFactory =
            Objects.requireNonNull(simulationFactory);

        restart();
    }

    public void restart() {
        long nextSeed;

        do {
            nextSeed = seedGenerator.nextLong();
        } while (nextSeed == seed);

        restart(nextSeed);
    }

    public void restart(long seed) {
        this.seed = seed;
        evolutionRandom = new Random(seed);
        scenarioSeed = evolutionRandom.nextLong();
        generation = 1;
        previousRanking = new SimulationResult[0];

        genomes = BaseFactory.randomPopulation(
            config, generation, evolutionRandom
        );

        startEvaluation();
    }

    public void step() {
        if (evaluation.allDead()) {
            nextGeneration();
        }

        evaluation.step();
    }

    private void nextGeneration() {
        SimulationResult[] ranking = evaluation.ranking();

        int nextGeneration =
            Math.addExact(generation, 1);

        int minimum = config.minCells(nextGeneration);
        int maximum = config.maxCells(nextGeneration);

        BaseGenome[] next =
            new BaseGenome[genomes.length];

        int elites = Math.max(1, next.length / 10);
        int immigrants = Math.max(1, next.length / 10);
        int parentCount = Math.max(1, next.length / 4);

        for (int i = 0; i < elites; i++) {
            next[i] = GenomeMutator.adapt(
                genomes[ranking[i].individualId()],
                minimum,
                maximum,
                evolutionRandom
            );
        }

        for (int i = elites; i < next.length - immigrants; i++) {
            int parent = ranking[
                evolutionRandom.nextInt(parentCount)
            ].individualId();

            next[i] = GenomeMutator.mutate(
                genomes[parent],
                minimum,
                maximum,
                evolutionRandom
            );
        }

        for (int i = next.length - immigrants; i < next.length; i++) {
            next[i] = BaseFactory.randomGenome(
                config.width(),
                config.height(),
                minimum,
                maximum,
                evolutionRandom
            );
        }

        previousRanking = ranking;
        genomes = next;
        generation = nextGeneration;

        startEvaluation();
    }

    private void startEvaluation() {
        simulations = new Simulation[genomes.length];

        for (int i = 0; i < genomes.length; i++) {
            World world =
                BaseFactory.createWorld(genomes[i]);

            simulations[i] = Objects.requireNonNull(
                simulationFactory.create(world, scenarioSeed)
            );

            if (simulations[i].world() != world) {
                throw new IllegalArgumentException(
                    "Simulation must use the supplied world."
                );
            }
        }

        evaluation = new GenerationEvaluation(
            generation, simulations
        );
    }

    public int generation() {
        return generation;
    }

    public long seed() {
        return seed;
    }

    public long scenarioSeed() {
        return scenarioSeed;
    }

    public boolean generationFinished() {
        return evaluation.allDead();
    }

    public BaseGenome[] genomes() {
        return genomes.clone();
    }

    public Simulation[] simulations() {
        return simulations.clone();
    }

    public SimulationResult[] ranking() {
        return evaluation.ranking();
    }

    public SimulationResult[] previousRanking() {
        return previousRanking.clone();
    }
}