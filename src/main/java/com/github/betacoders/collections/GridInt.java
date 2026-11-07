package com.github.betacoders.collections;

/**
 * GridInt
 */
public class GridInt {
  protected final int sizeX;
  protected final int sizeY;
  protected int[] data;

  public GridInt(int width, int height) {
    this.sizeX = width;
    this.sizeY = height;
    this.data = new int[sizeX * sizeY];
  }

  public int get(int x, int y) {
    return this.data[toIndex(x, y)];
  }

  public void set(int x, int y, int t) {
    this.data[toIndex(x, y)] = t;
  }

  public int getWidth() {
    return sizeX;
  }

  public int getHeight() {
    return sizeY;
  }

  public int toIndex(int x, int y) {
    return sizeX * y + x;
  }
}
