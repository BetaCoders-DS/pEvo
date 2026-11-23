package com.github.betacoders.world.enemies;

import com.github.betacoders.collections.Vec2D;
import com.github.betacoders.world.Base;
import com.github.betacoders.world.Cells;

/**
 * Inimigo terrestre.
 *
 * - Vai ate o nucleo; arestas e celulas no caminho sao obstaculos.
 * - Quem decide o caminho e o GroundNavigator (hoje StraightNavigator:
 *   linha reta e quebra a parede; no futuro, flow field com BFS).
 * - Nao ataca aqui: o World chama canAttack()/attack() depois do tick.
 * - Recalcula a navegacao so quando o grafo muda (celula morreu) ou o
 *   bloqueio atual morreu.
 */
public final class GroundEnemy extends EnemyAbs {
  public static final float HP_MAX = 80f;
  public static final double MASS = 1.;
  public static final double RAD = 8.; // px
  public static final double VEL_MAX = 1.2; // px/tick
  public static final double FORCE_MAX = 0.3;
  public static final float ATTACK_DAMAGE = 10f;
  public static final double ATTACK_RANGE = 6.; // px, borda a borda
  public static final int ATTACK_INTERVAL = 45; // ticks

  private final GroundNavigator navigator;
  private final Vec2D waypoint = new Vec2D();

  private Cells blocker;
  private int lastGraphSize = -1;
  private int recomputations;

  public GroundEnemy(int id, double x, double y) {
    this(id, x, y, StraightNavigator.INSTANCE);
  }

  public GroundEnemy(int id, double x, double y, GroundNavigator navigator) {
    super(id, x, y, HP_MAX, MASS, RAD, VEL_MAX, FORCE_MAX,
        ATTACK_DAMAGE, ATTACK_RANGE, ATTACK_INTERVAL);

    if (navigator == null)
      throw new IllegalArgumentException("Navegador obrigatorio.");

    this.navigator = navigator;
  }

  @Override
  public boolean flying() {
    return false;
  }

  @Override
  public boolean tick(Base base) {
    if (!alive())
      return false;

    tickCooldown();

    if (needsRecompute(base)) {
      blocker = navigator.navigate(this, base, waypoint);
      lastGraphSize = base.graph().size();
      ++recomputations;
    }

    target = blocker != null ? blocker : base.core();

    if (inHoldRange(target))
      stop();
    else
      seek(waypoint.x, waypoint.y);

    integrate();
    return true;
  }

  /**
   * Celulas so sao removidas durante a simulacao (e levam suas arestas),
   * entao mudanca no numero de vertices = grafo mudou.
   */
  private boolean needsRecompute(Base base) {
    return lastGraphSize != base.graph().size()
        || (blocker != null && !blocker.alive());
  }

  /** Celula que esta sendo quebrada, ou null se o caminho esta livre. */
  public Cells blocker() {
    return blocker;
  }

  /** Quantas vezes a navegacao foi recalculada (para testes/depuracao). */
  public int recomputations() {
    return recomputations;
  }
}
