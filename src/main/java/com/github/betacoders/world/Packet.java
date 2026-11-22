package com.github.betacoders.world;

import com.github.betacoders.collections.Graph;
import com.github.betacoders.collections.List;
import com.github.betacoders.collections.Vector2D;

/**
 * Packet
 */
public final class Packet {
  private final List<Cells> path;
  private final double speed; // px/s
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
      this.currentLink = graph.getEdge(path.get(0), path.get(1));

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

  public Vector2D pos() {
    if (path.size() == 0)
      return new Vector2D(0, 0);

    if (done || hop + 1 >= path.size())
      return path.get(path.size() - 1).pos();

    Vector2D start = path.get(hop).pos();
    Vector2D end = path.get(hop + 1).pos();

    double x = start.x + (end.x - start.x) * progress;
    double y = start.y + (end.y - start.y) * progress;

    return new Vector2D(x, y);
  }

  public void step(Graph<Cells, Link> graph, double dt) {
    if (done)
      return;

    Cells from = path.get(hop);
    Cells to = path.get(hop + 1);

    if (!from.alive() || !to.alive() || currentLink == null) {
      releaseLink();
      done = true;
      return;
    }

    double len = currentLink.length();
    progress += (speed / len) * dt;

    while (progress >= 1.0 && !done) {
      progress = 0.;
      releaseLink();
      ++hop;

      if (hop + 1 >= path.size()) {
        done = true;
        if (to instanceof Cells.Turret turret && turret.alive())
          turret.receiveAmmo();

        return;
      }

      Cells nextFrom = path.get(hop);
      Cells nextTo = path.get(hop + 1);

      currentLink = graph.getEdge(nextFrom, nextTo);
      if (currentLink == null || !nextTo.alive()) {
        done = true;
        return;
      }
      currentLink.enter();
    }
  }

  private void releaseLink() {
    if (currentLink != null) {
      currentLink.leave();
      currentLink = null;
    }
  }
}
