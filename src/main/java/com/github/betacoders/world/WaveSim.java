package com.github.betacoders.world;

import java.util.Objects;
import java.util.Random;

import com.github.betacoders.collections.List;
import com.github.betacoders.collections.Vec2D;
import com.github.betacoders.collections.Vector;
import com.github.betacoders.world.enemies.FlyingEnemy;

public class WaveSim implements Simulation {
  public record WaveConfig(
      int baseCount,
      double countGrowth,
      int spawnInterval,
      int interWaveDelay,
      double spawnRadius,
      double radiusGrowth,
      float hpBase,
      double hpGrowth,
      float attackDamageBase,
      double attackDamageGrowth,
      double attackRange,
      int attackInterval,
      double rad,
      double velMax,
      double forceMax) {

    public static final WaveConfig DEFAULT = new WaveConfig(
        2, 0.5, 12, 40, 320., 12.,
        40.f, 0.25, 8.f, 0.2,
        26., 30, 14., 45., 300.);

    public WaveConfig {
      if (baseCount < 1
          || countGrowth < 0.
          || spawnInterval < 1
          || interWaveDelay < 0
          || spawnRadius <= 0.
          || radiusGrowth < 0.
          || hpBase <= 0f
          || hpGrowth < 0.
          || attackDamageBase < 0f
          || attackDamageGrowth < 0.
          || attackRange < 0.
          || attackInterval < 1
          || rad <= 0.
          || velMax <= 0.
          || forceMax <= 0.) {
        throw new IllegalArgumentException(
            "Invalid wave configuration.");
      }
    }
  }

  public interface WaveFactory {
    List<Enemy> create(int wave, Random rng, WaveConfig config, Vec2D core, int firstId);
  }

  private final World world;
  private final Random rng;
  private final WaveFactory waveFactory;
  private final WaveConfig config;

  private final Vector<Enemy> pending = new Vector<>();
  private int nextEnemyId = 1;
  private int spawnCooldown;
  private int interWaveCooldown;
  private boolean waveActive;

  public WaveSim(World world, long seed) {
    this(world, seed, WaveConfig.DEFAULT);
  }

  public WaveSim(World world, long seed, WaveConfig config) {
    this(world, seed, WaveSim::createDefaultWave, config);
  }

  public WaveSim(World world, long seed, WaveFactory waveFactory) {
    this(world, seed, waveFactory, WaveConfig.DEFAULT);
  }

  public WaveSim(World world, long seed, WaveFactory waveFactory, WaveConfig config) {
    this.world = Objects.requireNonNull(world, "World required.");
    this.waveFactory = Objects.requireNonNull(waveFactory, "Wave factory required.");
    this.config = Objects.requireNonNull(config, "Wave config required.");
    this.rng = new Random(seed);
    this.interWaveCooldown = 0;
  }

  private static List<Enemy> createDefaultWave(
      int wave, Random rng, WaveConfig config, Vec2D core, int firstId) {
    List<Enemy> enemies = new Vector<>();
    int count = Math.max(1, (int) Math.round(
        config.baseCount() * (1. + config.countGrowth() * (wave - 1))));
    double radius = config.spawnRadius()
        + config.radiusGrowth() * (wave - 1);

    for (int i = 0; i < count; ++i) {
      double angle = rng.nextDouble() * 2. * Math.PI;
      double x = core.x + Math.cos(angle) * radius;
      double y = core.y + Math.sin(angle) * radius;

      enemies.add(new FlyingEnemy(firstId + i, x, y));
    }

    return enemies;
  }

  @Override
  public World world() {
    return world;
  }

  @Override
  public void tick() {
    if (world.over())
      return;

    if (!waveActive && interWaveCooldown-- <= 0)
      startWave();

    if (waveActive && spawnCooldown-- <= 0 && !pending.isEmpty())
      spawnEnemy();

    world.tick();

    if (waveActive && pending.isEmpty() && world.enemies().isEmpty()) {
      world.completeWave();
      waveActive = false;
      interWaveCooldown = config.interWaveDelay();
    }
  }

  private void startWave() {
    List<Enemy> created = waveFactory.create(
        world.wave(), rng, config, world.base().core().pos(), nextEnemyId);

    pending.clear();
    if (created != null)
      pending.addAll(created);

    nextEnemyId += pending.size();
    spawnCooldown = 0;
    waveActive = true;
  }

  private void spawnEnemy() {
    Enemy enemy = pending.get(0);
    pending.swapRemove(0);

    if (enemy != null)
      world.addEnemy(enemy);

    spawnCooldown = config.spawnInterval();
  }
}
