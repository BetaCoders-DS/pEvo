package com.github.betacoders.world;

public interface Simulation {
  World world();

  default Base base() {
    return world().base();
  }

  void tick();
}
