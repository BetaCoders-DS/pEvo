package com.github.betacoders.world;

import com.github.betacoders.world.tiles.Entities;

/**
 * Entities
 */
public class EntitiesLayer extends Layer<Entities> {
  public EntitiesLayer(int width, int height) {
    super(width, height, Entities.class);
  }
}
