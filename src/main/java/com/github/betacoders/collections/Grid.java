package com.github.betacoders.collections;

/**
 * Grid
 */
public class Grid<T> {
  protected final int sizeX;
  protected final int sizeY;
  protected T[] data;

  public Grid(int width, int height) {
    this.sizeX = width;
    this.sizeY = height;
    this.data = (T[]) new Object[sizeX * sizeY];
  }

  public T get(int x, int y) {
    return this.data[toIndex(x, y)];
  }

  public void set(int x, int y, T t) {
    this.data[toIndex(x, y)] = t;
  }

  public int width() {
    return sizeX;
  }

  public int height() {
    return sizeY;
  }

  public int toIndex(int x, int y) {
    return sizeX * y + x;
  }
}
