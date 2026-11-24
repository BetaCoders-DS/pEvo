package com.github.betacoders.sketch;

import java.util.Objects;

import com.github.betacoders.Demo.BloonsSprites;
import com.github.betacoders.Demo.BloonsSprites.Sprite;
import com.github.betacoders.Demo.Renderer;
import com.github.betacoders.EvoEngine;
import com.github.betacoders.sketch.PopulationView.Panel;
import com.github.betacoders.world.World;

import processing.core.PApplet;
import processing.core.PImage;

public class Sketch extends PApplet {
    private static final int BACK_WIDTH = 166;
    private static final int BACK_HEIGHT = 32;
    private static final int BACK_TOP = 20;

    private final EvoEngine engine;
    private final PopulationView view;
    private final Renderer renderer;
    private long lastFrameMillis;

    public Sketch(EvoEngine engine) {
        this.engine = Objects.requireNonNull(engine);
        view = new PopulationView(
            engine.populationSize(), engine.config().width(), engine.config().height());
        renderer = new Renderer(this, engine.config().width(), engine.config().height());
    }

    @Override
    public void settings() {
        size(1280, 720);
    }

    @Override
    public void setup() {
        frameRate(60);
        lastFrameMillis = millis();
        surface.setTitle("pEvo | Bloons — evolução de bases");
        surface.setResizable(true);
        textFont(createFont("SansSerif", 14, true));
    }

    @Override
    public void draw() {
        engine.step();

        long now = millis();
        engine.advance(now - lastFrameMillis);
        lastFrameMillis = now;

        background(13, 36, 56);
        drawHeader();

        for (Panel panel : view.panels(width, height)) {
            drawIndividual(panel, engine.worldAt(panel.individualIndex()));
        }
    }

    private void drawHeader() {
        pushStyle();
        rectMode(CORNER);
        imageMode(CORNER);
        noStroke();
        fill(23, 65, 100);
        rect(0, 0, width, 94);

        fill(255, 218, 83);
        textAlign(LEFT, BASELINE);
        textSize(27);
        text("pEvo", 16, 30);
        fill(240, 248, 255);
        textSize(14);
        text(view.isFocused() ? "BLOONS / BASE " + (view.focusedIndex() + 1) : "BLOONS / POPULAÇÃO", 105, 27);

        fill(188, 217, 236);
        textSize(11);
        String hint = view.isFocused()
            ? "Passe sobre uma torre para ver o alcance. Esc volta à grade."
            : "Todas as bases evoluem juntas. Clique em um quadro para ampliar.";
        float statsRight = view.isFocused() ? backLeft() - 24 : width - 16;
        clip(16, 36, Math.max(1, (width >= 850 ? statsRight - 190 : width - 16) - 16), 22);
        text(hint, 16, 50);
        noClip();

        if (width >= 850) {
            int alive = 0;
            for (int i = 0; i < engine.populationSize(); i++) {
                if (!engine.worldAt(i).over()) {
                    alive++;
                }
            }
            textAlign(RIGHT, BASELINE);
            textSize(14);
            fill(255, 224, 112);
            text("GERAÇÃO " + engine.generation(), statsRight, 27);
            textSize(11);
            fill(188, 237, 192);
            text(alive + " / " + engine.populationSize() + " BASES ATIVAS", statsRight, 48);
            fill(188, 217, 236);
            text(engine.ticksPerSecond() + " tps", statsRight, 68);
        }

        float slot = width >= 850 ? 120 : (width - 32) / 4f;
        drawLegend(Sprite.CORE, "Sede", 16, 76);
        drawLegend(Sprite.TURRET, "Atirador", 16 + slot, 76);
        drawLegend(Sprite.RELAY, "Conector", 16 + slot * 2, 76);
        drawLegend(Sprite.EXTRACTOR, "Extrator", 16 + slot * 3, 76);
        if (width >= 850) {
            drawLegend(Sprite.RED, "Bloons", 16 + slot * 4, 76);
            drawLegend(Sprite.MOAB, "MOAB · a cada 5 ondas", 16 + slot * 5, 76);
        }
        noStroke();
        fill(237, 185, 64);
        rect(16, 93, Math.max(0, width - 32), 2);

        if (view.isFocused()) {
            boolean hovered = overBackButton(mouseX, mouseY);
            stroke(255, 222, 111);
            strokeWeight(1);
            fill(hovered ? color(255, 213, 78) : color(40, 95, 136));
            rect(backLeft(), BACK_TOP, BACK_WIDTH, BACK_HEIGHT, 6);
            fill(hovered ? color(45, 57, 38) : color(255, 245, 202));
            textAlign(CENTER, CENTER);
            textSize(12);
            text("Voltar à grade  [Esc]", backLeft() + BACK_WIDTH / 2f, BACK_TOP + BACK_HEIGHT / 2f);
        }
        popStyle();
    }

