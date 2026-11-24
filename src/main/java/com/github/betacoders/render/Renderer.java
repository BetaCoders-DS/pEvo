package com.github.betacoders.render;

import java.awt.RenderingHints;

import com.github.betacoders.collections.Vec2D;
import com.github.betacoders.world.Cells;
import com.github.betacoders.world.Enemy;
import com.github.betacoders.world.Link;
import com.github.betacoders.world.Packet;
import com.github.betacoders.world.World;
import com.github.betacoders.world.enemies.EnemyAbs;
import com.github.betacoders.render.BloonsSprites.Sprite;

import processing.awt.PGraphicsJava2D;
import processing.core.PApplet;
import processing.core.PImage;

public final class Renderer {
  private static final float CELL_CORE_SIZE = 66;
  private static final float CELL_EXTRACTOR_SIZE = 48;
  private static final float CELL_RELAY_SIZE = 50;
  private static final float ENEMY_SIZE = 34;
  private static final float COLOSSAL_SIZE = 94;
  private static final double HOVER_RADIUS_SQ = 45. * 45.;

  private final PApplet g;
  private final double worldW;
  private final double worldH;

  public Renderer(PApplet g, double worldW, double worldH) {
    this.g = g;
    this.worldW = worldW;
    this.worldH = worldH;
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
      g.noStroke();
      g.fill(Theme.ARENA_BG);
      g.rectMode(PApplet.CORNER);
      g.rect(x, y, width, height);
      g.translate(offsetX, offsetY);
      g.scale(scale);
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

  private void drawRange(Cells.Turret turret) {
    g.fill(Theme.RANGE_FILL);
    g.stroke(Theme.RANGE_STROKE);
    g.strokeWeight(2);
    g.ellipse((float) turret.pos().x, (float) turret.pos().y,
        (float) turret.range() * 2, (float) turret.range() * 2);
  }

  private void drawLinks(World world) {
    for (Link link : world.graph().edges()) {
      Vec2D a = link.a().pos();
      Vec2D b = link.b().pos();
      g.stroke(Theme.LINK);
      g.strokeWeight(5);
      g.line((float) a.x, (float) a.y, (float) b.x, (float) b.y);
      g.stroke(link.load() > 0 ? Theme.LINK_LOAD : Theme.LINK_FREE);
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
    drawCell(world, world.base().core(), details, hovered);
  }

  private void drawCell(World world, Cells cell, boolean details, Cells hovered) {
    float x = (float) cell.pos().x;
    float y = (float) cell.pos().y;
    float size = cell instanceof Cells.Core ? CELL_CORE_SIZE
        : cell instanceof Cells.Extractor ? CELL_EXTRACTOR_SIZE : CELL_RELAY_SIZE;

    g.noStroke();
    g.fill(Theme.CELL_SHADOW);
    g.ellipse(x, y + 15, size * 0.9f, size * 0.55f);
    g.noFill();
    g.stroke(cell == hovered ? Theme.PANEL_STROKE_HOVER : cellColor(cell));
    g.strokeWeight(cell instanceof Cells.Core ? 3 : 2);
    g.ellipse(x, y + 3, (float) cell.rad() * 2 + 8, (float) cell.rad() * 2 + 8);

    float angle = 0;
    if (cell instanceof Cells.Turret) {
      Enemy target = nearestEnemy(world, cell.pos());
      if (target != null)
        angle = (float) Math.atan2(target.pos().y - y, target.pos().x - x) - PApplet.HALF_PI;
    }
    if (!cell.alive())
      g.tint(Theme.DEAD_TINT);
    drawSprite(BloonsSprites.cell(cell), x, y, size, angle);
    g.noTint();

    if (cell.hp() < cell.hpMax() || cell instanceof Cells.Core || cell == hovered)
      Bars.health(g, x - 27, y - size / 2 - 8, 54, 5, cell.hp() / cell.hpMax());
    if (details) {
      if (cell instanceof Cells.Turret turret)
        Bars.ammo(g, x, y + size / 2 + 8, turret.stored(), turret.cap());
      if (cell instanceof Cells.Extractor extractor)
        Bars.ammo(g, x, y + size / 2 + 8, extractor.stored(), extractor.cap());
    }
  }

  private int cellColor(Cells cell) {
    if (cell instanceof Cells.Core)
      return Theme.CELL_CORE;
    if (cell instanceof Cells.Turret)
      return Theme.CELL_TURRET;
    if (cell instanceof Cells.Extractor)
      return Theme.CELL_EXTRACTOR;
    return Theme.CELL_RELAY;
  }

  private void drawPackets(World world) {
    g.stroke(Theme.PACKET_STROKE);
    g.strokeWeight(2);
    g.fill(Theme.PACKET_FILL);
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

      Sprite sprite = BloonsSprites.enemy(enemy);
      boolean colossal = sprite == Sprite.MAHORAGA;
      float size = colossal ? COLOSSAL_SIZE : ENEMY_SIZE;
      float x = (float) enemy.pos().x;
      float y = (float) enemy.pos().y;
      float angle = 0;
      if (colossal && enemy instanceof EnemyAbs flying
          && flying.vel().x * flying.vel().x + flying.vel().y * flying.vel().y > 0.01)
        angle = (float) Math.atan2(flying.vel().y, flying.vel().x) + PApplet.PI;

      g.noStroke();
      g.fill(Theme.ENEMY_SHADOW);
      g.ellipse(x + 3, y + size * 0.3f, size * 0.65f, size * 0.3f);
      drawSprite(sprite, x, y, size, angle);
      if (enemy instanceof EnemyAbs damageable && damageable.hp() < damageable.hpMax())
        Bars.health(g, x - size / 2, y - size / 2 - 7, size, 4, damageable.hp() / damageable.hpMax());
      if (details && colossal) {
        g.fill(Theme.COLOSSAL_TEXT);
        g.textAlign(PApplet.CENTER, PApplet.BOTTOM);
        g.textSize(12);
        g.text("COLOSSAL", x, y - size / 2 - 10);
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

  private Cells hoveredCell(World world, float x, float y) {
    Cells selected = null;
    double best = HOVER_RADIUS_SQ;
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
