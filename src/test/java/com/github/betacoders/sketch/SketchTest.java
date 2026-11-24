package com.github.betacoders.sketch;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Font;

import org.junit.jupiter.api.Test;

import com.github.betacoders.Demo.Renderer;
import com.github.betacoders.EvoEngine;
import com.github.betacoders.collections.AdjacencyGraph;
import com.github.betacoders.evolution.EvolutionConfig;
import com.github.betacoders.sketch.PopulationView.Panel;
import com.github.betacoders.world.Base;
import com.github.betacoders.world.Cells;
import com.github.betacoders.world.Link;
import com.github.betacoders.world.World;
import com.github.betacoders.world.enemies.FlyingEnemy;

import processing.awt.PGraphicsJava2D;
import processing.core.PApplet;
import processing.core.PFont;

class SketchTest {
    @Test
    void drawingAndChangingFocusKeepsEveryIndividualAdvancingOncePerFrame() {
        EvoEngine engine = new EvoEngine(EvolutionConfig.standard());
        engine.restart(2026);
        Sketch sketch = new Sketch(engine);
        PGraphicsJava2D graphics = graphicsFor(sketch, 1280, 720);
        try {
            sketch.draw();
            assertTicks(engine, 1);

            Panel selected = new PopulationView(20, 1000, 700).panels(1280, 720)[2];
            sketch.mouseButton = PApplet.LEFT;
            sketch.mouseX = (int) (selected.x() + selected.width() / 2);
            sketch.mouseY = (int) (selected.y() + selected.height() / 2);
            sketch.mousePressed();
            sketch.draw();
            assertTicks(engine, 2);

            sketch.key = PApplet.ESC;
            sketch.keyPressed();
            assertEquals(0, sketch.key, "Esc must return to the grid instead of closing the app.");
            sketch.draw();
            assertTicks(engine, 3);

            sketch.mousePressed();
            sketch.mouseX = sketch.width - 60;
            sketch.mouseY = 30;
            sketch.mousePressed();
            sketch.draw();
            assertTicks(engine, 4);
        } finally {
            graphics.endDraw();
            graphics.dispose();
        }
    }

    @Test
    void rendererClipsTheWorldToItsPanelAndRestoresDrawingCoordinates() {
        PApplet applet = new PApplet();
        PGraphicsJava2D graphics = graphicsFor(applet, 420, 240);
        try {
            graphics.background(17, 19, 23);
            int background = graphics.get(0, 0);
            AdjacencyGraph<Cells, Link> graph = new AdjacencyGraph<>();
            graph.addVertex(new Cells.Core(0, 500, 350));
            World world = new World(new Base(graph));
            world.addEnemy(new FlyingEnemy(1, 2000, 350));

            // O uso de sprites altera imageMode; o recorte deve continuar em coordenadas do painel.
            graphics.imageMode(PApplet.CENTER);
            new Renderer(applet, 1000, 700).draw(world, 50, 60, 200, 100);
            graphics.loadPixels();
            for (int y = 0; y < graphics.height; y++) {
                for (int x = 0; x < graphics.width; x++) {
                    if (x < 50 || x >= 250 || y < 60 || y >= 160) {
                        assertEquals(background, graphics.pixels[y * graphics.width + x],
                            "A world must not draw over another panel.");
                    }
                }
            }

            graphics.noStroke();
            graphics.fill(255, 0, 0);
            graphics.rect(10, 10, 5, 5);
            assertEquals(0xffff0000, graphics.get(12, 12),
                "Panel clipping and transforms must not affect later drawing.");
        } finally {
            graphics.endDraw();
            graphics.dispose();
        }
    }

    private static void assertTicks(EvoEngine engine, int expected) {
        for (int i = 0; i < engine.populationSize(); i++) {
            assertEquals(expected, engine.worldAt(i).ticks());
        }
    }

    private static PGraphicsJava2D graphicsFor(PApplet applet, int width, int height) {
        PGraphicsJava2D graphics = new PGraphicsJava2D();
        applet.width = width;
        applet.height = height;
        applet.g = graphics;
        graphics.setParent(applet);
        graphics.setSize(width, height);
        graphics.beginDraw();
        graphics.textFont(new PFont(new Font("SansSerif", Font.PLAIN, 14), true));
        return graphics;
    }
}
