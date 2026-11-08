package com.github.betacoders.map;

import com.github.betacoders.map.tiles.BackTile;

/**
 * BackLayer
 */
public class BackLayer extends Layer<BackTile> {
  public BackLayer(int width, int height) {
    super(width, height, BackTile.class);
  }
}