    private void drawLegend(Sprite sprite, String label, float x, float y) {
        PImage image = BloonsSprites.image(sprite);
        float factor = 23f / Math.max(image.width, image.height);
        imageMode(CENTER);
        image(image, x + 11, y - 1, image.width * factor, image.height * factor);
        fill(222, 239, 248);
        textAlign(LEFT, CENTER);
        textSize(width >= 850 ? 11 : 10);
        text(label, x + 29, y);
    }

    private void drawIndividual(Panel panel, World world) {
        boolean hovered = !view.isFocused() && panel.contains(mouseX, mouseY);
        pushStyle();
        rectMode(CORNER);
        strokeWeight(hovered ? 2 : 1);
        stroke(hovered ? color(255, 219, 83)
            : world.over() ? color(142, 112, 100) : color(73, 131, 166));
        fill(world.over() ? color(60, 73, 81) : color(29, 78, 115));
        rect(panel.x(), panel.y(), panel.width(), panel.height(), 6);

        float inset = PopulationView.PANEL_INSET;
        renderer.draw(world, panel.x() + inset,
            panel.y() + PopulationView.PANEL_HEADER_HEIGHT,
            panel.width() - 2 * inset,
            panel.height() - PopulationView.PANEL_HEADER_HEIGHT
                - PopulationView.PANEL_FOOTER_HEIGHT - inset,
            view.isFocused());

        textAlign(LEFT, CENTER);
        textSize(11);
        fill(255, 245, 204);
        text("BASE " + (panel.individualIndex() + 1), panel.x() + 10,
            panel.y() + PopulationView.PANEL_HEADER_HEIGHT / 2f);
        textAlign(RIGHT, CENTER);
        textSize(10);
        fill(world.over() ? color(255, 188, 165) : color(204, 246, 196));
        if (panel.width() >= 120) {
            String status = panel.width() < 180
                ? "O" + world.wave() + " · " + (world.over() ? "FIM" : "ATIVA")
                : "ONDA " + world.wave() + " · " + (world.over() ? "DERROTADA" : "ATIVA");
            text(status, panel.x() + panel.width() - 10,
                panel.y() + PopulationView.PANEL_HEADER_HEIGHT / 2f);
        }
        drawCoreHealth(panel, world);
        popStyle();
    }

    private void drawCoreHealth(Panel panel, World world) {
        float health = Math.max(0, Math.min(1,
            world.base().core().hp() / world.base().core().hpMax()));
        float top = panel.y() + panel.height() - PopulationView.PANEL_FOOTER_HEIGHT;
        float barWidth = Math.max(8, panel.width() - 108);
        noStroke();
        fill(18, 50, 75);
        rect(panel.x() + 3, top, panel.width() - 6, PopulationView.PANEL_FOOTER_HEIGHT - 3, 2);
        textSize(8);
        textAlign(LEFT, CENTER);
        fill(202, 224, 234);
        text("SEDE", panel.x() + 10, top + 6);
        fill(10, 31, 45);
        rect(panel.x() + 55, top + 3, barWidth, 6, 2);
        fill(health < 0.3f ? color(250, 121, 85) : color(176, 231, 92));
        rect(panel.x() + 55, top + 3, barWidth * health, 6, 2);
        textAlign(RIGHT, CENTER);
        fill(242, 247, 218);
        text(Math.round(health * 100) + "%", panel.x() + panel.width() - 10, top + 6);
    }

    @Override
    public void mousePressed() {
        if (mouseButton != LEFT) {
            return;
        }

        if (view.isFocused() && overBackButton(mouseX, mouseY)) {
            view.showAll();
        } else {
            view.focusAt(mouseX, mouseY, width, height);
        }
    }

    @Override
    public void keyPressed() {
        if (key == ESC) {
            key = 0;
            view.showAll();
        } else if (keyCode == SHIFT) {
            engine.increaseSpeed();
            frameRate(Math.min(60, engine.ticksPerSecond()));
        } else if (keyCode == CONTROL) {
            engine.decreaseSpeed();
            frameRate(Math.min(60, engine.ticksPerSecond()));
        }
    }

    private float backLeft() {
        return width - BACK_WIDTH - PopulationView.MARGIN;
    }

    private boolean overBackButton(float x, float y) {
        return x >= backLeft() && x < backLeft() + BACK_WIDTH
            && y >= BACK_TOP && y < BACK_TOP + BACK_HEIGHT;
    }
}
