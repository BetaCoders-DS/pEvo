package com.github.betacoders.world.enemies;

import com.github.betacoders.collections.Vec2D;
import com.github.betacoders.world.Base;
import com.github.betacoders.world.Cells;

public class GroundEnemy extends EnemyAbs {
  public static final float HP_MAX = 80f;
  public static final double MASS = 1.;
  public static final double RAD = 8.;
  public static final double VEL_MAX = 1.2;
  public static final double FORCE_MAX = 0.3;
  public static final float ATTACK_DAMAGE = 10f;
  public static final double ATTACK_RANGE = 6.;
  public static final int ATTACK_INTERVAL = 45;

  private static final int RECOMPUTE_INTERVAL = 10;

  private final GroundNavigator navigator;
  private final Vec2D waypoint = new Vec2D();

  private Cells blocker;
  private int lastGraphSize = -1;
  private int recomputeTimer;
  private int recomputations;

  public GroundEnemy(int id, double x, double y) {
    this(id, x, y, HP_MAX, MASS, RAD, VEL_MAX, FORCE_MAX,
        ATTACK_DAMAGE, ATTACK_RANGE, ATTACK_INTERVAL, StraightNavigator.INSTANCE);
  }

  protected GroundEnemy(int id, double x, double y, float hpMax, double mass,
      double rad, double velMax, double forceMax, float attackDamage,
      double attackRange, int attackInterval, GroundNavigator navigator) {
    super(id, x, y, hpMax, mass, rad, velMax, forceMax,
        attackDamage, attackRange, attackInterval);

    if (navigator == null)
      throw new IllegalArgumentException("Navegador obrigatorio.");

    this.navigator = navigator;
  }

  @Override
  public boolean flying() {
    return false;
  }

  @Override
  public boolean tick(Base base) {
    if (!alive())
      return false;

    tickCooldown();

    if (--recomputeTimer <= 0
        || lastGraphSize != base.graph().size()
        || (blocker != null && !blocker.alive())) {
      blocker = navigator.navigate(this, base, waypoint);
      lastGraphSize = base.graph().size();
      recomputeTimer = RECOMPUTE_INTERVAL;
      ++recomputations;
    }

    target = blocker != null ? blocker : base.core();
    updateWaypoint(target);

    if (inHoldRange(target))
      stop();
    else
      seek(waypoint.x, waypoint.y);

    integrate();
    return true;
  }

  private void updateWaypoint(Cells cell) {
    Vec2D p = cell.pos();
    double dx = p.x - pos.x;
    double dy = p.y - pos.y;
    double dist = Math.hypot(dx, dy);
    double holdDist = cell.rad() + rad + attackRange * HOLD_FRACTION;

    if (dist <= holdDist) {
      waypoint.x = p.x;
      waypoint.y = p.y;
      return;
    }

    double scale = holdDist / dist;
    waypoint.x = p.x - dx * scale;
    waypoint.y = p.y - dy * scale;
  }

  public Cells blocker() {
    return blocker;
  }

  public int recomputations() {
    return recomputations;
  }
}
