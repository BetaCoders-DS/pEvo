package com.github.betacoders.evolution;

import com.github.betacoders.collections.AdjacencyGraph;
import com.github.betacoders.collections.Graph;
import com.github.betacoders.world.Base;
import com.github.betacoders.world.Cells;
import com.github.betacoders.world.Link;
import com.github.betacoders.world.Simulation;

public final class EvolutionDemo {
    public static void main(String[] args) {
        verifyWavePriority();
        verifyGenerationEvaluation();

        System.out.println("All evaluation checks passed.");
    }

    private static void verifyWavePriority() {
        SimulationResult moreWaves = new SimulationResult(
            1, 0, 4, 0, 0.0, 1_000_000.0
        );

        SimulationResult fewerWaves = new SimulationResult(
            1, 1, 3, 1_000_000, 1_000_000.0, 0.0
        );

        if (moreWaves.score() <= fewerWaves.score()) {
            throw new AssertionError(
                "Completed waves must have priority."
            );
        }
    }

    private static void verifyGenerationEvaluation() {
        TestSimulation compact = new TestSimulation(1000.0, 1);
        TestSimulation longer = new TestSimulation(2000.0, 2);

        GenerationEvaluation evaluation = new GenerationEvaluation(
            1,
            new Simulation[] {compact, longer}
        );

        evaluation.step();

        if (evaluation.allDead()) {
            throw new AssertionError(
                "One simulation should still be active."
            );
        }

        if (evaluation.defeatsRegistered() != 1) {
            throw new AssertionError(
                "Exactly one defeat should be registered."
            );
        }

        try {
            evaluation.ranking();

            throw new AssertionError(
                "Ranking must wait for all defeats."
            );
        } catch (IllegalStateException expected) {
        }

        evaluation.step();

        if (!evaluation.allDead()) {
            throw new AssertionError(
                "All simulations should be finished."
            );
        }

        evaluation.step();

        if (compact.base().ticks() != 1
                || longer.base().ticks() != 2) {
            throw new AssertionError(
                "Defeated simulations must stop advancing."
            );
        }

        if (evaluation.defeatsRegistered() != 2) {
            throw new AssertionError(
                "Each defeat must be registered once."
            );
        }

        SimulationResult[] ranking = evaluation.ranking();

        if (ranking[0].individualId() != 0) {
            throw new AssertionError(
                "The compact base should win this scenario."
            );
        }

        if (ranking[0].initialEdgeLength() != 1000.0) {
            throw new AssertionError(
                "Undirected edges must be counted once."
            );
        }

        SimulationResult[] parents = evaluation.selectParents(1);

        if (parents.length != 1 || parents[0].individualId() != 0) {
            throw new AssertionError(
                "Parent selection must follow the ranking."
            );
        }

        for (SimulationResult result : ranking) {
            System.out.printf(
                "Base %d | Waves: %d | Kills: %d"
                    + " | Time: %.4fs | Edge length: %.2f"
                    + " | Score: %.2f%n",
                result.individualId(),
                result.wavesCompleted(),
                result.enemiesKilled(),
                result.survivalSeconds(),
                result.initialEdgeLength(),
                result.score()
            );
        }
    }

    private static final class TestSimulation implements Simulation {
        private final Base base;
        private final int deathTick;

        private TestSimulation(double length, int deathTick) {
            Graph<Cells, Link> graph = new AdjacencyGraph<>();

            Cells.Core core = new Cells.Core(0, 0.0, 0.0);
            Cells.Relay relay = new Cells.Relay(1, length, 0.0);

            graph.addEdge(core, relay, new Link(core, relay));

            this.base = new Base(graph, 42L);
            this.deathTick = deathTick;
        }

        @Override
        public Base base() {
            return base;
        }

        @Override
        public void tick() {
            if (!base.tick()) {
                return;
            }

            if (base.ticks() == 1) {
                base.recordEnemyKilled();
                base.completeWave();
            }

            if (base.ticks() >= deathTick) {
                base.core().damage(base.core().hp());
            }
        }
    }
}