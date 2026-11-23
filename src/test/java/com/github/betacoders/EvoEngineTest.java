package com.github.betacoders;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.github.betacoders.evolution.EvolutionConfig;
import com.github.betacoders.evolution.SimulationResult;
import com.github.betacoders.world.Enemy;
import com.github.betacoders.world.World;
import com.github.betacoders.world.enemies.FlyingEnemy;

class EvoEngineTest {
    private static final EvolutionConfig CONFIG = new EvolutionConfig(
        4, 800, 600, 3, 6, 5, 0, 3, 12
    );

    @Test
    void advancesEveryIndependentWorldWithRealEnemies() {
        EvoEngine engine = new EvoEngine(CONFIG);
        engine.restart(2026);

        World first = engine.worldAt(0);
        World second = engine.worldAt(1);
        assertNotSame(first, second);
        assertNotSame(first.graph(), second.graph());
        assertNotSame(first.base().core(), second.base().core());

        engine.step();

        for (int i = 0; i < engine.populationSize(); i++) {
            World world = engine.worldAt(i);
            assertEquals(1, world.ticks());
            assertEquals(1, world.enemies().size());
            assertInstanceOf(FlyingEnemy.class, world.enemies().get(0));
        }

        Enemy firstEnemy = first.enemies().get(0);
        Enemy secondEnemy = second.enemies().get(0);
        assertNotSame(firstEnemy, secondEnemy);
        firstEnemy.damage(Float.MAX_VALUE);
        first.base().core().damage(Float.MAX_VALUE);
        engine.step();

        assertEquals(1, first.ticks(), "A defeated world must stop advancing.");
        assertEquals(2, second.ticks());
        assertTrue(secondEnemy.alive());
        assertFalse(second.over());
    }

    @Test
    void reproducesThePopulationAndWavesWhenRestartedWithTheSameSeed() {
        EvoEngine engine = new EvoEngine(CONFIG);
        engine.restart(91);
        for (int i = 0; i < 80; i++) {
            engine.step();
        }
        World original = engine.worldAt(0);
        long scenarioSeed = engine.scenarioSeed();

        engine.restart(91);
        assertEquals(91, engine.seed());
        assertEquals(scenarioSeed, engine.scenarioSeed());
        assertNotSame(original, engine.worldAt(0));
        assertEquals(0, engine.worldAt(0).ticks());

        for (int i = 0; i < 80; i++) {
            engine.step();
        }

        World replay = engine.worldAt(0);
        assertEquals(original.cellCount(), replay.cellCount());
        assertEquals(original.totalEdgeLength(), replay.totalEdgeLength());
        assertEquals(original.base().core().hp(), replay.base().core().hp());
        assertEquals(original.enemies().size(), replay.enemies().size());
        for (int i = 0; i < original.enemies().size(); i++) {
            Enemy expected = original.enemies().get(i);
            Enemy actual = replay.enemies().get(i);
            assertEquals(expected.id(), actual.id());
            assertEquals(expected.pos().x, actual.pos().x);
            assertEquals(expected.pos().y, actual.pos().y);
        }
    }

    @Test
    void realCombatFinishesAGenerationAndStartsFreshWorlds() {
        EvoEngine engine = new EvoEngine(CONFIG);
        engine.restart(2026);
        World[] originals = new World[engine.populationSize()];
        for (int i = 0; i < originals.length; i++) {
            originals[i] = engine.worldAt(i);
        }
        long scenarioSeed = engine.scenarioSeed();

        for (int i = 0; i < 20_000 && !engine.generationFinished(); i++) {
            engine.step();
        }

        assertTrue(engine.generationFinished(), "Real enemies must defeat the population.");
        assertEquals(1, engine.generation());
        engine.step();

        assertEquals(2, engine.generation());
        assertEquals(scenarioSeed, engine.scenarioSeed());
        SimulationResult[] ranking = engine.previousRanking();
        assertEquals(originals.length, ranking.length);
        for (SimulationResult result : ranking) {
            World original = originals[result.individualId()];
            assertTrue(original.over());
            assertEquals(original.ticks(), result.survivalTicks());
            assertEquals(original.wavesCompleted(), result.wavesCompleted());
            assertEquals(original.enemiesKilled(), result.enemiesKilled());
        }
        for (int i = 0; i < originals.length; i++) {
            World next = engine.worldAt(i);
            assertNotSame(originals[i], next);
            assertEquals(1, next.ticks());
            assertFalse(next.over());
            assertEquals(1, next.enemies().get(0).id());
        }

        engine.restart(2026);
        assertEquals(1, engine.generation());
        assertEquals(0, engine.previousRanking().length);
        assertEquals(0, engine.worldAt(0).ticks());
    }

    @Test
    void rejectsInvalidIndividualIndices() {
        EvoEngine engine = new EvoEngine(CONFIG);
        assertThrows(IndexOutOfBoundsException.class, () -> engine.worldAt(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> engine.worldAt(4));
    }
}
