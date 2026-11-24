package com.github.betacoders.world;

import java.util.Objects;

import com.github.betacoders.collections.Graph;
import com.github.betacoders.collections.List;
import com.github.betacoders.collections.Vec2D;
import com.github.betacoders.collections.Vector;

public class World {
  private static final int SETTLE_TICKS = 300;

  private final Base base;
  private final Vector<Enemy> enemies;

  private int tick = 0;
  private int wave = 1;
  private int enemiesKilled = 0;
  private boolean over = false;

  public World(Base base) {
    this.base = Objects.requireNonNull(
        base, "A base é obrigatória.");

    this.enemies = new Vector<>();

    for (int i = 0; i < SETTLE_TICKS; i++)
      base.settleOnce();
  }

  public Base base() {
    return base;
  }

  public Graph<Cells, Link> graph() {
    return base.graph();
  }

  public int cellCount() {
    return graph().size();
  }

  public double totalEdgeLength() {
    return base.settledLength();
  }

  public List<Enemy> enemies() {
    return enemies;
  }

  public boolean addEnemy(Enemy enemy) {
    if (enemy == null || !enemy.alive())
      return false;

    return enemies.push(enemy);
  }

  public boolean removeEnemy(Enemy enemy) {
    int index = enemies.indexOf(enemy);

    if (index == -1)
      return false;

    enemies.swapRemove(index);
    return true;
  }

  public boolean tick() {
    if (over()) {
      over = true;
      return false;
    }

    tick++;

    if (!base.tick()) {
      over = true;
      return false;
    }

    for (int i = enemies.size() - 1; i >= 0; i--) {
      Enemy enemy = enemies.get(i);

      if (!enemy.tick(base) || !enemy.alive()) {
        enemies.swapRemove(i);
        continue;
      }

      enemyAttack(enemy);
    }

    if (!base.core().alive()) {
      over = true;
      return false;
    }

    for (Cells cell : base.graph())
      if (cell instanceof Cells.Turret turret && turret.alive())
        turretAttack(turret);

    for (int i = enemies.size() - 1; i >= 0; i--)
      if (!enemies.get(i).alive())
        enemies.swapRemove(i);

    if (!base.core().alive()) {
      over = true;
      return false;
    }

    return true;
  }

  private void turretAttack(Cells.Turret turret) {
    if (!turret.canShoot())
      return;

    Enemy target = nearestEnemy(
        turret.pos(), turret.range());

    if (target == null || !turret.shoot())
      return;

    target.damage(turret.shotDamage());

    if (!target.alive())
      recordEnemyKilled();
  }

  private void enemyAttack(Enemy enemy) {
    if (!enemy.canAttack())
      return;

    Cells target = nearestCell(enemy);

    if (target != null)
      target.damage(enemy.attack());
  }

  private Enemy nearestEnemy(Vec2D from, double range) {
    Enemy best = null;
    double bestDistance = range * range;

    for (int i = 0; i < enemies.size(); i++) {
      Enemy enemy = enemies.get(i);

      if (!enemy.alive())
        continue;

      double distance = Vec2D.distSq(from, enemy.pos());

      if (distance <= bestDistance) {
        bestDistance = distance;
        best = enemy;
      }
    }

    return best;
  }

  private Cells nearestCell(Enemy enemy) {
    Cells best = null;
    double bestDistance = Double.POSITIVE_INFINITY;

    for (Cells cell : base.graph()) {
      if (!enemy.inRange(cell))
        continue;

      double distance = Vec2D.distSq(enemy.pos(), cell.pos());

      if (distance <= bestDistance) {
        bestDistance = distance;
        best = cell;
      }
    }

    return best;
  }

  public int ticks() {
    return tick;
  }

  public int wave() {
    return wave;
  }

  public int wavesCompleted() {
    return wave - 1;
  }

  public int enemiesKilled() {
    return enemiesKilled;
  }

  public boolean over() {
    return over || !base.core().alive();
  }

  public void recordEnemyKilled() {
    if (!over())
      enemiesKilled++;
  }

  public void completeWave() {
    if (!over())
      wave++;
  }
}
