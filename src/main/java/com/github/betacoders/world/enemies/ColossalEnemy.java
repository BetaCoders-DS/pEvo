package com.github.betacoders.world.enemies;

public final class ColossalEnemy extends GroundEnemy {
  public static final float HP_MAX = 1500f;
  public static final double MASS = 60.;
  public static final double RAD = 36.;
  public static final double VEL_MAX = 0.5;
  public static final double FORCE_MAX = 1.2;
  public static final float ATTACK_DAMAGE = 60f;
  public static final double ATTACK_RANGE = 20.;
  public static final int ATTACK_INTERVAL = 90;

  public ColossalEnemy(int id, double x, double y) {
    this(id, x, y, StraightNavigator.INSTANCE);
  }

  public ColossalEnemy(int id, double x, double y, GroundNavigator navigator) {
    super(id, x, y, HP_MAX, MASS, RAD, VEL_MAX, FORCE_MAX,
        ATTACK_DAMAGE, ATTACK_RANGE, ATTACK_INTERVAL, navigator);
  }
}