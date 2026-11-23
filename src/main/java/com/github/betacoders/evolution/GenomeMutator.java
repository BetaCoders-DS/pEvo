package com.github.betacoders.evolution;

import java.util.Objects;
import java.util.Random;

import com.github.betacoders.collections.ImmutableList;
import com.github.betacoders.collections.List;
import com.github.betacoders.collections.Vector;
import com.github.betacoders.evolution.BaseGenome.CellGene;
import com.github.betacoders.evolution.BaseGenome.CellType;
import com.github.betacoders.evolution.BaseGenome.EdgeGene;

public final class GenomeMutator {
    private GenomeMutator() {
    }

    public static BaseGenome adapt(
            BaseGenome parent,
            int minCells,
            int maxCells,
            Random random) {

        Objects.requireNonNull(parent);
        Objects.requireNonNull(random);

        if (minCells < 3 || maxCells < minCells) {
            throw new IllegalArgumentException(
                "Invalid cell limits."
            );
        }

        if (parent.cells().size() >= minCells
                && parent.cells().size() <= maxCells) {
            return parent;
        }

        List<CellGene> cells = copy(parent.cells());
        List<EdgeGene> edges = copy(parent.edges());

        while (cells.size() < minCells) {
            addCell(parent, cells, edges, random);
        }

        while (cells.size() > maxCells) {
            removeCell(cells, edges, random);
        }

        return new BaseGenome(
            parent.width(),
            parent.height(),
            cells,
            edges
        );
    }

    public static BaseGenome mutate(
            BaseGenome parent,
            int minCells,
            int maxCells,
            Random random) {

        BaseGenome fitted =
            adapt(parent, minCells, maxCells, random);

        for (int attempt = 0; attempt < 60; attempt++) {
            List<CellGene> cells = copy(fitted.cells());
            List<EdgeGene> edges = copy(fitted.edges());

            int index =
                1 + random.nextInt(cells.size() - 1);

            CellGene cell = cells.get(index);

            try {
                switch (random.nextInt(5)) {
                    case 0 -> cells.set(index, new CellGene(
                        cell.type(),
                        cell.x() + (random.nextDouble() * 2 - 1) * 80,
                        cell.y() + (random.nextDouble() * 2 - 1) * 80
                    ));

                    case 1 -> cells.set(index, new CellGene(
                        BaseFactory.randomType(random),
                        cell.x(),
                        cell.y()
                    ));

                    case 2 -> {
                        if (cells.size() >= maxCells) {
                            continue;
                        }

                        addCell(fitted, cells, edges, random);
                    }

                    case 3 -> {
                        if (cells.size() <= minCells) {
                            continue;
                        }

                        removeCell(cells, edges, random);
                    }

                    case 4 -> {
                        edges.remove(
                            random.nextInt(edges.size())
                        );

                        int a = random.nextInt(cells.size());
                        int b = random.nextInt(cells.size() - 1);

                        if (b >= a) {
                            b++;
                        }

                        edges.add(new EdgeGene(a, b));

                        BaseFactory.repairConnectivity(
                            cells.size(), edges, random
                        );
                    }
                }

                if (ImmutableList.sameElements(
                        cells, fitted.cells())
                        && ImmutableList.sameElements(
                            edges, fitted.edges())) {
                    continue;
                }

                return new BaseGenome(
                    fitted.width(),
                    fitted.height(),
                    cells,
                    edges
                );
            } catch (IllegalArgumentException rejected) {
            }
        }

        return fitted;
    }

    private static <T> Vector<T> copy(List<T> source) {
        Vector<T> result = new Vector<>(source.size());
        result.addAll(source);
        return result;
    }

    private static void addCell(
            BaseGenome parent,
            List<CellGene> cells,
            List<EdgeGene> edges,
            Random random) {

        int index = cells.size();

        BaseFactory.appendCell(
            cells,
            BaseFactory.randomType(random),
            parent.width(),
            parent.height(),
            random
        );

        edges.add(new EdgeGene(
            index, random.nextInt(index)
        ));
    }

    private static void removeCell(
            List<CellGene> cells,
            List<EdgeGene> edges,
            Random random) {

        int extractors = 0;
        int turrets = 0;

        for (CellGene cell : cells) {
            if (cell.type() == CellType.EXTRACTOR) {
                extractors++;
            }

            if (cell.type() == CellType.TURRET) {
                turrets++;
            }
        }

        List<Integer> removable = new Vector<>();

        for (int i = 1; i < cells.size(); i++) {
            CellType type = cells.get(i).type();

            if (type == CellType.RELAY
                    || type == CellType.EXTRACTOR && extractors > 1
                    || type == CellType.TURRET && turrets > 1) {
                removable.add(i);
            }
        }

        if (removable.isEmpty()) {
            throw new IllegalArgumentException(
                "No cell can be removed."
            );
        }

        int removed =
            removable.get(random.nextInt(removable.size()));

        cells.remove(removed);

        List<EdgeGene> remaining = new Vector<>();

        for (EdgeGene edge : edges) {
            if (edge.a() != removed && edge.b() != removed) {
                remaining.add(new EdgeGene(
                    edge.a() > removed ? edge.a() - 1 : edge.a(),
                    edge.b() > removed ? edge.b() - 1 : edge.b()
                ));
            }
        }

        edges.clear();
        edges.addAll(remaining);

        BaseFactory.repairConnectivity(
            cells.size(), edges, random
        );
    }
}