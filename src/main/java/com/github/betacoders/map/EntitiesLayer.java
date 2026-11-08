package com.github.betacoders.map;

import com.github.betacoders.map.tiles.Entities;

/**
 * Entities
 */
public class EntitiesLayer extends Layer<Entities> {
  public EntitiesLayer(int width, int height) {
    super(width, height, Entities.class);
  }
}
