package com.github.betacoders.world;

import com.github.betacoders.collections.Graph;
import com.github.betacoders.collections.List;
import com.github.betacoders.collections.Vec2D;

/**
 * Packet
 */
public final class Packet {
  private final List<Cells> path;
  private final double speed; // px/tick
  private int hop;
  private double progress; // 0 - 1
  private Link currentLink;
  private boolean done;

  public Packet(List<Cells> path, double speed, Graph<Cells, Link> graph) {
    this.path = path;
    this.speed = speed;
    this.hop = 0;
    this.progress = 0.;
    this.done = path == null || path.size() < 2;

    if (!this.done) {
      this.currentLink = graph.edge(path.get(0), path.get(1));

      if (this.currentLink != null)
        this.currentLink.enter();
      else
        this.done = true;
    }
  }

  public boolean done() {
    return done;
  }

  public Cells destination() {
    return path.get(path.size() - 1);
  }

  public Vec2D pos() {
    if (path.size() == 0)
      return new Vec2D(0, 0);

    if (done || hop + 1 >= path.size())
      return path.get(path.size() - 1).pos();

    Vec2D start = path.get(hop).pos();
    Vec2D end = path.get(hop + 1).pos();

    double x = start.x + (end.x - start.x) * progress;
    double y = start.y + (end.y - start.y) * progress;

    return new Vec2D(x, y);
  }

  public boolean tick(Graph<Cells, Link> graph) {
    if (done)
      return false;

    Cells from = path.get(hop);
    Cells to = path.get(hop + 1);

    if (!from.alive() || !to.alive() || currentLink == null) {
      releaseLink();
      done = true;
      return true;
    }

    double len = currentLink.length();
    progress += speed / len;

    while (progress >= 1. && !done) {
      progress -= 1.;
      releaseLink();
      ++hop;

      if (hop + 1 >= path.size()) {
        done = true;
        if (path.get(path.size() - 1) instanceof Cells.Turret turret && turret.alive())
          turret.receiveAmmo();

        return true;
      }

      Cells nextFrom = path.get(hop);
      Cells nextTo = path.get(hop + 1);

      currentLink = graph.edge(nextFrom, nextTo);
      if (currentLink == null || !nextTo.alive()) {
        done = true;
        return true;
      }
      currentLink.enter();
    }

    return true;
  }

  private void releaseLink() {
    if (currentLink != null) {
      currentLink.leave();
      currentLink = null;
    }
  }
}
