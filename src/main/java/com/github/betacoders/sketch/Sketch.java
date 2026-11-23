package com.github.betacoders.sketch;

import com.github.betacoders.world.DemoWorld;
import processing.core.PApplet;

/**
 * Janela principal do programa.
 *
 * O Processing chama estes três métodos sozinho:
 *   settings() -> uma vez, antes de abrir a janela
 *   setup()    -> uma vez, depois que a janela abriu
 *   draw()     -> repetidamente, a cada frame (60)
 */
public class Sketch extends PApplet {
  // O mundo que será mostrado (por enquanto, o de demonstração).
  private DemoWorld world;

  @Override
  public void settings() {
    super.settings();
    // Tamanho da janela em pixels (largura, altura).
    size(1000, 700);
  }

  @Override
  public void setup() {
    super.setup();
    frameRate(60);
    world = new DemoWorld(1000, 700);
  }

  @Override
  public void draw() {
     super.draw();
    // Avança a simulação 1 tick (um frame = um tick).
    world.tick();

    // Teste: uma vez por segundo (a cada 60 frames) mostra no console
    // quantos pacotes de munição estão viajando. O número deve variar.
    if (frameCount % 60 == 0)
      System.out.println("Pacotes em andamento: " + world.base().packets().size());

    background(18, 20, 28);
  }
}