package com.github.betacoders.collections;

public final class Vec2D {
  public double x;
  public double y;

  public Vec2D() {
    this.x = 0;
    this.y = 0;
  }

  public Vec2D(double x, double y) {
    this.x = x;
    this.y = y;
  }

  public void add(Vec2D other) {
    this.x += other.x;
    this.y += other.y;
  }

  public void subtract(Vec2D other) {
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

  public Vec2D copy() {
    return new Vec2D(x, y);
  }

  public static double dist(Vec2D from, Vec2D to) {
    return Math.hypot((to.x - from.x), (to.y - from.y));
  }

  public static double distSq(Vec2D from, Vec2D to) {
    return (to.x - from.x) * (to.x - from.x) +
        (to.y - from.y) * (to.y - from.y);
  }
}
