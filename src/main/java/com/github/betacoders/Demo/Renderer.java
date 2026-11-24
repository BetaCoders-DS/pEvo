package com.github.betacoders.Demo;

import java.awt.RenderingHints;

import com.github.betacoders.Demo.BloonsSprites.Sprite;
import com.github.betacoders.collections.Vec2D;
import com.github.betacoders.world.Cells;
import com.github.betacoders.world.Enemy;
import com.github.betacoders.world.Link;
import com.github.betacoders.world.Packet;
import com.github.betacoders.world.World;
import com.github.betacoders.world.enemies.EnemyAbs;

import processing.awt.PGraphicsJava2D;
import processing.core.PApplet;
import processing.core.PImage;

/** Renderiza a arena e os sprites top down sem alterar o estado dos mundos. */
public final class Renderer {
  private final PApplet g;
  private final double worldW;
  private final double worldH;

  public Renderer(PApplet g, double worldW, double worldH) {
    this.g = g;
    this.worldW = worldW;
    this.worldH = worldH;
  }

  public void draw(World world) {
    draw(world, 0, 0, g.width, g.height, false);
  }

  public void draw(World world, float x, float y, float width, float height) {
    draw(world, x, y, width, height, false);
  }

  public void draw(World world, float x, float y, float width, float height, boolean details) {
    if (width <= 0 || height <= 0)
      return;
    SceneCamera camera = SceneCamera.fit(world, worldW, worldH);
    float scale = Math.min(width / camera.width(), height / camera.height());
    float offsetX = x + width / 2 - camera.centerX() * scale;
    float offsetY = y + height / 2 - camera.centerY() * scale;
    float mouseWorldX = (g.mouseX - offsetX) / scale;
    float mouseWorldY = (g.mouseY - offsetY) / scale;
    boolean inside = g.mouseX >= x && g.mouseX < x + width
        && g.mouseY >= y && g.mouseY < y + height;
    Cells hovered = details && inside ? hoveredCell(world, mouseWorldX, mouseWorldY) : null;
    PGraphicsJava2D java2d = g.g instanceof PGraphicsJava2D graphics ? graphics : null;
    RenderingHints renderingHints = java2d == null ? null : java2d.g2.getRenderingHints();

    g.pushStyle();
    g.pushMatrix();
    g.imageMode(PApplet.CORNER);
    g.clip(x, y, width, height);
    try {
      if (java2d != null) {
        java2d.g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
      }
      // O terreno cobre também as margens do enquadramento, sem faixas vazias.
      g.noStroke();
      g.fill(124, 166, 79);
      g.rectMode(PApplet.CORNER);
      g.rect(x, y, width, height);
      g.translate(offsetX, offsetY);
      g.scale(scale);
      drawTerrain(camera);
      if (hovered instanceof Cells.Turret turret)
        drawRange(turret);
      drawLinks(world);
      drawCells(world, details, hovered);
      drawPackets(world);
      drawEnemies(world, details);
    } finally {
      g.popMatrix();
      g.noClip();
      g.popStyle();
      if (java2d != null) {
        java2d.g2.setRenderingHints(renderingHints);
      }
    }
  }

  private void drawTerrain(SceneCamera camera) {
    g.noStroke();
    int minX = (int) Math.floor((camera.centerX() - camera.width()) / 80) * 80;
    int maxX = (int) Math.ceil((camera.centerX() + camera.width()) / 80) * 80;
    int minY = (int) Math.floor((camera.centerY() - camera.height()) / 80) * 80;
    int maxY = (int) Math.ceil((camera.centerY() + camera.height()) / 80) * 80;
    for (int y = minY; y < maxY; y += 80) {
      for (int x = minX; x < maxX; x += 80) {
        if (Math.floorMod(x / 80 + y / 80, 2) == 0) {
          g.fill(132, 173, 84);
          g.rect(x, y, 80, 80);
        }
        g.stroke(99, 145, 65, 100);
        g.strokeWeight(2);
        g.line(x + 23, y + 39, x + 26, y + 32);
        g.line(x + 26, y + 39, x + 31, y + 35);
        g.noStroke();
      }
    }
  }

  private void drawRange(Cells.Turret turret) {
    g.fill(255, 255, 230, 22);
    g.stroke(255, 251, 204, 170);
    g.strokeWeight(2);
    g.ellipse((float) turret.pos().x, (float) turret.pos().y,
        (float) turret.range() * 2, (float) turret.range() * 2);
  }

  private void drawLinks(World world) {
    for (Link link : world.graph().edges()) {
      Vec2D a = link.a().pos();
      Vec2D b = link.b().pos();
      g.stroke(49, 76, 40, 165);
      g.strokeWeight(5);
      g.line((float) a.x, (float) a.y, (float) b.x, (float) b.y);
      g.stroke(link.load() > 0 ? g.color(255, 207, 76) : g.color(212, 225, 145, 190));
      g.strokeWeight(2);
      g.line((float) a.x, (float) a.y, (float) b.x, (float) b.y);
    }
  }

  private void drawCells(World world, boolean details, Cells hovered) {
    for (Cells cell : world.graph()) {
      if (cell instanceof Cells.Core || !cell.alive())
        continue;
      drawCell(world, cell, details, hovered);
    }
    // A sede permanece identificável mesmo em bases muito agrupadas.
    drawCell(world, world.base().core(), details, hovered);
  }

