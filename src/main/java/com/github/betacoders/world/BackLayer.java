package com.github.betacoders.world;

import com.github.betacoders.world.tiles.BackTile;

/**
 * BackLayer
 */
public class BackLayer extends Layer<BackTile> {
  public BackLayer(int width, int height) {
    super(width, height, BackTile.class);
  }
}
