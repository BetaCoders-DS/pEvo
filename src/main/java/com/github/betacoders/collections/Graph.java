package com.github.betacoders.collections;

import java.util.Arrays;

public class Graph {
  @FunctionalInterface
  public interface CoordinateConsumer {
    void accept(int x, int y);
  }

  public static final int BLOCKED = -1;
  public static final int DEFAULT_COST = 1;
  private static final int NO_PORTAL = -1;

  private static final int[] DX = { 0, 1, 0, -1 };
  private static final int[] DY = { -1, 0, 1, 0 };

  private final int width;
  private final int height;

  private final int[] costs;
  private final int[] portals;

  public Graph(int width, int height) {
    this(width, height, DEFAULT_COST);
  }

  public Graph(int width, int height, int defaultCost) {
    this.width = width;
    this.height = height;
    int totalNodes = width * height;

    this.costs = new int[totalNodes];
    this.portals = new int[totalNodes];

    Arrays.fill(costs, defaultCost);
    Arrays.fill(portals, NO_PORTAL);
  }

  public void setCost(int x, int y, int cost) {
    if (inBounds(x, y))
      costs[pack(x, y)] = cost;
  }

  public int getCost(int x, int y) {
    return inBounds(x, y) ? costs[pack(x, y)] : BLOCKED;
  }

  public void fillCost(int cost) {
    Arrays.fill(costs, cost);
  }

  public boolean isTraversable(int x, int y) {
    return inBounds(x, y) && costs[pack(x, y)] >= 0;
  }

  public void addPortal(int x1, int y1, int x2, int y2) {
    if (inBounds(x1, y1) && inBounds(x2, y2))
      portals[pack(x1, y1)] = pack(x2, y2);
  }

  public void removePortal(int x, int y) {
    if (inBounds(x, y))
      portals[pack(x, y)] = NO_PORTAL;
  }

  public void clearPortals() {
    Arrays.fill(portals, NO_PORTAL);
  }

  public boolean hasPortal(int x, int y) {
    return inBounds(x, y) && portals[pack(x, y)] != NO_PORTAL;
  }

  public void clear(int defaultCost) {
    fillCost(defaultCost);
    clearPortals();
  }

  public void clear() {
    clear(DEFAULT_COST);
  }

  public void forEachNeighbor(int x, int y, CoordinateConsumer consumer) {
    if (!isTraversable(x, y))
      return;

    for (int i = 0; i < 4; i++)
      if (isTraversable(x + DX[i], y + DY[i]))
        consumer.accept(x + DX[i], y + DY[i]);

    int target = portals[pack(x, y)];
    if (target != NO_PORTAL && isTraversable(unpackX(target), unpackY(target)))
      consumer.accept(unpackX(target), unpackY(target));
  }

  public int getEdgeWeight(int fromX, int fromY, int toX, int toY) {
    if (!isTraversable(fromX, fromY) || !isTraversable(toX, toY))
      return BLOCKED;

    int dx = Math.abs(fromX - toX);
    int dy = Math.abs(fromY - toY);

    if (dx + dy == 1)
      return costs[pack(toX, toY)];

    int target = portals[pack(fromX, fromY)];
    if (target != NO_PORTAL && target == pack(toX, toY))
      return costs[target];

    return BLOCKED;
  }

  public int width() {
    return width;
  }

  public int height() {
    return height;
  }

  public boolean inBounds(int x, int y) {
    return x >= 0 && x < width && y >= 0 && y < height;
  }

  private int pack(int x, int y) {
    return y * width + x;
  }

  private int unpackX(int index) {
    return index % width;
  }

  private int unpackY(int index) {
    return index / width;
  }
}
