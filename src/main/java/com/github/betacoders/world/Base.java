package com.github.betacoders.world;

import java.util.HashSet;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import com.github.betacoders.algo.AStarPathfinder;
import com.github.betacoders.collections.Graph;
import com.github.betacoders.collections.List;
import com.github.betacoders.collections.Vector;

public class Base {
    public static final int TICKS_PER_SECOND = 60;

    private final Graph<Cells, Link> graph;
    private Cells.Core core;
    private final AStarPathfinder<Cells, Link> pathfinder;
    private final List<Packet> packets;
    private final Random rng;

    private int wave = 1;
    private boolean over = false;
    private int tick = 0;
    private int enemiesKilled = 0;

    public Base(Graph<Cells, Link> graph, long seed) {
        this.graph = Objects.requireNonNull(
            graph, "O grafo é obrigatório."
        );

        this.rng = new Random(seed);

        this.pathfinder = new AStarPathfinder<>(
            graph,
            Link::cost,
            (a, b) -> (int) Math.hypot(
                a.pos().x - b.pos().x,
                a.pos().y - b.pos().y
            )
        );

        this.packets = new Vector<>();

        for (Cells cell : graph) {
            if (cell instanceof Cells.Core c) {
                if (core != null) {
                    throw new IllegalArgumentException(
                        "A base deve ter apenas um núcleo."
                    );
                }

                core = c;
            }
        }

        if (core == null) {
            throw new IllegalArgumentException(
                "A base precisa de um núcleo."
            );
        }
    }

    public Graph<Cells, Link> graph() {
        return graph;
    }

    public Cells.Core core() {
        return core;
    }

    public AStarPathfinder<Cells, Link> pathfinder() {
        return pathfinder;
    }

    public List<Packet> packets() {
        return packets;
    }

    public int wave() {
        return wave;
    }

    public int wavesCompleted() {
        return wave - 1;
    }

    public boolean over() {
        return over || !core.alive();
    }

    public int ticks() {
        return tick;
    }

    public int enemiesKilled() {
        return enemiesKilled;
    }

    public double survivalSeconds() {
        return tick / (double) TICKS_PER_SECOND;
    }

    public double totalEdgeLength() {
        Set<Link> uniqueEdges = new HashSet<>();

        for (Cells cell : graph) {
            graph.forEachNeighbor(
                cell,
                (neighbor, link) -> uniqueEdges.add(link)
            );
        }

        double total = 0.0;

        for (Link link : uniqueEdges) {
            total += link.length();
        }

        return total;
    }

    public void recordEnemyKilled() {
        if (!over()) {
            enemiesKilled++;
        }
    }

    public void completeWave() {
        if (!over()) {
            wave++;
        }
    }

    public boolean tick() {
        if (over()) {
            over = true;
            return false;
        }

        tick++;
        return true;
    }
}