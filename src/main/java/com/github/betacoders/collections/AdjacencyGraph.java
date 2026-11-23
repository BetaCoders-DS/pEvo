package com.github.betacoders.collections;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class AdjacencyGraph<V, E> implements Graph<V, E> {
  private static final int INIT_CAP = 16;
  private static final int INIT_DEG = 2;

  private final Map<V, Integer> slot;

  private Vector<V> verts;
  private Vector<E> edges;

  private int[][] vertEdges;
  private int[] vertDeg;

  private int[] edgeFrom;
  private int[] edgeTo;

  private int[] freeVerts;
  private int[] freeEdges;
  private int freeVertCount;
  private int freeEdgeCount;

  private int vertCount;
  private int edgeCount;

  public AdjacencyGraph() {
    this(INIT_CAP);
  }

  public AdjacencyGraph(int cap) {
    int capacity = Math.max(4, cap);
    int edgeCap = capacity * INIT_DEG;

    this.slot = new HashMap<>(capacity);
    this.verts = new Vector<>(capacity);
    this.edges = new Vector<>(edgeCap);

    this.vertEdges = new int[capacity][INIT_DEG];
    this.vertDeg = new int[capacity];

    this.edgeFrom = new int[edgeCap];
    this.edgeTo = new int[edgeCap];

    this.freeVerts = new int[capacity];
    this.freeEdges = new int[edgeCap];
  }

  @Override
  public int size() {
    return vertCount;
  }

  @Override
  public int edgeCount() {
    return edgeCount;
  }

  @Override
  public boolean containsVertex(V v) {
    return slotOf(v) >= 0;
  }

  @Override
  public boolean containsEdge(V a, V b) {
    return edge(a, b) != null;
  }

  @Override
  public E edge(V a, V b) {
    int from = slotOf(a);
    int to = slotOf(b);
    if (from < 0 || to < 0)
      return null;

    int e = edgeBetween(from, to);
    return e >= 0 ? edges.get(e) : null;
  }

  @Override
  public List<V> vertices() {
    List<V> out = new Vector<>(vertCount);
    for (int i = 0; i < verts.size(); ++i) {
      V v = verts.get(i);
      if (v != null)
        out.add(v);
    }
    return out;
  }

  @Override
  public List<V> neighbors(V v) {
    int from = slotOf(v);
    if (from < 0)
      return null;

    int deg = vertDeg[from];
    int[] inc = vertEdges[from];
    List<V> out = new Vector<>(deg);

    for (int i = 0; i < deg; ++i)
      out.add(verts.get(otherEnd(inc[i], from)));

    return out;
  }

  @Override
  public void forEachNeighbor(V v, NeighborConsumer<V, E> consumer) {
    int from = slotOf(v);
    if (from < 0)
      return;

    int deg = vertDeg[from];
    int[] inc = vertEdges[from];

    for (int i = 0; i < deg; ++i) {
      int e = inc[i];
      consumer.accept(
          verts.get(otherEnd(e, from)),
          edges.get(e));
    }
  }

  @Override
  public boolean addVertex(V v) {
    if (v == null || containsVertex(v))
      return false;

    allocVertex(v);
    return true;
  }

  @Override
  public E addEdge(V a, V b, E edge) {
    if (a == null || b == null || edge == null)
      return null;

    int from = ensureVertex(a);
    int to = ensureVertex(b);

    int existing = edgeBetween(from, to);
    if (existing >= 0) {
      E old = edges.get(existing);
      edges.set(existing, edge);
      return old;
    }

    int e = allocEdge(from, to, edge);
    attachEdge(from, e);
    if (from != to)
      attachEdge(to, e);

    return null;
  }

  @Override
  public E removeEdge(V a, V b) {
    int from = slotOf(a);
    int to = slotOf(b);
    if (from < 0 || to < 0)
      return null;

    int e = edgeBetween(from, to);
    if (e < 0)
      return null;

    E removed = edges.get(e);
    detachEdge(from, e);
    if (from != to)
      detachEdge(to, e);

    freeEdge(e);
    return removed;
  }

  @Override
  public boolean removeVertex(V v) {
    int vert = slotOf(v);
    if (vert < 0)
      return false;

    int deg = vertDeg[vert];
    int[] inc = vertEdges[vert];

    for (int i = 0; i < deg; ++i) {
      int e = inc[i];
      int other = otherEnd(e, vert);
      if (other != vert)
        detachEdge(other, e);
      freeEdge(e);
    }

    vertDeg[vert] = 0;
    verts.set(vert, null);
    slot.remove(v);
    freeVerts = ensureCap(freeVerts, freeVertCount + 1);
    freeVerts[freeVertCount++] = vert;
    --vertCount;
    return true;
  }

  @Override
  public void clear() {
    slot.clear();
    verts.clear();
    edges.clear();
    Arrays.fill(vertDeg, 0);
    freeVertCount = 0;
    freeEdgeCount = 0;
    vertCount = 0;
    edgeCount = 0;
  }

  @Override
  public Iterator<V> iterator() {
    return new Iterator<>() {
      private int cursor = advance(0);

      private int advance(int i) {
        while (i < verts.size() && verts.get(i) == null)
          ++i;
        return i;
      }

      @Override
      public boolean hasNext() {
        return cursor < verts.size();
      }

      @Override
      public V next() {
        if (!hasNext())
          throw new NoSuchElementException();

        V v = verts.get(cursor);
        cursor = advance(cursor + 1);
        return v;
      }
    };
  }

  @Override
  public List<E> edges() {
    List<E> out = new Vector<>(edgeCount);
    for (int i = 0; i < edges.size(); ++i) {
      E e = edges.get(i);
      if (e != null)
        out.add(e);
    }
    return out;
  }

  private int slotOf(V v) {
    if (v == null)
      return -1;

    Integer idx = slot.get(v);
    return idx == null ? -1 : idx.intValue();
  }

  private int otherEnd(int edge, int vert) {
    int from = edgeFrom[edge];
    return from == vert ? edgeTo[edge] : from;
  }

  private int edgeBetween(int from, int to) {
    int deg = vertDeg[from];
    int[] inc = vertEdges[from];

    for (int i = 0; i < deg; ++i) {
      int e = inc[i];
      if (otherEnd(e, from) == to)
        return e;
    }

    return -1;
  }

  private int ensureVertex(V v) {
    int existing = slotOf(v);
    return existing >= 0 ? existing : allocVertex(v);
  }

  private int allocVertex(V v) {
    int vert;
    if (freeVertCount > 0) {
      vert = freeVerts[--freeVertCount];
      verts.set(vert, v);
    } else {
      vert = verts.size();
      vertEdges = ensureCap(vertEdges, vert + 1);
      vertDeg = ensureCap(vertDeg, vert + 1);
      if (vertEdges[vert] == null)
        vertEdges[vert] = new int[INIT_DEG];
      verts.add(v);
    }

    vertDeg[vert] = 0;
    slot.put(v, vert);
    ++vertCount;
    return vert;
  }

  private int allocEdge(int from, int to, E edge) {
    int e;
    if (freeEdgeCount > 0) {
      e = freeEdges[--freeEdgeCount];
      edges.set(e, edge);
    } else {
      e = edges.size();
      edgeFrom = ensureCap(edgeFrom, e + 1);
      edgeTo = ensureCap(edgeTo, e + 1);
      edges.add(edge);
    }

    edgeFrom[e] = from;
    edgeTo[e] = to;
    ++edgeCount;
    return e;
  }

  private void freeEdge(int edge) {
    edges.set(edge, null);
    freeEdges = ensureCap(freeEdges, freeEdgeCount + 1);
    freeEdges[freeEdgeCount++] = edge;
    --edgeCount;
  }

  private void attachEdge(int vert, int edge) {
    int deg = vertDeg[vert];
    vertEdges[vert] = ensureCap(vertEdges[vert], deg + 1);
    vertEdges[vert][deg] = edge;
    vertDeg[vert] = deg + 1;
  }

  private void detachEdge(int vert, int edge) {
    int deg = vertDeg[vert];
    int[] inc = vertEdges[vert];

    for (int i = 0; i < deg; ++i) {
      if (inc[i] == edge) {
        inc[i] = inc[--vertDeg[vert]];
        return;
      }
    }
  }

  private static int[] ensureCap(int[] arr, int needed) {
    if (needed <= arr.length)
      return arr;

    return Arrays.copyOf(arr, Math.max(INIT_DEG, arr.length * 2));
  }

  private static int[][] ensureCap(int[][] arr, int needed) {
    if (needed <= arr.length)
      return arr;

    return Arrays.copyOf(arr, arr.length * 2);
  }
}
