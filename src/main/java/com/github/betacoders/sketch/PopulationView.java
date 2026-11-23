package com.github.betacoders.sketch;

/** Calcula os painéis e guarda o foco sem depender do Processing ou dos mundos. */
public final class PopulationView {
    public static final int MARGIN = 16;
    public static final int HEADER_HEIGHT = 68;
    public static final int PANEL_HEADER_HEIGHT = 30;
    public static final int PANEL_INSET = 3;
    private static final int GAP = 10;

    public record Panel(int individualIndex, float x, float y, float width, float height) {
        public boolean contains(float px, float py) {
            return px >= x && px < x + width && py >= y && py < y + height;
        }
    }

    private final int populationSize;
    private final double worldWidth;
    private final double worldHeight;
    private int focusedIndex = -1;

    public PopulationView(int populationSize, double worldWidth, double worldHeight) {
        if (populationSize < 1 || !Double.isFinite(worldWidth) || worldWidth <= 0
                || !Double.isFinite(worldHeight) || worldHeight <= 0) {
            throw new IllegalArgumentException("Dimensões e população devem ser positivas.");
        }
        this.populationSize = populationSize;
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
    }

    public Panel[] panels(int width, int height) {
        float availableWidth = width - 2f * MARGIN;
        float availableHeight = height - HEADER_HEIGHT - MARGIN;
        if (availableWidth <= 2 * PANEL_INSET
                || availableHeight <= PANEL_HEADER_HEIGHT + PANEL_INSET) {
            return new Panel[0];
        }

        if (isFocused()) {
            return new Panel[] {
                new Panel(focusedIndex, MARGIN, HEADER_HEIGHT, availableWidth, availableHeight)
            };
        }

        // Escolhe a grade que dá a maior escala aos mundos, já descontando os títulos.
        int columns = 1;
        double bestScale = 0;
        for (int candidate = 1; candidate <= populationSize; candidate++) {
            int rows = (populationSize + candidate - 1) / candidate;
            float panelWidth = (availableWidth - (candidate - 1) * GAP) / candidate;
            float panelHeight = (availableHeight - (rows - 1) * GAP) / rows;
            double scale = Math.min((panelWidth - 2 * PANEL_INSET) / worldWidth,
                (panelHeight - PANEL_HEADER_HEIGHT - PANEL_INSET) / worldHeight);
            if (scale > bestScale) {
                bestScale = scale;
                columns = candidate;
            }
        }
        if (bestScale <= 0) {
            return new Panel[0];
        }

        int rows = (populationSize + columns - 1) / columns;
        float panelWidth = (availableWidth - (columns - 1) * GAP) / columns;
        float panelHeight = (availableHeight - (rows - 1) * GAP) / rows;
        Panel[] panels = new Panel[populationSize];
        for (int i = 0; i < panels.length; i++) {
            panels[i] = new Panel(i,
                MARGIN + (i % columns) * (panelWidth + GAP),
                HEADER_HEIGHT + (i / columns) * (panelHeight + GAP),
                panelWidth, panelHeight);
        }
        return panels;
    }

    public boolean focusAt(float x, float y, int width, int height) {
        if (isFocused()) {
            return false;
        }
        for (Panel panel : panels(width, height)) {
            if (panel.contains(x, y)) {
                focusedIndex = panel.individualIndex();
                return true;
            }
        }
        return false;
    }

    public boolean isFocused() {
        return focusedIndex >= 0;
    }

    public int focusedIndex() {
        return focusedIndex;
    }

    public void showAll() {
        focusedIndex = -1;
    }
}
