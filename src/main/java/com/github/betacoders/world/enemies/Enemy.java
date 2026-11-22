package com.github.betacoders.world.enemies;

import com.github.betacoders.collections.Vec2D;
import com.github.betacoders.world.World;

/**
 * Enemy
 *
 * Base dos inimigos. Sem Processing: só Vec2D e Math, para rodar headless.
 * Unidades: px, px/s, g, s.
 */
public abstract class Enemy {
protected static final double DRAG = 1.0;

  protected final Vec2D pos;
  protected final Vec2D vel = new Vec2D();

  protected final double mass;
  protected final double velMax;
  protected final double forceMax;

  protected final float hpMax;
  protected float hp;

  protected final float attackDamage;
  protected final double attackRange;
  protected final double attackInterval;
  protected double cooldown;

  protected Damageable target;

  private final Vec2D desired = new Vec2D();
  private final Vec2D steer = new Vec2D();
  private final Vec2D acc = new Vec2D();

  protected Enemy(double x, double y, float hpMax, double mass, double velMax,
      double forceMax, float attackDamage, double attackRange, double attackInterval) {
    this.pos = new Vec2D(x, y);
    this.hpMax = hpMax;
    this.hp = hpMax;
    this.mass = mass;
    this.velMax = velMax;
    this.forceMax = forceMax;
    this.attackDamage = attackDamage;
    this.attackRange = attackRange;
    this.attackInterval = attackInterval;
    this.cooldown = 0.;
  }

    public abstract boolean flying();
    public abstract void update(World world, double dt);

    public Vec2D pos() {
        return pos;
    }

    public Vec2D vel() {
        return vel;
    }

    public float hp() {
        return hp;
    }

    public float hpMax() {
        return hpMax;
    }

    public boolean alive() {
        return hp > 0f;
    }
    
    public void damage(float amount) {
        hp = Math.max(0f, hp - amount);
    }

    public Damageable target() {
        return target;
    }
}
