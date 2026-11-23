package com.github.betacoders.sketch;

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

  @Override
  public void settings() {
    super.settings();
    // Tamanho da janela em pixels (largura, altura).
    size(1000, 700);
  }

  @Override
  public void setup() {
    super.setup();
    // Quantos fps o draw() deve rodar.
    frameRate(60);
  }

  @Override
  public void draw() {
     super.draw();
    // Pinta o fundo inteiro (R, G, B). A cada frame para apagar o desenho do frame anterior.
    background(18, 20, 28);
  }
}
