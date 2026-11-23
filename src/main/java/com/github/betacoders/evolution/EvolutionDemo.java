package com.github.betacoders.evolution;

import java.util.Random;

import com.github.betacoders.collections.HashSet;
import com.github.betacoders.collections.ImmutableList;
import com.github.betacoders.collections.List;
import com.github.betacoders.collections.Set;
import com.github.betacoders.collections.Vector;
import com.github.betacoders.world.Cells;
import com.github.betacoders.world.Simulation;
import com.github.betacoders.world.World;

public final class EvolutionDemo {
    public static void main(String[] args) {
        verifyVector();

        EvolutionConfig config =
            EvolutionConfig.standard();

        BaseGenome[] population =
            BaseFactory.randomPopulation(
                config, 1, new Random(2026)
            );

        Set<Integer> sizes = new HashSet<>();
        Set<Integer> turretCounts = new HashSet<>();

        for (BaseGenome genome : population) {
            verifyGenome(genome, 5, 12);
            sizes.add(genome.cells().size());

            int count = 0;

            for (BaseGenome.CellGene cell : genome.cells()) {
                if (cell.type() == BaseGenome.CellType.TURRET) {
                    count++;
                }
            }

            turretCounts.add(count);
        }

        require(
            sizes.size() > 1 && turretCounts.size() > 1,
            "Population did not vary."
        );

        require(
            config.maxCells(6) == 15
                && config.maxCells(1000) == 30,
            "Invalid growth."
        );

        BaseGenome original = population[0];

        List<BaseGenome.CellGene> saved =
            new ImmutableList<>(original.cells());

        BaseGenome mutated = original;
        Random random = new Random(91);

        for (int i = 0; i < 300; i++) {
            mutated = GenomeMutator.mutate(
                mutated, 5, 18, random
            );

            verifyGenome(mutated, 5, 18);
        }

        require(
            original.cells().equals(saved),
            "Parent was modified."
        );

        verifyGenome(
            GenomeMutator.adapt(original, 15, 18, random),
            15, 18
        );

        verifyGenome(
            GenomeMutator.adapt(mutated, 3, 3, random),
            3, 3
        );

        verifyIsolation(original);
        verifyEvaluation(original);
        verifyExperiment();

        System.out.println("All evolution checks passed.");
    }

    private static void verifyVector() {
        Vector<Integer> values = new Vector<>();

        values.add(10);
        values.add(20);
        values.add(30);

        require(
            values.swapRemove(0) == 10,
            "Wrong removed value."
        );

        require(
            values.size() == 2 && values.get(0) == 30,
            "Swap removal did not shrink."
        );

        require(
            values.swapRemove(1) == 20 && values.size() == 1,
            "Last removal failed."
        );

        require(
            values.swapRemove(0) == 30 && values.isEmpty(),
            "Vector did not empty."
        );
    }

    private static void verifyGenome(
            BaseGenome genome,
            int minimum,
            int maximum) {

        require(
            genome.cells().size() >= minimum
                && genome.cells().size() <= maximum,
            "Cell limits were violated."
        );

        new BaseGenome(
            genome.width(),
            genome.height(),
            genome.cells(),
            genome.edges()
        );
    }

    private static void verifyIsolation(BaseGenome genome) {
        World first = BaseFactory.createWorld(genome);
        World second = BaseFactory.createWorld(genome);

        for (int i = 0; i < 200; i++) {
            first.tick();
        }

        require(
            second.ticks() == 0
                && second.base().packets().isEmpty(),
            "World state was shared."
        );

        require(
            resources(first) > 0 && resources(second) == 0,
            "Ammo production or isolation failed."
        );

        first.base().core().damage(
            first.base().core().hp()
        );

        require(
            first.over() && !second.over(),
            "Defeat was not detected immediately."
        );

        int ticks = first.ticks();
        first.tick();

        require(
            first.ticks() == ticks,
            "Defeated world kept advancing."
        );

        try {
            genome.cells().clear();
            throw new AssertionError("Genome is mutable.");
        } catch (UnsupportedOperationException expected) {
        }
    }

    private static int resources(World world) {
        int total = world.base().packets().size();

        for (Cells cell : world.graph()) {
            if (cell instanceof Cells.Extractor extractor) {
                total += extractor.stored();
            }

            if (cell instanceof Cells.Turret turret) {
                total += turret.stored();
            }
        }

        return total;
    }

