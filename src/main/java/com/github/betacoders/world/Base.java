package com.github.betacoders.world;

import java.util.Objects;

import com.github.betacoders.algo.AStarPathfinder;
import com.github.betacoders.collections.Graph;
import com.github.betacoders.collections.List;
import com.github.betacoders.collections.Vec2D;
import com.github.betacoders.collections.Vector;

/**
 * Base
 */
public class Base {
  private static final double PACKET_SPEED = 4.;
  private static final double EDGE_REST_LENGTH = 70.;
  private static final double MAX_ATTRACTION_STEP = 1.;

  private final Graph<Cells, Link> graph;
  private Cells.Core core;
  private final AStarPathfinder<Cells, Link> pathfinder;
  private final Vector<Packet> packets;

  private final double totalEdgeLength;

  public Base(Graph<Cells, Link> graph) {
    this.graph = Objects.requireNonNull(
        graph, "O grafo é obrigatório.");

    this.pathfinder = new AStarPathfinder<>(
        graph,
        Link::cost,
        (a, b) -> (int) Math.hypot(
            a.pos().x - b.pos().x,
            a.pos().y - b.pos().y));

    for (Cells cell : graph)
      if (cell instanceof Cells.Core c) {
        if (core != null) {
          throw new IllegalArgumentException(
              "A base deve ter apenas um núcleo.");
        }

        core = c;
      }

    if (core == null)
      throw new IllegalArgumentException(
          "A base precisa de um núcleo.");

    this.packets = new Vector<>();

    this.totalEdgeLength = sumEdgeLengths();
  }

  private double sumEdgeLengths() {
    double sum = 0.;
    for (Link l : graph.edges())
      sum += l.genomeLength();

    return sum;
  }

  public double settledLength() {
    double sum = 0.;
    for (Link l : graph.edges())
      sum += l.length();

    return sum;
  }

  public void attractOnce() {
    for (Link l : graph.edges())
      attract(l);
  }

  private void attract(Link l) {
    Cells a = l.a();
    Cells b = l.b();
    Vec2D pa = a.pos();
    Vec2D pb = b.pos();

    double dx = pb.x - pa.x;
    double dy = pb.y - pa.y;
    double dist = Math.hypot(dx, dy);
    if (dist <= EDGE_REST_LENGTH || dist == 0.)
      return;

    double excess = dist - EDGE_REST_LENGTH;
    double step = Math.min(excess, MAX_ATTRACTION_STEP) / dist;
    double totalMass = a.mass() + b.mass();
    double aShare = b.mass() / totalMass;
    double bShare = a.mass() / totalMass;

    pa.x += dx * step * aShare;
    pa.y += dy * step * aShare;
    pb.x -= dx * step * bShare;
    pb.y -= dy * step * bShare;
  }

  public Graph<Cells, Link> graph() {
    return graph;
  }

  public Cells.Core core() {
    return core;
  }

  public AStarPathfinder<Cells, Link> pathfinder() {
    return pathfinder;
  }

  public List<Packet> packets() {
    return packets;
  }

  public boolean tick() {
    attractOnce();
    pruneDeadCells();
    tickCells();
    dispatchAmmo();
    tickPackets();

    return core.alive();
  }

  private void pruneDeadCells() {
    List<Cells> verts = graph.vertices();

    for (int i = verts.size() - 1; i >= 0; --i) {
      Cells cell = verts.get(i);
      if (!cell.alive())
        graph.removeVertex(cell);
    }
  }

  private void tickCells() {
    for (Cells cell : graph)
      cell.tick();
  }

  private void dispatchAmmo() {
    for (Cells cell : graph) {
      if (!(cell instanceof Cells.Extractor extractor) || extractor.stored() <= 0)
        continue;

      Cells.Turret target = findNeediestTurret(extractor);
      if (target == null)
        continue;

      List<Cells> path = pathfinder.findPath(extractor, target);
      if (path != null && path.size() >= 2 && extractor.consume())
        packets.add(new Packet(path, PACKET_SPEED, graph));
    }
  }

  private Cells.Turret findNeediestTurret(Cells.Extractor source) {
    Cells.Turret best = null;
    double bestScore = Double.POSITIVE_INFINITY;

    for (Cells cell : graph) {
      if (!(cell instanceof Cells.Turret turret) || !turret.alive())
        continue;

      int reserved = turret.stored() + inFlightTo(turret);
      if (reserved >= turret.cap())
        continue;

      double dist = Vec2D.dist(source.pos(), turret.pos());
      double score = dist + reserved * 100.;
      if (score < bestScore) {
        bestScore = score;
        best = turret;
      }
    }

    return best;
  }

  private int inFlightTo(Cells.Turret turret) {
    int count = 0;
    for (int i = 0; i < packets.size(); ++i) {
      Packet p = packets.get(i);
      if (!p.done() && p.destination() == turret)
        ++count;
    }
    return count;
  }

  private void tickPackets() {
    for (int i = packets.size() - 1; i >= 0; --i) {
      Packet p = packets.get(i);
      p.tick(graph);
      if (p.done())
        packets.swapRemove(i);
    }
  }

  public double totalEdgeLength() {
    return totalEdgeLength;
  }
}
