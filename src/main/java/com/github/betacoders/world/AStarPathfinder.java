package com.github.betacoders.world;

import com.github.betacoders.collections.Graph;
import com.github.betacoders.collections.HashMap;
import com.github.betacoders.collections.List;
import com.github.betacoders.collections.Map;
import com.github.betacoders.collections.Vector;
import java.util.PriorityQueue;
import java.util.function.ToIntBiFunction;
import java.util.function.ToIntFunction;

/**
 * AStarPathfinder
 * 
 * @param <T> Vertex type
 * @param <E> Edge Type
 */
public class AStarPathfinder<T, E> {

  private record Node<T>(T vertex, int distToStart) implements Comparable<Node<T>> {
    @Override
    public int compareTo(Node<T> other) {
      return Integer.compare(this.distToStart, other.distToStart);
    }
  }

  private final Graph<T, E> graph;
  private final ToIntFunction<E> edgeCost;
  private final ToIntBiFunction<T, T> heuristic;
  private final Map<T, Integer> toHereCost = new HashMap<>();
  private final Map<T, T> prevVertex = new HashMap<>();
  private final Map<T, Integer> lastVisitedAt = new HashMap<>();
  private int searchId;

  public AStarPathfinder(Graph<T, E> graph, ToIntFunction<E> edgeCost) {
    this(graph, edgeCost, (vertex, target) -> 0);
  }

  public AStarPathfinder(Graph<T, E> graph, ToIntFunction<E> edgeCost,
      ToIntBiFunction<T, T> heuristic) {
    this.graph = graph;
    this.edgeCost = edgeCost;
    this.heuristic = heuristic;
  }

  public static <T> AStarPathfinder<T, Integer> weighted(Graph<T, Integer> graph) {
    return new AStarPathfinder<>(graph, cost -> cost);
  }

  public List<T> findPath(T start, T target) {
    if (start == null || target == null || !graph.containsVertex(start)
        || !graph.containsVertex(target))
      return new Vector<>();

    ++searchId;
    toHereCost.put(start, 0);
    prevVertex.put(start, null);
    lastVisitedAt.put(start, searchId);

    PriorityQueue<Node<T>> toVisit = new PriorityQueue<>();
    toVisit.add(new Node<>(start, heuristic.applyAsInt(start, target)));

    while (!toVisit.isEmpty()) {
      T current = toVisit.poll().vertex();

      if (current.equals(target))
        return buildPath(target);

      graph.forEachNeighbor(current,
          (neighbor, edge) -> considerNeighbor(current, neighbor, edge, target, toVisit));
    }

    return new Vector<>();
  }

  private void considerNeighbor(T current, T neighbor, E edge, T target,
      PriorityQueue<Node<T>> toVisit) {
    if (edge == null)
      return;

    int stepCost = edgeCost.applyAsInt(edge);
    if (stepCost < 0)
      return;

    int candidateCost = toHereCost.get(current) + stepCost;
    Integer lastVisit = lastVisitedAt.get(neighbor);
    if (lastVisit != null && lastVisit.intValue() == searchId
        && candidateCost >= toHereCost.get(neighbor))
      return;

    lastVisitedAt.put(neighbor, searchId);
    toHereCost.put(neighbor, candidateCost);
    prevVertex.put(neighbor, current);
    toVisit.add(new Node<>(neighbor,
        candidateCost + heuristic.applyAsInt(neighbor, target)));
  }

  private List<T> buildPath(T target) {
    Vector<T> reversedPath = new Vector<>();
    for (T current = target; current != null; current = prevVertex.get(current))
      reversedPath.push(current);

    Vector<T> path = new Vector<>(reversedPath.size());
    for (int i = reversedPath.size() - 1; i >= 0; --i)
      path.push(reversedPath.get(i));

    return path;
  }
}
