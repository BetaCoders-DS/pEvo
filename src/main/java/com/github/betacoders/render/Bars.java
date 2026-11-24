package com.github.betacoders.render;

import processing.core.PApplet;

public final class Bars {
  private Bars() {
  }

  public static void health(PApplet g, float x, float y, float width, float height, float fraction) {
    float clamped = Math.max(0f, Math.min(1f, fraction));

    g.rectMode(PApplet.CORNER);
    g.noStroke();
    g.fill(Theme.BAR_BORDER);
    g.rect(x - 1, y - 1, width + 2, height + 2, 2);
    g.fill(clamped < 0.3f ? Theme.BAR_LOW : Theme.BAR_HIGH);
    g.rect(x, y, width * clamped, height, 1);
  }

  public static void ammo(PApplet g, float x, float y, int stored, int capacity) {
    g.stroke(Theme.AMMO_STROKE);
    g.strokeWeight(1);
    for (int i = 0; i < capacity; ++i) {
      g.fill(i < stored ? Theme.AMMO_FULL : Theme.AMMO_EMPTY);
      g.rect(x - capacity * 4 + i * 8, y, 6, 5, 1);
    }
  }
}
