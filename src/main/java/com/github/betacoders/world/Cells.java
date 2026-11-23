package com.github.betacoders.world;

import com.github.betacoders.collections.Vec2D;

/**
 * Cell
 */
public sealed abstract class Cells {
  protected final int id;

  protected float hp;
  protected final float hpMax;
  protected Vec2D pos;
  protected final float mass; // grams
  protected final double rad; // px

  protected static final float NORMAL_MASS = 100.f;
  protected static final float NORMAL_HP = 100.f;
  protected static final double NORMAL_RAD = 25.;

  protected Cells(int id, float hpMax, Vec2D pos, float mass, double radius) {
    this.id = id;
    this.hp = hpMax;
    this.hpMax = hpMax;
    this.pos = pos;
    this.mass = mass;
    this.rad = radius;
  }

  public int id() {
    return id;
  }

  public float hp() {
    return hp;
  }

  public float hpMax() {
    return hpMax;
  }

  public Vec2D pos() {
    return pos;
  }

  public float mass() {
    return mass;
  }

  public double rad() {
    return rad;
  }

  public boolean alive() {
    return hp > 0f;
  }

  public void damage(float amount) {
    hp = Math.max(0f, hp - amount);
  }

  public boolean tick() {
    return this.alive();
  }

  public static final class Core extends Cells {
    public Core(int id, double x, double y) {
      super(
          id,
          NORMAL_HP * 10,
          new Vec2D(x, y),
          NORMAL_MASS * 10,
          NORMAL_RAD * 1.25);
    }
  }

  public static final class Relay extends Cells {
    public Relay(int id, double x, double y) {
      super(id,
          NORMAL_HP,
          new Vec2D(x, y),
          NORMAL_MASS,
          NORMAL_RAD);
    }
  }

  public static final class Extractor extends Cells {
    private final int cap;
    private int stored;
    private int cooldown;
    private final int interval;

    public Extractor(int id, double x, double y, int cap, int interval) {
      super(id,
          NORMAL_HP,
          new Vec2D(x, y),
          NORMAL_MASS * 2,
          NORMAL_RAD * 1.1);

      this.cap = cap;
      this.stored = 0;
      this.interval = interval;
      this.cooldown = interval;
    }

    public int cap() {
      return cap;
    }

    public int stored() {
      return stored;
    }

    @Override
    public boolean tick() {
      if (!alive())
        return false;

      if (stored < cap)

        if (--cooldown <= 0) {
          cooldown = interval;
          ++stored;
        }

      return true;
    }

    public boolean consume() {
      if (stored <= 0)
        return false;

      --stored;
      return true;
    }
  }

  public static final class Turret extends Cells {
    private final int cap;
    private int stored;
    private final double range; // px
    private final float shotDamage;
    private final int shotInterval; // ticks
    private int cooldown; // ticks

    public Turret(int id, double x, double y, int cap, double range, float fireDamage, int fireInterval) {
      super(id,
          NORMAL_HP * 0.8f,
          new Vec2D(x, y),
          NORMAL_MASS * 0.6f,
          NORMAL_RAD * 0.8);

      this.cap = cap;
      this.stored = 0;
      this.range = range;
      this.shotDamage = fireDamage;
      this.shotInterval = fireInterval;
      this.cooldown = 0;
    }

    public int cap() {
      return cap;
    }

    public int stored() {
      return stored;
    }

    public double range() {
      return range;
    }

    public float shotDamage() {
      return shotDamage;
    }

    public boolean receiveAmmo() {
      if (stored >= cap)
        return false;

      ++stored;
      return true;
    }

    public boolean canShoot() {
      return stored > 0 && cooldown <= 0;
    }

    @Override
    public boolean tick() {
      if (!alive())
        return false;

      if (cooldown > 0)
        --cooldown;

      return true;
    }

    public boolean shoot() {
      if (!canShoot())
        return false;

      --stored;
      cooldown = shotInterval;
      return true;
    }
  }
}
