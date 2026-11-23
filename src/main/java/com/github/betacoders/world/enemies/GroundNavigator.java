package com.github.betacoders.world.enemies;

import com.github.betacoders.collections.Vec2D;
import com.github.betacoders.world.Base;
import com.github.betacoders.world.Cells;

public interface GroundNavigator {
  Cells navigate(GroundEnemy enemy, Base base, Vec2D outWaypoint);
}
