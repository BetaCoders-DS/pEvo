package com.github.betacoders.evolution;

import com.github.betacoders.world.Simulation;
import com.github.betacoders.world.World;

@FunctionalInterface
public interface SimulationFactory {
    Simulation create(World world, long scenarioSeed);
}