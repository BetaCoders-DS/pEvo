package com.github.betacoders.world;

import java.util.Random;

import com.github.betacoders.algo.AStarPathfinder;
import com.github.betacoders.collections.Graph;
import com.github.betacoders.collections.List;
import com.github.betacoders.collections.Vector;

/**
 * World
 */
public class World {
  private final Graph<Cells, Link> graph;
  private Cells.Core core;
  private final AStarPathfinder<Cells, Link> pathfinder;
  private final List<Packet> packets;
  private final Random rng;

  public World(Graph<Cells, Link> graph, long seed) {
    this.graph = graph;
    this.rng = new Random(seed);

    this.pathfinder = new AStarPathfinder<>(graph, Link::cost,
        (a, b) -> (int) Math.hypot(
            a.pos().x - b.pos().x,
            a.pos().y - b.pos().y));

    this.packets = new Vector<>();

    for (Cells cell : graph)
      if (cell instanceof Cells.Core c)
        this.core = c;
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

}
