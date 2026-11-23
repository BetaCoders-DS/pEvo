package com.github.betacoders.world.enemies;

import com.github.betacoders.collections.Vec2D;
import com.github.betacoders.world.Base;
import com.github.betacoders.world.Cells;
import com.github.betacoders.world.Enemy;


public abstract class EnemyAbs implements Enemy {
  protected static final double DRAG = 0.02;

  /**
   * Fracao do alcance em que o inimigo para de se aproximar.
   * Margem para drag/overshoot nao o tirarem do alcance.
   */
  protected static final double HOLD_FRACTION = 0.8;

  protected final int id;
  protected final Vec2D pos;
  protected final Vec2D vel = new Vec2D();

  protected final double mass;
  protected final double rad;
  protected final double velMax;
  protected final double forceMax;

  protected final float hpMax;
  protected float hp;

  protected final float attackDamage;
  protected final double attackRange; // px, borda a borda
  protected final int attackInterval; // ticks
  protected int cooldown; // ticks

  protected Cells target;

  private final Vec2D desired = new Vec2D();
  private final Vec2D steer = new Vec2D();
  private final Vec2D acc = new Vec2D();

  protected EnemyAbs(int id, double x, double y, float hpMax, double mass, double rad,
      double velMax, double forceMax, float attackDamage, double attackRange,
      int attackInterval) {
    if (rad < 0 || attackRange < 0 || attackInterval < 1)
      throw new IllegalArgumentException("Parametros de inimigo invalidos.");

    this.id = id;
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

  /**
   * Subclasse deve chamar tickCooldown() todo tick.
   *
   * @return false se o inimigo deve ser removido do mundo
   */
  @Override
  public abstract boolean tick(Base base);

  @Override
  public int id() {
    return id;
  }

  @Override
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

  @Override
  public boolean alive() {
    return hp > 0f;
  }

  @Override
  public double attackRange() {
    return attackRange;
  }

  /**
   * Distancia entre a borda deste inimigo e a borda da celula.
   * Negativa se estiverem sobrepostos.
   */
  public double edgeDistance(Cells c) {
    return Vec2D.dist(pos, c.pos()) - rad - c.rad();
  }

  public double distanceFrom(Vec2D p) {
    return Vec2D.dist(pos, p) - rad;
  }

  @Override
  public boolean inRange(Cells c) {
    return c != null && c.alive() && edgeDistance(c) <= attackRange;
  }

  protected boolean inHoldRange(Cells c) {
    return c != null && c.alive() && edgeDistance(c) <= attackRange * HOLD_FRACTION;
  }

  protected boolean targetValid() {
    return target != null && target.alive();
  }


  @Override
  public boolean canAttack() {
    return alive() && cooldown <= 0;
  }

  @Override
  public float attack() {
    if (!canAttack())
      return 0f;

    cooldown = attackInterval;
    return attackDamage;
  }

  @Override
  public void damage(float amount) {
    hp = Math.max(0f, hp - amount);
  }

  protected void tickCooldown() {
    if (cooldown > 0)
      --cooldown;
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
