package com.github.betacoders.sketch;

import com.github.betacoders.Demo.DemoWorld;

import processing.core.PApplet;

/**
 * Janela principal do programa.
 */
public class Sketch extends PApplet {
  private DemoWorld world;

  @Override
  public void settings() {
    size(1000, 700);
  }

  @Override
  public void setup() {
    frameRate(60);
    world = new DemoWorld(1000, 700);
  }

  @Override
  public void draw() {
    // Avança a simulação 1 tick (um frame = um tick).
      world.tick();

    // Teste: uma vez por segundo, mostra a vida da base, quantos inimigos
    // morreram e quantos estão em campo. "mortos" deve subir com o tempo
    // e a vida da base deve cair devagar.
    if (frameCount % 60 == 0)
      System.out.println("Vida da base: " + world.base().core().hp()
          + " | mortos: " + world.kills()
          + " | inimigos: " + world.enemies().size());

    background(18, 20, 28);
  }
}