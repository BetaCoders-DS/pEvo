package com.github.betacoders.map;

import com.github.betacoders.map.tiles.StaticTile;

/**
 * StaticLayer
 */
public class StaticLayer extends Layer<StaticTile> {
  public StaticLayer(int width, int height) {
    super(width, height, StaticTile.class);
  }
}
