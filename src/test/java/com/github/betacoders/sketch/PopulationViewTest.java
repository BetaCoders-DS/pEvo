package com.github.betacoders.sketch;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.github.betacoders.sketch.PopulationView.Panel;

class PopulationViewTest {
    @Test
    void laysOutTheWholePopulationWithoutOverlapsAtDifferentWindowSizes() {
        for (int count : new int[] { 4, 7, 20 }) {
            PopulationView view = new PopulationView(count, 1000, 700);
            for (int[] size : new int[][] { { 1280, 720 }, { 800, 600 }, { 600, 900 } }) {
                Panel[] panels = view.panels(size[0], size[1]);
                assertEquals(count, panels.length);
                for (int i = 0; i < panels.length; i++) {
                    Panel panel = panels[i];
                    assertEquals(i, panel.individualIndex());
                    assertTrue(panel.x() >= PopulationView.MARGIN);
                    assertTrue(panel.y() >= PopulationView.HEADER_HEIGHT);
                    assertTrue(panel.x() + panel.width() <= size[0] - PopulationView.MARGIN + 0.01f);
                    assertTrue(panel.y() + panel.height() <= size[1] - PopulationView.MARGIN + 0.01f);
                    assertTrue(panel.width() > 0);
                    assertTrue(panel.height() > PopulationView.PANEL_HEADER_HEIGHT);
                    for (int j = 0; j < i; j++) {
                        Panel other = panels[j];
                        boolean overlaps = panel.x() < other.x() + other.width()
                            && panel.x() + panel.width() > other.x()
                            && panel.y() < other.y() + other.height()
                            && panel.y() + panel.height() > other.y();
                        assertFalse(overlaps);
                    }
                }
            }
        }
    }

    @Test
    void eachPanelExpandsTheIndividualThatWasClicked() {
        PopulationView view = new PopulationView(20, 1000, 700);
        for (Panel panel : view.panels(1280, 720)) {
            assertTrue(view.focusAt(panel.x() + panel.width() / 2,
                panel.y() + panel.height() / 2, 1280, 720));
            assertTrue(view.isFocused());
            assertEquals(panel.individualIndex(), view.focusedIndex());
            Panel[] focused = view.panels(1280, 720);
            assertEquals(1, focused.length);
            assertEquals(panel.individualIndex(), focused[0].individualIndex());
            assertTrue(focused[0].width() > panel.width());
            assertTrue(focused[0].height() > panel.height());

            view.showAll();
            assertFalse(view.isFocused());
            assertEquals(20, view.panels(1280, 720).length);
        }
    }

    @Test
    void ignoresClicksOnTheHeaderGapsAndOutsideTheWindow() {
        PopulationView view = new PopulationView(20, 1000, 700);
        Panel first = view.panels(1280, 720)[0];
        assertFalse(view.focusAt(40, 20, 1280, 720));
        assertFalse(view.focusAt(-1, -1, 1280, 720));
        assertFalse(view.focusAt(1280, 720, 1280, 720));
        assertFalse(view.focusAt(first.x() + first.width() + 1,
            first.y() + first.height() / 2, 1280, 720));
        assertFalse(view.isFocused());
    }

    @Test
    void resizingKeepsTheFocusedIndividual() {
        PopulationView view = new PopulationView(7, 1000, 700);
        Panel selected = view.panels(1280, 720)[6];
        view.focusAt(selected.x() + 1, selected.y() + 1, 1280, 720);
        Panel resized = view.panels(800, 600)[0];
        assertEquals(6, resized.individualIndex());
        assertEquals(800 - 2 * PopulationView.MARGIN, resized.width());
        assertEquals(600 - PopulationView.HEADER_HEIGHT - PopulationView.MARGIN, resized.height());
        assertFalse(view.focusAt(100, 100, 800, 600));
        assertEquals(6, view.focusedIndex());
    }

    @Test
    void aWindowTooSmallToDrawDoesNotProduceInvalidPanels() {
        PopulationView view = new PopulationView(20, 1000, 700);
        assertEquals(0, view.panels(20, 20).length);
        assertFalse(view.focusAt(10, 10, 20, 20));
    }
}
