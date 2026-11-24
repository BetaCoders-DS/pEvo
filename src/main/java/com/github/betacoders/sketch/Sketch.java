package com.github.betacoders.sketch;

import java.util.Objects;

import com.github.betacoders.Demo.Renderer;
import com.github.betacoders.EvoEngine;
import com.github.betacoders.sketch.PopulationView.Panel;
import com.github.betacoders.world.World;

import processing.core.PApplet;

/** Janela da população, com uma grade de indivíduos e ampliação por clique. */
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
    surface.setTitle("pEvo | Evolução de bases");
    surface.setResizable(true);
    textFont(createFont("SansSerif", 14, true));
  }

  @Override
  public void draw() {
    engine.step();

    long now = millis();
    engine.advance(now - lastFrameMillis);
    lastFrameMillis = now;

    background(12, 16, 24);
    drawHeader();

    for (Panel panel : view.panels(width, height))
      drawIndividual(panel, engine.worldAt(panel.individualIndex()));
  }

  private void drawHeader() {
    fill(232, 238, 248);
    textAlign(LEFT, BASELINE);
    textSize(21);
    text(view.isFocused() ? "pEvo / Base " + (view.focusedIndex() + 1) : "pEvo / População", 16, 28);

    fill(155, 169, 190);
    textSize(12);
    String hint = view.isFocused() ? "Esc para voltar à grade" : "Clique em um indivíduo para ampliar";
    text("Geração " + engine.generation() + "  ·  " + engine.populationSize()
        + " indivíduos  ·  " + engine.ticksPerSecond() + " tps  ·  " + hint, 16, 51);

    if (view.isFocused()) {
      boolean hovered = overBackButton(mouseX, mouseY);
      stroke(hovered ? color(104, 162, 241) : color(64, 85, 114));
      fill(hovered ? color(35, 54, 80) : color(24, 35, 52));
      rect(backLeft(), BACK_TOP, BACK_WIDTH, BACK_HEIGHT, 6);
      fill(224, 234, 248);
      textAlign(CENTER, CENTER);
      textSize(12);
      text("Voltar à grade  [Esc]", backLeft() + BACK_WIDTH / 2f, BACK_TOP + BACK_HEIGHT / 2f);
    }
  }

  private void drawIndividual(Panel panel, World world) {
    boolean hovered = !view.isFocused() && panel.contains(mouseX, mouseY);
    pushStyle();
    rectMode(CORNER);
    strokeWeight(hovered ? 2 : 1);
    stroke(hovered ? color(104, 162, 241)
        : world.over() ? color(109, 61, 74) : color(48, 63, 84));
    fill(18, 24, 35);
    rect(panel.x(), panel.y(), panel.width(), panel.height(), 7);

    float inset = PopulationView.PANEL_INSET;
    renderer.draw(world, panel.x() + inset,
        panel.y() + PopulationView.PANEL_HEADER_HEIGHT,
        panel.width() - 2 * inset,
        panel.height() - PopulationView.PANEL_HEADER_HEIGHT - inset);

    textAlign(LEFT, CENTER);
    textSize(12);
    fill(227, 235, 247);
    text("Base " + (panel.individualIndex() + 1), panel.x() + 10,
        panel.y() + PopulationView.PANEL_HEADER_HEIGHT / 2f);
    textAlign(RIGHT, CENTER);
    textSize(10);
    fill(world.over() ? color(240, 125, 137) : color(135, 205, 176));
    if (panel.width() >= 120) {
      String status = panel.width() < 180
          ? "O" + world.wave() + " · " + (world.over() ? "Fim" : "Ativa")
          : "Onda " + world.wave() + " · " + (world.over() ? "Derrotada" : "Ativa");
      text(status, panel.x() + panel.width() - 10,
          panel.y() + PopulationView.PANEL_HEADER_HEIGHT / 2f);
    }
    popStyle();
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
      // Impede o comportamento padrão do Processing de fechar a aplicação.
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
