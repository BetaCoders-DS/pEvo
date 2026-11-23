package com.github.betacoders.evolution;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class BaseGenome {
    public static final double BORDER_MARGIN = 40.0;
    public static final double MIN_DISTANCE = 70.0;

    public enum CellType {
        CORE, RELAY, EXTRACTOR, TURRET
    }

    public record CellGene(CellType type, double x, double y) {
        public CellGene {
            Objects.requireNonNull(type, "Cell type is required.");

            if (!Double.isFinite(x) || !Double.isFinite(y)) {
                throw new IllegalArgumentException(
                    "Coordinates must be finite."
                );
            }
        }
    }

    public record EdgeGene(int a, int b) {
        public EdgeGene {
            if (a < 0 || b < 0 || a == b) {
                throw new IllegalArgumentException(
                    "Invalid edge endpoints."
                );
            }

            if (a > b) {
                int temporary = a;
                a = b;
                b = temporary;
            }
        }
    }

    private final double width;
    private final double height;
    private final List<CellGene> cells;
    private final List<EdgeGene> edges;

    public BaseGenome(
            double width,
            double height,
            List<CellGene> cells,
            List<EdgeGene> edges) {

        validateDimensions(width, height);

        this.width = width;
        this.height = height;
        this.cells = List.copyOf(cells);
        this.edges = List.copyOf(edges);

        validateCells();

        Set<EdgeGene> unique = new HashSet<>();

        for (EdgeGene edge : this.edges) {
            if (edge.b() >= this.cells.size() || !unique.add(edge)) {
                throw new IllegalArgumentException(
                    "Invalid or duplicate edge."
                );
            }
        }

        for (boolean visited : reachable(this.cells.size(), this.edges)) {
            if (!visited) {
                throw new IllegalArgumentException(
                    "The graph must be connected."
                );
            }
        }
    }

    static void validateDimensions(double width, double height) {
        if (!Double.isFinite(width)
                || !Double.isFinite(height)
                || width <= 2 * BORDER_MARGIN
                || height <= 2 * BORDER_MARGIN) {
            throw new IllegalArgumentException(
                "Invalid world dimensions."
            );
        }
    }

    private void validateCells() {
        if (cells.size() < 3
                || cells.get(0).type() != CellType.CORE
                || cells.get(0).x() != width / 2
                || cells.get(0).y() != height / 2) {
            throw new IllegalArgumentException(
                "A centered core is required."
            );
        }

        int extractors = 0;
        int turrets = 0;

        for (int i = 0; i < cells.size(); i++) {
            CellGene cell = cells.get(i);

            if (i > 0 && cell.type() == CellType.CORE) {
                throw new IllegalArgumentException(
                    "Only one core is allowed."
                );
            }

            if (cell.type() == CellType.EXTRACTOR) {
                extractors++;
            }

            if (cell.type() == CellType.TURRET) {
                turrets++;
            }

            if (cell.x() < BORDER_MARGIN
                    || cell.x() > width - BORDER_MARGIN
                    || cell.y() < BORDER_MARGIN
                    || cell.y() > height - BORDER_MARGIN) {
                throw new IllegalArgumentException(
                    "Cell outside world bounds."
                );
            }

            for (int j = 0; j < i; j++) {
                CellGene other = cells.get(j);

                if (Math.hypot(
                        cell.x() - other.x(),
                        cell.y() - other.y()) < MIN_DISTANCE) {
                    throw new IllegalArgumentException(
                        "Cells are too close."
                    );
                }
            }
        }

        if (extractors == 0 || turrets == 0) {
            throw new IllegalArgumentException(
                "An extractor and turret are required."
            );
        }
    }

    static boolean[] reachable(int count, List<EdgeGene> edges) {
        boolean[] visited = new boolean[count];
        ArrayDeque<Integer> pending = new ArrayDeque<>();

        visited[0] = true;
        pending.add(0);

        while (!pending.isEmpty()) {
            int current = pending.removeFirst();

            for (EdgeGene edge : edges) {
                int next = edge.a() == current
                    ? edge.b()
                    : edge.b() == current ? edge.a() : -1;

                if (next >= 0 && !visited[next]) {
                    visited[next] = true;
                    pending.addLast(next);
                }
            }
        }

        return visited;
    }

    public double width() {
        return width;
    }

    public double height() {
        return height;
    }

    public List<CellGene> cells() {
        return cells;
    }

    public List<EdgeGene> edges() {
        return edges;
    }
}