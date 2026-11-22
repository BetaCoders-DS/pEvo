package com.github.betacoders.collections;

import java.util.Iterator;

public class AdjacencyGraph<V, E> implements Graph<V, E> {
  private static final int INIT_CAP = 16;

  private final Map<V, Map<V, E>> adjacency;
  private int edgeCount;

  public AdjacencyGraph() {
    this(INIT_CAP);
  }

  public AdjacencyGraph(int cap) {
    this.adjacency = new HashMap<>(cap);
    this.edgeCount = 0;
  }

  private Map<V, E> vertex(V v) {
    adjacency.computeIfAbsent(v, k -> new HashMap<V, E>());
    return adjacency.get(v);
  }

  @Override
  public int size() {
    return adjacency.size();
  }

  @Override
  public int edgeCount() {
    return edgeCount;
  }

  @Override
  public boolean containsVertex(V v) {
    return adjacency.get(v) != null;
  }

  @Override
  public boolean containsEdge(V a, V b) {
    return getEdge(a, b) != null;
  }

  @Override
  public E getEdge(V a, V b) {
    Map<V, E> out = adjacency.get(a);
    return out == null ? null : out.get(b);
  }

  @Override
  public Set<V> vertices() {
    return adjacency.keySet();
  }

  @Override
  public Set<V> neighbors(V v) {
    Map<V, E> out = adjacency.get(v);
    return out == null ? null : out.keySet();
  }

  @Override
  public void forEachNeighbor(V v, NeighborConsumer<V, E> consumer) {
    Map<V, E> out = adjacency.get(v);
    if (out == null)
      return;

    for (Map.Entry<V, E> e : out.entrySet())
      consumer.accept(e.key(), e.value());
  }

  @Override
  public boolean addVertex(V v) {
    if (v == null || containsVertex(v))
      return false;

    adjacency.put(v, new HashMap<V, E>());
    return true;
  }

  @Override
  public E addEdge(V a, V b, E edge) {
    if (a == null || b == null || edge == null)
      return null;

    E old = getEdge(a, b);
    if (old == null)
      ++edgeCount;

    vertex(a).put(b, edge);
    vertex(b).put(a, edge);
    return old;
  }

  @Override
  public E removeEdge(V a, V b) {
    Map<V, E> from = adjacency.get(a);
    E old = from == null ? null : from.remove(b);
    if (old == null)
      return null;

    if (!a.equals(b)) {
      Map<V, E> to = adjacency.get(b);
      if (to != null)
        to.remove(a);
    }

    --edgeCount;
    return old;
  }

  @Override
  public boolean removeVertex(V v) {
    if (!containsVertex(v))
      return false;

    for (V u : adjacency.keySet()) {
      if (u.equals(v))
        continue;

      Map<V, E> out = adjacency.get(u);
      if (out.remove(v) != null)
        --edgeCount;
    }

    Map<V, E> self = adjacency.get(v);
    if (self.remove(v) != null)
      --edgeCount;

    adjacency.remove(v);
    return true;
  }

  @Override
  public void clear() {
    adjacency.clear();
    edgeCount = 0;
  }

  @Override
  public Iterator<V> iterator() {
    return adjacency.keySet().iterator();
  }
}
