package com.github.betacoders.world;

import com.github.betacoders.world.tiles.StaticTile;

/**
 * StaticLayer
 */
public class StaticLayer extends Layer<StaticTile> {
  public StaticLayer(int width, int height) {
    super(width, height, StaticTile.class);
  }
}
