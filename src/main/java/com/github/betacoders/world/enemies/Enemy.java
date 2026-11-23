package com.github.betacoders.world.enemies;

import com.github.betacoders.collections.Vec2D;
import com.github.betacoders.world.Base;
import com.github.betacoders.world.Cells;

public abstract class Enemy {
  protected static final double DRAG = 0.02;

  protected final Vec2D pos;
  protected final Vec2D vel = new Vec2D();

  protected final double mass;
  protected final double rad;
  protected final double velMax;
  protected final double forceMax;

  protected final float hpMax;
  protected float hp;

  protected final float attackDamage;
  protected final double attackRange;
  protected final int attackInterval;
  protected int cooldown;

  protected Cells target;

  private final Vec2D desired = new Vec2D();
  private final Vec2D steer = new Vec2D();
  private final Vec2D acc = new Vec2D();

  protected Enemy(double x, double y, float hpMax, double mass, double rad,
      double velMax, double forceMax, float attackDamage, double attackRange,
      int attackInterval) {
    this.pos = new Vec2D(x, y);
    this.hpMax = hpMax;
    this.hp = hpMax;
    this.mass = mass;
    this.rad = rad;
    this.velMax = velMax;
    this.forceMax = forceMax;
    this.attackDamage = attackDamage;
    this.attackRange = attackRange;
    this.attackInterval = attackInterval;
    this.cooldown = 0;
  }

  public abstract boolean flying();

  public abstract boolean tick(Base base);

  public Vec2D pos() {
    return pos;
  }

  public Vec2D vel() {
    return vel;
  }

  public double rad() {
    return rad;
  }

  public float hp() {
    return hp;
  }

  public float hpMax() {
    return hpMax;
  }

  public Cells target() {
    return target;
  }

  public boolean alive() {
    return hp > 0f;
  }

  public void damage(float amount) {
    hp = Math.max(0f, hp - amount);
  }

  public double distanceFrom(Vec2D p) {
    return Vec2D.dist(pos, p) - rad;
  }

  protected void seek(double tx, double ty) {
    desired.x = tx - pos.x;
    desired.y = ty - pos.y;
    normalizeInPlace(desired);

  }

  protected void stop() {
    desired.x = 0;
    desired.y = 0;
    applySteering();
  }

  private void applySteering() {
    steer.x = desired.x - vel.x;
    steer.y = desired.y - vel.y;
    limitInPlace(steer, forceMax);
    acc.x = steer.x / mass;
    acc.y = steer.y / mass;
  }

  protected void integrate() {
    vel.x += acc.x;
    vel.y += acc.y;

    vel.multiply(1. - DRAG);
    limitInPlace(vel, velMax);

    pos.x += vel.x;
    pos.y += vel.y;

    acc.x = 0;
    acc.y = 0;
  }

  protected void tickCooldown() {
    if (cooldown > 0)
      --cooldown;
  }

  protected boolean inRange(Cells c) {
    return c != null && c.alive()
        && Vec2D.dist(c.pos(), pos) - c.rad() <= attackRange;
  }

  protected boolean tryAttack() {
    if (cooldown > 0 || !inRange(target)) return false;

    target.damage(attackDamage);
    cooldown = attackInterval;
    return true;
  }

  private static void normalizeInPlace(Vec2D v) {
    double len = Math.sqrt(v.x * v.x + v.y * v.y);
    if (len > 0) v.divide(len);
  }

  private static void limitInPlace(Vec2D v, double max) {
    if (v.x * v.x + v.y * v.y > max * max) {
      normalizeInPlace(v);
      v.multiply(max);
    }
  }
}