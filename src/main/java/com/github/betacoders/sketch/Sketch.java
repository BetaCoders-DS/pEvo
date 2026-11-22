package com.github.betacoders.sketch;

import processing.core.PApplet;

/**
 * Sketch
 */
public class Sketch extends PApplet {
  @Override
  public void settings() {
    super.settings();
    size(1000, 700);
  }

  @Override
  public void setup() {
    super.setup();
    frameRate(60);
  }

  @Override
  public void draw() {
    super.draw();
    background(18, 20, 28);
  }
}
