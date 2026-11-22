package com.github.betacoders.world.enemies;

import com.github.betacoders.collections.Vec2D;
import com.github.betacoders.world.World;

/**
 * Enemy
 *
 * Base dos inimigos. Sem Processing: só Vec2D e Math, para rodar headless.
 * Unidades: px, px/s, g, s.
 *
 * Usa só o que o Vec2D atual tem: campos x/y, multiply, divide.
 * Operações extras (normalizar, limitar, somar escalado) são helpers privados aqui.
 */

public abstract class Enemy {
  protected static final double DRAG = 1.0; 
  protected final Vec2D pos;
  protected final Vec2D vel = new Vec2D();

  protected final double mass; // g
  protected final double velMax; // px/s
  protected final double forceMax; // g*px/s^2

  protected final float hpMax;
  protected float hp;

  protected final float attackDamage;
  protected final double attackRange; // px
  protected final double attackInterval; // s
  protected double cooldown; // s

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

  protected void seek(double tx, double ty) {
    desired.x = tx - pos.x;
    desired.y = ty - pos.y;
    normalizeInPlace(desired);
    desired.multiply(velMax);
    applySteering();
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

  protected void integrate(double dt) {
    vel.x += acc.x * dt;
    vel.y += acc.y * dt;

    vel.multiply(Math.max(0., 1. - DRAG * dt));
    limitInPlace(vel, velMax);

    pos.x += vel.x * dt;
    pos.y += vel.y * dt;

    acc.x = 0;
    acc.y = 0;
  }

  protected void tickCooldown(double dt) {
    if (cooldown > 0)
      cooldown = Math.max(0., cooldown - dt);
  }

  protected boolean inRange(Damageable t) {
    return t != null && t.alive() && t.distanceFrom(pos) <= attackRange;
  }

  protected boolean tryAttack() {
    if (cooldown > 0 || !inRange(target))
      return false;

    target.damage(attackDamage);
    cooldown = attackInterval;
    return true;
  }

  private static void normalizeInPlace(Vec2D v) {
    double len = Math.sqrt(v.x * v.x + v.y * v.y);
    if (len > 0)
      v.divide(len);
  }

  private static void limitInPlace(Vec2D v, double max) {
    if (v.x * v.x + v.y * v.y > max * max) {
      normalizeInPlace(v);
      v.multiply(max);
    }
  }
}