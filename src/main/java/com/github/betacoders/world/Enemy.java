package com.github.betacoders.world;

import com.github.betacoders.collections.Vec2D;

public interface Enemy {
  int id();

  Vec2D pos();

  boolean alive();

  void damage(float amount);

  default float attackDamage() {
    return 0f;
  }

  default double attackRange() {
    return 0.;
  }

  boolean tick();
}
