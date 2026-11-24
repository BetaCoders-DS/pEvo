package com.github.betacoders.Demo;

import com.github.betacoders.world.Cells;
import com.github.betacoders.world.Enemy;
import com.github.betacoders.world.World;

/** Enquadra os elementos reais da partida para aproveitar melhor as miniaturas. */
public record SceneCamera(float centerX, float centerY, float width, float height) {
    public static SceneCamera fit(World world, double worldWidth, double worldHeight) {
        double minX = world.base().core().pos().x;
        double maxX = minX;
        double minY = world.base().core().pos().y;
        double maxY = minY;
        for (Cells cell : world.graph()) {
            if (!cell.alive()) continue;
            minX = Math.min(minX, cell.pos().x - cell.rad());
            maxX = Math.max(maxX, cell.pos().x + cell.rad());
            minY = Math.min(minY, cell.pos().y - cell.rad());
            maxY = Math.max(maxY, cell.pos().y + cell.rad());
        }
        for (Enemy enemy : world.enemies()) {
            if (!enemy.alive()) continue;
            minX = Math.min(minX, enemy.pos().x - 40);
            maxX = Math.max(maxX, enemy.pos().x + 40);
            minY = Math.min(minY, enemy.pos().y - 40);
            maxY = Math.max(maxY, enemy.pos().y + 40);
        }
        return new SceneCamera((float) ((minX + maxX) / 2), (float) ((minY + maxY) / 2),
            (float) Math.max(Math.min(worldWidth, 420), maxX - minX + 100),
            (float) Math.max(Math.min(worldHeight, 300), maxY - minY + 100));
    }
}
