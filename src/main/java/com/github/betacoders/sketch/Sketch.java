package com.github.betacoders.sketch;

import java.util.Objects;

import com.github.betacoders.EvoEngine;
import com.github.betacoders.render.Bars;
import com.github.betacoders.render.BloonsSprites;
import com.github.betacoders.render.BloonsSprites.Sprite;
import com.github.betacoders.render.Renderer;
import com.github.betacoders.render.Theme;
import com.github.betacoders.sketch.PopulationView.Panel;
import com.github.betacoders.world.World;

import processing.core.PApplet;
import processing.core.PImage;

public class Sketch extends PApplet {
    private static final int BACK_WIDTH = 166;
    private static final int BACK_HEIGHT = 32;
    private static final int BACK_TOP = 20;

    private static final float TITLE_TEXT = 27;
    private static final float HEADER_TEXT = 14;
    private static final float HINT_TEXT = 11;
    private static final float STATS_TEXT = 14;
    private static final float LEGEND_TEXT = 11;
    private static final float LEGEND_TEXT_SMALL = 10;
    private static final float PANEL_TITLE_TEXT = 11;
    private static final float PANEL_WAVE_TEXT = 10;
    private static final float HEALTH_TEXT = 8;
    private static final float BACK_TEXT = 12;
    private static final float LEGEND_ICON_SIZE = 23;

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
        surface.setTitle("pEvo | evolução de bases");
        surface.setResizable(true);
        textFont(createFont("SansSerif", 14, true));
    }

    @Override
    public void draw() {
        engine.step();

        long now = millis();
        engine.advance(now - lastFrameMillis);
        lastFrameMillis = now;

        background(Theme.WINDOW_BG);
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
        fill(Theme.HEADER_BG);
        rect(0, 0, width, 94);

        fill(Theme.TITLE);
        textAlign(LEFT, BASELINE);
        textSize(TITLE_TEXT);
        text("pEvo", 16, 30);
        fill(Theme.TEXT);
        textSize(HEADER_TEXT);
        text(view.isFocused() ? "BASE " + (view.focusedIndex() + 1) : "POPULAÇÃO", 105, 27);

        fill(Theme.MUTED);
        textSize(HINT_TEXT);
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
            textSize(STATS_TEXT);
            fill(Theme.ACCENT);
            text("GERAÇÃO " + engine.generation(), statsRight, 27);
            textSize(HINT_TEXT);
            fill(Theme.ALIVE);
            text(alive + " / " + engine.populationSize() + " BASES ATIVAS", statsRight, 48);
            fill(Theme.MUTED);
            text(engine.ticksPerSecond() + " tps", statsRight, 68);
        }

        float slot = width >= 850 ? 120 : (width - 32) / 4f;
        drawLegend(Sprite.CORE, "Sede", 16, 76);
        drawLegend(Sprite.TURRET, "Atirador", 16 + slot, 76);
        drawLegend(Sprite.RELAY, "Conector", 16 + slot * 2, 76);
        drawLegend(Sprite.EXTRACTOR, "Extrator", 16 + slot * 3, 76);
        if (width >= 850) {
            drawLegend(Sprite.GROUND, "Terrestre", 16 + slot * 4, 76);
            drawLegend(Sprite.AEREO, "Aéreo", 16 + slot * 5, 76);
            drawLegend(Sprite.MAHORAGA, "Colossal", 16 + slot * 6, 76);
        }
        noStroke();
        fill(Theme.HEADER_RULE);
        rect(16, 93, Math.max(0, width - 32), 2);

        if (view.isFocused()) {
            boolean hovered = overBackButton(mouseX, mouseY);
            stroke(hovered ? Theme.BUTTON_STROKE_HOVER : Theme.BUTTON_STROKE);
            strokeWeight(1);
            fill(hovered ? Theme.BUTTON_FILL_HOVER : Theme.BUTTON_FILL);
            rect(backLeft(), BACK_TOP, BACK_WIDTH, BACK_HEIGHT, 6);
            fill(hovered ? Theme.BUTTON_TEXT_HOVER : Theme.BUTTON_TEXT);
            textAlign(CENTER, CENTER);
            textSize(BACK_TEXT);
            text("Voltar à grade  [Esc]", backLeft() + BACK_WIDTH / 2f, BACK_TOP + BACK_HEIGHT / 2f);
        }
        popStyle();
    }

    private void drawLegend(Sprite sprite, String label, float x, float y) {
        PImage image = BloonsSprites.image(sprite);
        float factor = LEGEND_ICON_SIZE / Math.max(image.width, image.height);
        imageMode(CENTER);
        image(image, x + 11, y - 1, image.width * factor, image.height * factor);
        fill(Theme.LEGEND_TEXT);
        textAlign(LEFT, CENTER);
        textSize(width >= 850 ? LEGEND_TEXT : LEGEND_TEXT_SMALL);
        text(label, x + 29, y);
    }

    private void drawIndividual(Panel panel, World world) {
        boolean hovered = !view.isFocused() && panel.contains(mouseX, mouseY);
        pushStyle();
        rectMode(CORNER);
        strokeWeight(hovered ? 2 : 1);
        stroke(hovered ? Theme.PANEL_STROKE_HOVER
            : world.over() ? Theme.PANEL_STROKE_OVER : Theme.PANEL_STROKE);
        fill(world.over() ? Theme.PANEL_FILL_OVER : Theme.PANEL_FILL);
        rect(panel.x(), panel.y(), panel.width(), panel.height(), 6);

        float inset = PopulationView.PANEL_INSET;
        renderer.draw(world, panel.x() + inset,
            panel.y() + PopulationView.PANEL_HEADER_HEIGHT,
            panel.width() - 2 * inset,
            panel.height() - PopulationView.PANEL_HEADER_HEIGHT
                - PopulationView.PANEL_FOOTER_HEIGHT - inset,
            view.isFocused());

        textAlign(LEFT, CENTER);
        textSize(PANEL_TITLE_TEXT);
        fill(Theme.PANEL_TEXT);
        text("BASE " + (panel.individualIndex() + 1), panel.x() + 10,
            panel.y() + PopulationView.PANEL_HEADER_HEIGHT / 2f);
        textAlign(RIGHT, CENTER);
        textSize(PANEL_WAVE_TEXT);
        fill(world.over() ? Theme.WAVE_TEXT_OVER : Theme.WAVE_TEXT);
        String wave = panel.width() < 180
            ? "O" + world.wave()
            : "ONDA " + world.wave();
        text(wave, panel.x() + panel.width() - 10,
            panel.y() + PopulationView.PANEL_HEADER_HEIGHT / 2f);
        drawCoreHealth(panel, world);
        popStyle();
    }

    private void drawCoreHealth(Panel panel, World world) {
        float health = Math.max(0, Math.min(1,
            world.base().core().hp() / world.base().core().hpMax()));
        float top = panel.y() + panel.height() - PopulationView.PANEL_FOOTER_HEIGHT;
        float barWidth = Math.max(8, panel.width() - 108);
        noStroke();
        fill(Theme.HEALTH_BG);
        rect(panel.x() + 3, top, panel.width() - 6, PopulationView.PANEL_FOOTER_HEIGHT - 3, 2);
        textSize(HEALTH_TEXT);
        textAlign(LEFT, CENTER);
        fill(Theme.HEALTH_LABEL);
        text("SEDE", panel.x() + 10, top + 6);
        Bars.health(this, panel.x() + 55, top + 3, barWidth, 6, health);
        textAlign(RIGHT, CENTER);
        fill(Theme.HEALTH_PERCENT);
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
