package com.github.betacoders.world;

import com.github.betacoders.collections.Vec2D;

public interface Enemy {
  int id();

  Vec2D pos();

  boolean alive();

  void damage(float amount);

  double attackRange();

  /** Permite que cada tipo de inimigo use a geometria do seu movimento. */
  default boolean inRange(Cells cell) {
    return cell != null && cell.alive()
        && Vec2D.dist(pos(), cell.pos()) <= attackRange();
  }

  boolean canAttack();

  float attack();

  boolean tick(Base base);
}
