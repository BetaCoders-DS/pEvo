package com.github.betacoders.world;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.github.betacoders.collections.AdjacencyGraph;
import com.github.betacoders.world.WaveSim.WaveConfig;
import com.github.betacoders.world.enemies.FlyingEnemy;

class WaveSimTest {
    @Test
    void createsFlyingEnemiesUsingTheWaveConfiguration() {
        World world = coreWorld();
        WaveConfig config = new WaveConfig(
            2, 0.5, 3, 1, 320, 12,
            40f, 0.25, 8f, 0.2, 26, 30, 14, 4, 3
        );
        WaveSim waves = new WaveSim(world, 42, config);
        waves.tick();

        FlyingEnemy first = (FlyingEnemy) world.enemies().get(0);
        assertEquals(1, first.id());
        assertEquals(40f, first.hpMax());
        assertEquals(14, first.rad());
        assertEquals(26, first.attackRange());
        assertEquals(8f, first.attack());
        assertTrue(first.vel().x * first.vel().x + first.vel().y * first.vel().y <= 16);

        first.damage(Float.MAX_VALUE);
        waves.tick();
        assertEquals(0, world.wavesCompleted(), "Pending enemies keep the wave active.");

        for (int i = 0; i < 10 && world.enemies().isEmpty(); i++) {
            waves.tick();
        }
        assertEquals(1, world.enemies().size());
        assertEquals(2, world.enemies().get(0).id());
        assertEquals(0, world.wavesCompleted());
        world.enemies().get(0).damage(Float.MAX_VALUE);
        waves.tick();
        assertEquals(1, world.wavesCompleted());

        for (int i = 0; i < 10 && world.enemies().isEmpty(); i++) {
            waves.tick();
        }
        FlyingEnemy nextWave = (FlyingEnemy) world.enemies().get(0);
        assertEquals(3, nextWave.id());
        assertEquals(50f, nextWave.hpMax());
        assertEquals(9.6f, nextWave.attack(), 0.0001f);
    }

    @Test
    void stopsSpawningAndAdvancingAfterDefeat() {
        World world = coreWorld();
        WaveSim waves = new WaveSim(world, 42);
        waves.tick();
        world.base().core().damage(Float.MAX_VALUE);
        for (int i = 0; i < 50; i++) {
            waves.tick();
        }
        assertEquals(1, world.ticks());
        assertEquals(1, world.enemies().size());
        assertEquals(0, world.wavesCompleted());
    }

    @Test
    void attacksUsingTheSameEdgeDistanceAsMovement() {
        World world = coreWorld();
        Cells.Core core = world.base().core();
        FlyingEnemy enemy = new FlyingEnemy(1, 149, 100);
        world.addEnemy(enemy);
        assertTrue(enemy.inRange(core));

        world.tick();
        assertEquals(core.hpMax() - FlyingEnemy.ATTACK_DAMAGE, core.hp());
        for (int i = 0; i < FlyingEnemy.ATTACK_INTERVAL - 1; i++) {
            world.tick();
        }
        assertEquals(core.hpMax() - FlyingEnemy.ATTACK_DAMAGE, core.hp());
        world.tick();
        assertEquals(core.hpMax() - 2 * FlyingEnemy.ATTACK_DAMAGE, core.hp());
    }

    @Test
    void allowsContactAttacksWithZeroRange() {
        World world = coreWorld();
        world.addEnemy(new FlyingEnemy(
            1, 140, 100, 40, 14, 4, 3, 8, 0, 30
        ));
        world.tick();
        assertEquals(992f, world.base().core().hp());
    }

    @Test
    void doesNotAttackCellsOutsideItsRange() {
        World world = coreWorld();
        world.addEnemy(new FlyingEnemy(1, 300, 100));
        world.tick();
        assertEquals(1000f, world.base().core().hp());
    }

    private static World coreWorld() {
        AdjacencyGraph<Cells, Link> graph = new AdjacencyGraph<>();
        graph.addVertex(new Cells.Core(0, 100, 100));
        return new World(new Base(graph));
    }
}