  private void drawCell(World world, Cells cell, boolean details, Cells hovered) {
    float x = (float) cell.pos().x;
    float y = (float) cell.pos().y;
    float size = cell instanceof Cells.Core ? 66 : cell instanceof Cells.Extractor ? 48 : 50;
    g.noStroke();
    g.fill(38, 65, 35, 65);
    g.ellipse(x, y + 15, size * 0.9f, size * 0.55f);
    g.noFill();
    g.stroke(cell == hovered ? g.color(255, 251, 206) : cellColor(cell));
    g.strokeWeight(cell instanceof Cells.Core ? 3 : 2);
    g.ellipse(x, y + 3, (float) cell.rad() * 2 + 8, (float) cell.rad() * 2 + 8);

    float angle = 0;
    if (cell instanceof Cells.Turret) {
      Enemy target = nearestEnemy(world, cell.pos());
      if (target != null) {
        angle = (float) Math.atan2(target.pos().y - y, target.pos().x - x) - PApplet.HALF_PI;
      }
    }
    if (!cell.alive())
      g.tint(160, 160);
    drawSprite(BloonsSprites.cell(cell), x, y, size, angle);
    g.noTint();
    if (cell.hp() < cell.hpMax() || cell instanceof Cells.Core || cell == hovered) {
      drawHealth(x - 27, y - size / 2 - 8, 54, 5, cell.hp() / cell.hpMax());
    }
    if (details) {
      if (cell instanceof Cells.Turret turret)
        drawAmmo(x, y + size / 2 + 8, turret.stored(), turret.cap());
      if (cell instanceof Cells.Extractor extractor)
        drawAmmo(x, y + size / 2 + 8, extractor.stored(), extractor.cap());
    }
  }

  private int cellColor(Cells cell) {
    if (cell instanceof Cells.Core)
      return g.color(255, 225, 80);
    if (cell instanceof Cells.Turret)
      return g.color(244, 128, 72);
    if (cell instanceof Cells.Extractor)
      return g.color(255, 241, 164);
    return g.color(128, 212, 248);
  }

  private void drawPackets(World world) {
    g.stroke(108, 68, 24);
    g.strokeWeight(2);
    g.fill(255, 225, 78);
    g.rectMode(PApplet.CENTER);
    for (Packet packet : world.base().packets()) {
      Vec2D pos = packet.pos();
      g.pushMatrix();
      g.translate((float) pos.x, (float) pos.y);
      g.rotate(PApplet.QUARTER_PI);
      g.rect(0, 0, 9, 9);
      g.popMatrix();
    }
    g.rectMode(PApplet.CORNER);
  }

  private void drawEnemies(World world, boolean details) {
    for (Enemy enemy : world.enemies()) {
      if (!enemy.alive())
        continue;
      Sprite sprite = BloonsSprites.enemy(world.wave(), enemy.id());
      boolean moab = sprite == Sprite.MOAB;
      float size = moab ? 94 : 34;
      float x = (float) enemy.pos().x;
      float y = (float) enemy.pos().y;
      float angle = 0;
      if (moab && enemy instanceof EnemyAbs flying
          && flying.vel().x * flying.vel().x + flying.vel().y * flying.vel().y > 0.01) {
        angle = (float) Math.atan2(flying.vel().y, flying.vel().x) + PApplet.PI;
      }
      g.noStroke();
      g.fill(35, 59, 37, 60);
      g.ellipse(x + 3, y + size * 0.3f, size * 0.65f, size * 0.3f);
      drawSprite(sprite, x, y, size, angle);
      if (enemy instanceof EnemyAbs damageable && damageable.hp() < damageable.hpMax()) {
        drawHealth(x - size / 2, y - size / 2 - 7, size, 4,
            damageable.hp() / damageable.hpMax());
      }
      if (details && moab) {
        g.fill(22, 49, 65);
        g.textAlign(PApplet.CENTER, PApplet.BOTTOM);
        g.textSize(12);
        g.text("MOAB", x, y - size / 2 - 10);
      }
    }
  }

  private void drawSprite(Sprite sprite, float x, float y, float size, float angle) {
    PImage image = BloonsSprites.image(sprite);
    float scale = size / Math.max(image.width, image.height);
    g.pushMatrix();
    g.translate(x, y);
    g.rotate(angle);
    g.imageMode(PApplet.CENTER);
    g.image(image, 0, 0, image.width * scale, image.height * scale);
    g.popMatrix();
  }

  private void drawAmmo(float x, float y, int stored, int capacity) {
    g.stroke(83, 72, 29);
    g.strokeWeight(1);
    for (int i = 0; i < capacity; i++) {
      g.fill(i < stored ? g.color(255, 228, 84) : g.color(77, 109, 53));
      g.rect(x - capacity * 4 + i * 8, y, 6, 5, 1);
    }
  }

  private void drawHealth(float x, float y, float width, float height, float fraction) {
    g.rectMode(PApplet.CORNER);
    g.stroke(39, 59, 32);
    g.strokeWeight(1);
    g.fill(39, 59, 32);
    g.rect(x - 1, y - 1, width + 2, height + 2, 2);
    g.noStroke();
    g.fill(fraction < 0.3f ? g.color(241, 89, 67) : g.color(169, 239, 88));
    g.rect(x, y, width * Math.max(0, Math.min(1, fraction)), height, 1);
  }

  private Cells hoveredCell(World world, float x, float y) {
    Cells selected = null;
    double best = 45 * 45;
    for (Cells cell : world.graph()) {
      double dx = cell.pos().x - x;
      double dy = cell.pos().y - y;
      if (cell.alive() && dx * dx + dy * dy < best) {
        best = dx * dx + dy * dy;
        selected = cell;
      }
    }
    return selected;
  }

  private Enemy nearestEnemy(World world, Vec2D from) {
    Enemy nearest = null;
    double best = Double.POSITIVE_INFINITY;
    for (Enemy enemy : world.enemies()) {
      double distance = Vec2D.distSq(from, enemy.pos());
      if (enemy.alive() && distance < best) {
        best = distance;
        nearest = enemy;
      }
    }
    return nearest;
  }
}
