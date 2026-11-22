package com.github.betacoders.collections;

public final class Vector2D {
  public double x;
  public double y;

  public Vector2D() {
    this.x = 0;
    this.y = 0;
  }

  public Vector2D(double x, double y) {
    this.x = x;
    this.y = y;
  }

  public void add(Vector2D other) {
    this.x += other.x;
    this.y += other.y;
  }

  public void subtract(Vector2D other) {
    this.x -= other.x;
    this.y -= other.y;
  }

  public void multiply(double scalar) {
    this.x *= scalar;
    this.y *= scalar;
  }

  public void divide(double scalar) {
    if (scalar != 0) {
      this.x /= scalar;
      this.y /= scalar;
    }
  }

  public Vector2D copy() {
    return new Vector2D(x, y);
  }

  public static double dist(Vector2D from, Vector2D to) {
    return Math.hypot((to.x - from.x), (to.y - from.y));
  }

  public static double distSq(Vector2D from, Vector2D to) {
    return (to.x - from.x) * (to.x - from.x) +
        (to.y - from.y) * (to.y - from.y);
  }
}
