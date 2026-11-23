package com.github.betacoders.world;

import com.github.betacoders.collections.Vec2D;

public final class Link {
  private final Cells a;
  private final Cells b;
  private final double genomeLength;
  private int load;

  private static final double LOAD_COST_MULT = 10.;

  public Link(Cells a, Cells b) {
    this.a = a;
    this.b = b;
    this.genomeLength = Vec2D.dist(a.pos(), b.pos());
    this.load = 0;
  }

  public Cells a() {
    return a;
  }

  public Cells b() {
    return b;
  }

  public double genomeLength() {
    return genomeLength;
  }

  public double length() {
    return Vec2D.dist(a.pos(), b.pos());
  }

  public int cost() {
    return Math.max(1, (int) Math.round(length() + load * LOAD_COST_MULT));
  }

  public int load() {
    return load;
  }

  public void enter() {
    ++load;
  }

  public void leave() {
    if (load > 0)
      --load;
  }
}
