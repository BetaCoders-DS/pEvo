package com.github.betacoders.world;

import com.github.betacoders.collections.GridInt;

/**
 * BackLayer
 */
public abstract class Layer<E extends Enum<E>> {
  protected final E[] values;

  protected GridInt idGrid;
  protected int width;
  protected int height;

  public Layer(int width, int height, Class<E> enumClass) {
    this.width = width;
    this.height = height;
    this.idGrid = new GridInt(width, height);
    this.values = enumClass.getEnumConstants();
  }

  public E get(int x, int y) {
    return values[idGrid.get(x, y)];
  }

  public void set(int x, int y, E v) {
    idGrid.set(x, y, v.ordinal());
  }

  public void fill(E v) {
    for (int x = 0; x < width; ++x)
      for (int y = 0; y < height; ++y)
        idGrid.set(x, y, v.ordinal());
  }

  public int width() {
    return width;
  }

  public int height() {
    return height;
  }

}
