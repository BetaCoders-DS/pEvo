package com.github.betacoders.collections;

public interface Graph<V, E> extends Iterable<V> {
  int size();

  int edgeCount();

  boolean containsVertex(V v);

  boolean containsEdge(V a, V b);

  E edge(V a, V b);

  List<V> vertices();

  List<E> edges();

  List<V> neighbors(V v);

  void forEachNeighbor(V v, NeighborConsumer<V, E> consumer);

  default boolean addVertex(V v) {
    throw new UnsupportedOperationException("Read-only graph");
  }

  default E addEdge(V a, V b, E edge) {
    throw new UnsupportedOperationException("Read-only graph");
  }

  default E removeEdge(V a, V b) {
    throw new UnsupportedOperationException("Read-only graph");
  }

  default boolean removeVertex(V v) {
    throw new UnsupportedOperationException("Read-only graph");
  }

  default void clear() {
    throw new UnsupportedOperationException("Read-only graph");
  }

  interface NeighborConsumer<V, E> {
    void accept(V neighbor, E edge);
  }
}
