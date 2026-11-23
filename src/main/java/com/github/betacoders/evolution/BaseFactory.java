package com.github.betacoders.evolution;

import java.util.Objects;
import java.util.Random;

import com.github.betacoders.collections.AdjacencyGraph;
import com.github.betacoders.collections.Graph;
import com.github.betacoders.collections.HashSet;
import com.github.betacoders.collections.List;
import com.github.betacoders.collections.Set;
import com.github.betacoders.collections.Vector;
import com.github.betacoders.evolution.BaseGenome.CellGene;
import com.github.betacoders.evolution.BaseGenome.CellType;
import com.github.betacoders.evolution.BaseGenome.EdgeGene;
import com.github.betacoders.world.Base;
import com.github.betacoders.world.Cells;
import com.github.betacoders.world.Link;
import com.github.betacoders.world.World;

public final class BaseFactory {
    private BaseFactory() {
    }

    public static BaseGenome[] randomPopulation(
            EvolutionConfig config,
            int generation,
            Random random) {

        Objects.requireNonNull(config);
        Objects.requireNonNull(random);

        BaseGenome[] population =
            new BaseGenome[config.populationSize()];

        for (int i = 0; i < population.length; i++) {
            population[i] = randomGenome(
                config.width(),
                config.height(),
                config.minCells(generation),
                config.maxCells(generation),
                random
            );
        }

        return population;
    }

    public static BaseGenome randomGenome(
            double width,
            double height,
            int minCells,
            int maxCells,
            Random random) {

        BaseGenome.validateDimensions(width, height);
        Objects.requireNonNull(random);

        if (minCells < 3 || maxCells < minCells) {
            throw new IllegalArgumentException(
                "Invalid cell limits."
            );
        }

        int count =
            minCells + random.nextInt(maxCells - minCells + 1);

        List<CellGene> cells = new Vector<>();

        cells.add(new CellGene(
            CellType.CORE, width / 2, height / 2
        ));

        appendCell(
            cells, CellType.EXTRACTOR,
            width, height, random
        );

        appendCell(
            cells, CellType.TURRET,
            width, height, random
        );

        while (cells.size() < count) {
            appendCell(
                cells, randomType(random),
                width, height, random
            );
        }

        shuffle(cells, 1, random);

        List<EdgeGene> edges = new Vector<>();
        Set<EdgeGene> selected = new HashSet<>();

        for (int i = 1; i < count; i++) {
            EdgeGene edge = new EdgeGene(
                i, random.nextInt(i)
            );

            edges.add(edge);
            selected.add(edge);
        }

        List<EdgeGene> candidates = new Vector<>();

        for (int a = 0; a < count; a++) {
            for (int b = a + 1; b < count; b++) {
                EdgeGene edge = new EdgeGene(a, b);

                if (!selected.contains(edge)) {
                    candidates.add(edge);
                }
            }
        }

        shuffle(candidates, 0, random);

        for (int i = 0;
                i < Math.min(count / 2, candidates.size());
                i++) {
            edges.add(candidates.get(i));
        }

        return new BaseGenome(
            width, height, cells, edges
        );
    }

    private static <T> void shuffle(
            List<T> items,
            int first,
            Random random) {

        for (int i = items.size() - 1; i > first; i--) {
            int other =
                first + random.nextInt(i - first + 1);

            T temporary = items.get(i);
            items.set(i, items.get(other));
            items.set(other, temporary);
        }
    }

    static CellType randomType(Random random) {
        return switch (random.nextInt(3)) {
            case 0 -> CellType.RELAY;
            case 1 -> CellType.EXTRACTOR;
            default -> CellType.TURRET;
        };
    }

    static void appendCell(
            List<CellGene> cells,
            CellType type,
            double width,
            double height,
            Random random) {

        for (int attempt = 0; attempt < 1000; attempt++) {
            double x = BaseGenome.BORDER_MARGIN
                + random.nextDouble()
                * (width - 2 * BaseGenome.BORDER_MARGIN);

            double y = BaseGenome.BORDER_MARGIN
                + random.nextDouble()
                * (height - 2 * BaseGenome.BORDER_MARGIN);

            boolean available = true;

            for (CellGene cell : cells) {
                if (Math.hypot(
                        x - cell.x(),
                        y - cell.y()) < BaseGenome.MIN_DISTANCE) {
                    available = false;
                    break;
                }
            }

            if (available) {
                cells.add(new CellGene(type, x, y));
                return;
            }
        }

        throw new IllegalArgumentException(
            "Not enough space for the requested cells."
        );
    }

    static void repairConnectivity(
            int count,
            List<EdgeGene> edges,
            Random random) {

        while (true) {
            boolean[] visited =
                BaseGenome.reachable(count, edges);

            List<Integer> connected = new Vector<>();
            List<Integer> disconnected = new Vector<>();

            for (int i = 0; i < count; i++) {
                (visited[i] ? connected : disconnected).add(i);
            }

            if (disconnected.isEmpty()) {
                return;
            }

            edges.add(new EdgeGene(
                connected.get(
                    random.nextInt(connected.size())
                ),
                disconnected.get(
                    random.nextInt(disconnected.size())
                )
            ));
        }
    }

    public static World createWorld(BaseGenome genome) {
        Objects.requireNonNull(genome);

        Graph<Cells, Link> graph = new AdjacencyGraph<>();
        Cells[] cells = new Cells[genome.cells().size()];

        for (int id = 0; id < cells.length; id++) {
            CellGene gene = genome.cells().get(id);

            cells[id] = switch (gene.type()) {
                case CORE -> new Cells.Core(
                    id, gene.x(), gene.y()
                );
                case RELAY -> new Cells.Relay(
                    id, gene.x(), gene.y()
                );
                case EXTRACTOR -> new Cells.Extractor(
                    id, gene.x(), gene.y(), 5, 50
                );
                case TURRET -> new Cells.Turret(
                    id, gene.x(), gene.y(), 6, 170, 12f, 60
                );
            };

            graph.addVertex(cells[id]);
        }

        for (EdgeGene edge : genome.edges()) {
            Cells a = cells[edge.a()];
            Cells b = cells[edge.b()];

            graph.addEdge(a, b, new Link(a, b));
        }

        return new World(new Base(graph));
    }
}
