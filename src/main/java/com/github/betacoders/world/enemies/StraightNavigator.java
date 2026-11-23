package com.github.betacoders.world.enemies;

import com.github.betacoders.collections.List;
import com.github.betacoders.collections.Vec2D;
import com.github.betacoders.world.Base;
import com.github.betacoders.world.Cells;
import com.github.betacoders.world.Link;

public final class StraightNavigator implements GroundNavigator {
  public static final StraightNavigator INSTANCE = new StraightNavigator();

  private static final int MAX_ITERATIONS = 4;
  private static final double EPS = 1e-9;

  private StraightNavigator() {
  }

  @Override
  public Cells navigate(GroundEnemy enemy, Base base, Vec2D outWaypoint) {
    Cells core = base.core();
    Cells goal = core;
    List<Link> edges = base.graph().edges(); // aloca: so chamado em recalculo

    for (int i = 0; i < MAX_ITERATIONS; ++i) {
      Cells obstacle = firstObstacle(enemy, base, goal, edges);
      if (obstacle == null)
        break;

      goal = obstacle;
    }

    outWaypoint.x = goal.pos().x;
    outWaypoint.y = goal.pos().y;
    return goal == core ? null : goal;
  }

  private static Cells firstObstacle(GroundEnemy enemy, Base base, Cells goal,
      List<Link> edges) {
    Vec2D p = enemy.pos();
    Vec2D g = goal.pos();

    double dx = g.x - p.x;
    double dy = g.y - p.y;
    double len = Math.hypot(dx, dy);
    double stopDist = goal.rad() + enemy.rad()
        + enemy.attackRange() * EnemyAbs.HOLD_FRACTION;

    double travel = len - stopDist;
    if (travel <= EPS)
      return null; // ja esta no alcance

    double ex = p.x + dx / len * travel;
    double ey = p.y + dy / len * travel;

    Cells best = null;
    double bestT = Double.POSITIVE_INFINITY;

    for (int i = 0; i < edges.size(); ++i) {
      Link l = edges.get(i);
      Cells a = l.a();
      Cells b = l.b();

      if (a == goal || b == goal || !a.alive() || !b.alive())
        continue;

      double t = segmentHitT(p.x, p.y, ex, ey,
          a.pos().x, a.pos().y, b.pos().x, b.pos().y, enemy.rad());
      if (Double.isNaN(t))
        continue;
      double cx = p.x + (ex - p.x) * t;
      double cy = p.y + (ey - p.y) * t;
      double da = distSq(cx, cy, a.pos().x, a.pos().y);
      double db = distSq(cx, cy, b.pos().x, b.pos().y);
      Cells owner = (da < db || (da == db && a.id() < b.id())) ? a : b;

      if (better(t, owner, bestT, best)) {
        bestT = t;
        best = owner;
      }
    }

    for (Cells c : base.graph()) {
      if (c == goal || !c.alive())
        continue;

      double t = projectT(c.pos().x, c.pos().y, p.x, p.y, ex, ey);
      double px = p.x + (ex - p.x) * t;
      double py = p.y + (ey - p.y) * t;
      double r = c.rad() + enemy.rad();

      if (distSq(px, py, c.pos().x, c.pos().y) <= r * r
          && better(t, c, bestT, best)) {
        bestT = t;
        best = c;
      }
    }

    return best;
  }

  private static boolean better(double t, Cells c, double bestT, Cells best) {
    return t < bestT || (t == bestT && best != null && c.id() < best.id());
  }

  static double segmentHitT(double p1x, double p1y, double p2x, double p2y,
      double q1x, double q1y, double q2x, double q2y, double clearance) {
    double rx = p2x - p1x, ry = p2y - p1y;
    double sx = q2x - q1x, sy = q2y - q1y;
    double denom = cross(rx, ry, sx, sy);

    // Cruzamento real: p1 + t*r = q1 + u*s.
    if (Math.abs(denom) > EPS) {
      double qpx = q1x - p1x, qpy = q1y - p1y;
      double t = cross(qpx, qpy, sx, sy) / denom;
      double u = cross(qpx, qpy, rx, ry) / denom;
      if (t >= 0 && t <= 1 && u >= 0 && u <= 1)
        return t;
    }
    double c2 = clearance * clearance;
    double bestD = Double.POSITIVE_INFINITY;
    double bestT = Double.NaN;

    double d = pointSegDistSq(p1x, p1y, q1x, q1y, q2x, q2y);
    if (d < bestD) {
      bestD = d;
      bestT = 0.;
    }
    d = pointSegDistSq(p2x, p2y, q1x, q1y, q2x, q2y);
    if (d < bestD) {
      bestD = d;
      bestT = 1.;
    }

    double tq = projectT(q1x, q1y, p1x, p1y, p2x, p2y);
    d = distSq(q1x, q1y, p1x + rx * tq, p1y + ry * tq);
    if (d < bestD) {
      bestD = d;
      bestT = tq;
    }
    tq = projectT(q2x, q2y, p1x, p1y, p2x, p2y);
    d = distSq(q2x, q2y, p1x + rx * tq, p1y + ry * tq);
    if (d < bestD) {
      bestD = d;
      bestT = tq;
    }

    return bestD <= c2 ? bestT : Double.NaN;
  }

  static double projectT(double x, double y, double ax, double ay, double bx, double by) {
    double abx = bx - ax, aby = by - ay;
    double lenSq = abx * abx + aby * aby;
    if (lenSq < EPS)
      return 0.;

    double t = ((x - ax) * abx + (y - ay) * aby) / lenSq;
    return Math.max(0., Math.min(1., t));
  }

  static double pointSegDistSq(double x, double y, double ax, double ay, double bx, double by) {
    double t = projectT(x, y, ax, ay, bx, by);
    return distSq(x, y, ax + (bx - ax) * t, ay + (by - ay) * t);
  }

  private static double cross(double ax, double ay, double bx, double by) {
    return ax * by - ay * bx;
  }

  private static double distSq(double ax, double ay, double bx, double by) {
    double dx = bx - ax, dy = by - ay;
    return dx * dx + dy * dy;
  }
}
