package com.github.betacoders.world;

import com.github.betacoders.collections.Vec2D;

public interface Enemy {
  int id();

  Vec2D pos();

  boolean alive();

  void damage(float amount);

  double attackRange();

  boolean canAttack();

  float attack();

  boolean tick(Base base);
}