    private static void verifyEvaluation(BaseGenome genome) {
        World first = BaseFactory.createWorld(genome);
        World second = BaseFactory.createWorld(genome);

        int count = first.cellCount();
        double length = first.totalEdgeLength();

        GenerationEvaluation evaluation =
            new GenerationEvaluation(
                1,
                new Simulation[] {
                    new TestSimulation(first, 42, 1),
                    new TestSimulation(second, 42, 2)
                }
            );

        try {
            evaluation.ranking();

            throw new AssertionError(
                "Ranking was released early."
            );
        } catch (IllegalStateException expected) {
        }

        for (Cells cell : first.graph()) {
            if (cell != first.base().core()) {
                first.graph().removeVertex(cell);
                break;
            }
        }

        evaluation.step();

        require(
            evaluation.defeatsRegistered() == 1,
            "Defeat was not registered."
        );

        evaluation.step();
        evaluation.step();

        require(
            evaluation.allDead()
                && evaluation.defeatsRegistered() == 2,
            "Defeats were counted incorrectly."
        );

        require(
            first.ticks() == 1 && second.ticks() == 2,
            "Finished ticks changed."
        );

        for (SimulationResult result : evaluation.ranking()) {
            if (result.individualId() == 0) {
                require(
                    result.initialCellCount() == count
                        && result.initialEdgeLength() == length,
                    "Initial costs changed."
                );
            }
        }

        SimulationResult small =
            new SimulationResult(
                1, 0, 3, 10, 60, 1000, 5
            );

        SimulationResult large =
            new SimulationResult(
                1, 1, 3, 10, 60, 1000, 12
            );

        require(
            small.score() > large.score(),
            "Initial size cost was ignored."
        );

        require(
            evaluation.selectParents(1).length == 1,
            "Parent selection failed."
        );
    }

    private static void verifyExperiment() {
        EvolutionConfig config = new EvolutionConfig(
            12, 1000, 700, 5, 8, 2, 1, 2, 18
        );

        EvolutionExperiment experiment =
            new EvolutionExperiment(
                config, TestSimulation::new
            );

        experiment.restart(2026);

        BaseGenome[] initial = experiment.genomes();
        long scenario = experiment.scenarioSeed();

        for (int generation = 1; generation <= 8; generation++) {
            require(
                experiment.generation() == generation,
                "Wrong generation."
            );

            for (BaseGenome genome : experiment.genomes()) {
                verifyGenome(
                    genome,
                    config.minCells(generation),
                    config.maxCells(generation)
                );
            }

            for (Simulation simulation : experiment.simulations()) {
                require(
                    ((TestSimulation) simulation).seed == scenario,
                    "Scenario seed changed."
                );
            }

            while (!experiment.generationFinished()) {
                experiment.step();
            }

            require(
                experiment.ranking().length == config.populationSize(),
                "Population changed."
            );

            if (generation < 8) {
                experiment.step();
            }
        }

        require(
            experiment.previousRanking().length == config.populationSize(),
            "History was lost."
        );

        experiment.restart(2026);

        BaseGenome[] replay = experiment.genomes();

        for (int i = 0; i < initial.length; i++) {
            require(
                initial[i].cells().equals(replay[i].cells())
                    && initial[i].edges().equals(replay[i].edges()),
                "Replay failed."
            );
        }

        experiment.restart();

        require(
            experiment.seed() != 2026
                && experiment.generation() == 1,
            "Random restart failed."
        );

        require(
            experiment.previousRanking().length == 0,
            "Restart kept old results."
        );

        require(
            !initial[0].cells().equals(
                experiment.genomes()[0].cells()
            ),
            "Random restart repeated the initial layout."
        );
    }

    private static void require(
            boolean condition,
            String message) {

        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static final class TestSimulation implements Simulation {
        private final World world;
        private final long seed;
        private final int deathTick;

        private TestSimulation(World world, long seed) {
            this(
                world,
                seed,
                2 + world.cellCount() % 3
            );
        }

        private TestSimulation(
                World world,
                long seed,
                int deathTick) {

            this.world = world;
            this.seed = seed;
            this.deathTick = deathTick;
        }

        @Override
        public World world() {
            return world;
        }

        @Override
        public void tick() {
            if (!world.tick()) {
                return;
            }

            if (world.ticks() == 1) {
                world.recordEnemyKilled();
                world.completeWave();
            }

            if (world.ticks() >= deathTick) {
                Cells.Core core = world.base().core();
                core.damage(core.hp());
            }
        }
    }
}