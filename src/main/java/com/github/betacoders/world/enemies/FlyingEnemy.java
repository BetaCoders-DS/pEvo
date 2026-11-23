package com.github.betacoders.world.enemies;

import com.github.betacoders.world.Base;
import com.github.betacoders.world.Cells;

public final class FlyingEnemy extends EnemyAbs{
    public static final float HP_MAX = 30f;
    public static final double MASS = 1.;
    public static final double RAD = 10;
    public static final double VEL_MAX = 2.5;
    public static final double FORCE_MAX = 5.;
    public static final float ATTACK_DAMAGE = 5f;
    public static final double ATTACK_RANGE = 8.;
    public static final int ATTACK_INTERVAL = 30;

    public static final int RETARGET_INTERVAL = 30;
    private int retargetTimer;

    public FlyingEnemy(int id, double x, double y) {
        this(id, x, y, HP_MAX, RAD, VEL_MAX, FORCE_MAX,
            ATTACK_DAMAGE, ATTACK_RANGE, ATTACK_INTERVAL);
    }

    public FlyingEnemy(int id, double x, double y, float hpMax, double rad,
            double velMax, double forceMax, float attackDamage, double attackRange,
            int attackInterval) {
        super(id, x, y, hpMax, MASS, rad, velMax, forceMax,
            attackDamage, attackRange, attackInterval);
        this.retargetTimer = Math.floorMod(id, RETARGET_INTERVAL);
    }

    @Override
    public boolean flying() {
        return true;
    }

    @Override
    public boolean tick(Base base) {
        if(!alive()) return false;

        tickCooldown();
        if(--retargetTimer <= 0 || !targetValid()) {
            acquireTarget(base);
            retargetTimer = RETARGET_INTERVAL;
        }
        if(target == null || inHoldRange(target)) {
            stop();
        } else {
            seek(target.pos().x, target.pos().y);
        }
        integrate();
        return true;
    }

private void acquireTarget(Base base) {
    Cells best = null;
    double bestDist = Double.POSITIVE_INFINITY;

    for (Cells c : base.graph()) {
      if (!c.alive())
        continue;

      double diddy = edgeDistance(c);
      if (diddy < bestDist || (diddy == bestDist && best != null && c.id() < best.id())) {
        bestDist = diddy;
        best = c;
      }
    }
    target = best;
  }
}

