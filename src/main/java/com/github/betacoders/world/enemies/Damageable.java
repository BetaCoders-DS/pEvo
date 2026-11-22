package com.github.betacoders.world.enemies;

import com.github.betacoders.collections.Vec2D;

public interface Damageable {
  boolean alive();
  void damage(float amount);
  double distanceFrom(Vec2D p);
}

