package com.github.betacoders.world;

import java.util.Objects;
import java.util.Random;

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

  private final Graph<Cells, Link> graph;
  private Cells.Core core;
  private final AStarPathfinder<Cells, Link> pathfinder;
  private final Vector<Packet> packets;
  private final Random rng;

  private int wave = 1;
  private boolean over = false;
  private int tick = 0;
  private int enemiesKilled = 0;

  private final double totalEdgeLength;

  public Base(Graph<Cells, Link> graph, long seed) {
    this.graph = Objects.requireNonNull(
        graph, "O grafo é obrigatório.");

    this.rng = new Random(seed);

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
      sum += l.length();

    return sum;
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

  public int wave() {
    return wave;
  }

  public int wavesCompleted() {
    return wave - 1;
  }

  public boolean tick() {
    if (over)
      return false;

    if (core == null || !core.alive()) {
      over = true;
      return false;
    }

    ++tick;

    pruneDeadCells();
    tickCells();
    dispatchAmmo();
    tickPackets();

    if (!core.alive()) {
      over = true;
      return false;
    }

    return true;
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

  public boolean over() {
    return over;
  }

  public int ticks() {
    return tick;
  }

  public int enemiesKilled() {
    return enemiesKilled;
  }

  public double totalEdgeLength() {
    return totalEdgeLength;
  }

  public void recordEnemyKilled() {
    if (!over())
      enemiesKilled++;
  }

  public void completeWave() {
    if (!over()) {
      wave++;
    }
  }
}
