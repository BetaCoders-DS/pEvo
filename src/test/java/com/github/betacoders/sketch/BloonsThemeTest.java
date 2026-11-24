package com.github.betacoders.sketch;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.github.betacoders.Demo.BloonsSprites;
import com.github.betacoders.Demo.BloonsSprites.Sprite;
import com.github.betacoders.Demo.SceneCamera;
import com.github.betacoders.collections.AdjacencyGraph;
import com.github.betacoders.world.Base;
import com.github.betacoders.world.Cells;
import com.github.betacoders.world.Link;
import com.github.betacoders.world.World;
import com.github.betacoders.world.enemies.FlyingEnemy;

import processing.core.PImage;

class BloonsThemeTest {
    @Test
    void everyBundledSpriteLoadsWithTransparencyAndSharesItsCache() {
        for (Sprite sprite : Sprite.values()) {
            PImage image = BloonsSprites.image(sprite);
            assertNotNull(image);
            assertTrue(image.width > 0 && image.height > 0);
            assertSame(image, BloonsSprites.image(sprite));
            image.loadPixels();
            boolean transparent = false;
            boolean visible = false;
            for (int pixel : image.pixels) {
                transparent |= (pixel >>> 24) == 0;
                visible |= (pixel >>> 24) > 0;
            }
            assertTrue(transparent, sprite + " must not have a solid background.");
            assertTrue(visible, sprite + " must contain visible artwork.");
        }
    }

    @Test
    void cameraIncludesEnemiesOutsideTheOriginalMapWithoutChangingTheWorld() {
        AdjacencyGraph<Cells, Link> graph = new AdjacencyGraph<>();
        graph.addVertex(new Cells.Core(0, 500, 350));
        World world = new World(new Base(graph));
        FlyingEnemy enemy = new FlyingEnemy(1, -300, 1200);
        world.addEnemy(enemy);

        SceneCamera camera = SceneCamera.fit(world, 1000, 700);
        assertTrue(camera.centerX() - camera.width() / 2 < enemy.pos().x);
        assertTrue(camera.centerY() + camera.height() / 2 > enemy.pos().y);
        assertTrue(camera.centerX() + camera.width() / 2 > world.base().core().pos().x);
        assertTrue(camera.centerY() - camera.height() / 2 < world.base().core().pos().y);
        assertEquals(0, world.ticks());
        assertEquals(-300, enemy.pos().x);
        assertEquals(1200, enemy.pos().y);
    }

    @Test
    void cameraMakesASmallBaseLargerThanTheWholeMapView() {
        AdjacencyGraph<Cells, Link> graph = new AdjacencyGraph<>();
        graph.addVertex(new Cells.Core(0, 500, 350));
        SceneCamera camera = SceneCamera.fit(new World(new Base(graph)), 1000, 700);
        assertTrue(camera.width() < 1000);
        assertTrue(camera.height() < 700);
        assertTrue(camera.width() > 0 && camera.height() > 0);
    }
}
