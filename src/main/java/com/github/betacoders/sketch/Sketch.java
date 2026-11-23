package com.github.betacoders.sketch;

import com.github.betacoders.Demo.DemoWorld;
import com.github.betacoders.Demo.Renderer;
import processing.core.PApplet;

/**
 * Janela principal do programa.
 */
public class Sketch extends PApplet {
  private DemoWorld world; // o que acontece (a simulação)
  private Renderer renderer; // como é desenhado

  @Override
  public void settings() {
    size(1000, 700);
  }

  @Override
  public void setup() {
    frameRate(60);
    world = new DemoWorld(1000, 700);
    // O Renderer precisa receber "this" (esta janela) para poder desenhar
    // nela, e o tamanho do mundo para desenhar a grade.
    renderer = new Renderer(this, 1000, 700);
  }

  @Override
  public void draw() {
    // 1) Atualiza a simulação (1 tick).
    world.tick();

    // 2) Limpa a tela e desenha o estado atual por cima.
    background(18, 20, 28);
    renderer.draw(world.base(), world);
  }
}  
