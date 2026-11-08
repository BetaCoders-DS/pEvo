package com.github.betacoders.world;

import com.github.betacoders.collections.GridGraph;
import java.util.PriorityQueue;

public class AStarPathfinder {

  private record Node(int index, int fScore) implements Comparable<Node> {
    @Override
    public int compareTo(Node o) {
      return Integer.compare(this.fScore, o.fScore);
    }
  }

  private final GridGraph graph;
  private final int[] gScore;
  private final int[] cameFrom;
  private final int[] visitedRun;
  private int runId;

  public AStarPathfinder(GridGraph graph) {
    this.graph = graph;
    int maxNodes = graph.width() * graph.height();
    this.gScore = new int[maxNodes];
    this.cameFrom = new int[maxNodes];
    this.visitedRun = new int[maxNodes];
  }

  public int[] findPath(int startX, int startY, int targetX, int targetY) {
    if (!graph.isTraversable(startX, startY) || !graph.isTraversable(targetX, targetY))
      return new int[0];

    runId++;
    int startIndex = graph.pack(startX, startY);
    int targetIndex = graph.pack(targetX, targetY);

    visitedRun[startIndex] = runId;
    gScore[startIndex] = 0;
    cameFrom[startIndex] = -1;

    PriorityQueue<Node> openSet = new PriorityQueue<>();
    openSet.add(new Node(startIndex, heuristic(startX, startY, targetX, targetY)));

    while (!openSet.isEmpty()) {
      int curr = openSet.poll().index();

      if (curr == targetIndex)
        return reconstructPath(targetIndex);

      int currX = graph.unpackX(curr);
      int currY = graph.unpackY(curr);

      graph.forEachNeighbor(currX, currY,
          (nx, ny) -> relaxNeighbor(curr, currX, currY, nx, ny, targetX, targetY, openSet));
    }

    return new int[0];
  }

  private void relaxNeighbor(int curr, int currX, int currY, int nx, int ny, int targetX, int targetY,
      PriorityQueue<Node> openSet) {
    int weight = graph.getEdgeWeight(currX, currY, nx, ny);
    if (weight == GridGraph.BLOCKED)
      return;

    int nIndex = graph.pack(nx, ny);
    int tentativeG = gScore[curr] + weight;

    if (visitedRun[nIndex] == runId && tentativeG >= gScore[nIndex])
      return;

    visitedRun[nIndex] = runId;
    gScore[nIndex] = tentativeG;
    cameFrom[nIndex] = curr;
    openSet.add(new Node(nIndex, tentativeG + heuristic(nx, ny, targetX, targetY)));
  }

  private int heuristic(int x1, int y1, int x2, int y2) {
    return Math.abs(x1 - x2) + Math.abs(y1 - y2);
  }

  private int[] reconstructPath(int targetIndex) {
    int length = 0;
    for (int curr = targetIndex; curr != -1; curr = cameFrom[curr])
      length++;

    int[] path = new int[length];
    int curr = targetIndex;
    for (int i = length - 1; i >= 0; i--) {
      path[i] = curr;
      curr = cameFrom[curr];
    }

    return path;
  }
}
