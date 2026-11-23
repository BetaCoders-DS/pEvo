package com.github.betacoders.world;

import java.util.Objects;
import java.util.Random;

import com.github.betacoders.collections.Vec2D;
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

  public interface EnemyFactory {
    Enemy create(int id, double x, double y, int wave, Random rng, WaveConfig config);
  }

  private final World world;
  private final Random rng;
  private final EnemyFactory enemyFactory;
  private final WaveConfig config;

  private int nextEnemyId = 1;
  private int pending;
  private int spawnCooldown;
  private int interWaveCooldown;
  private boolean waveActive;

  public WaveSim(World world, long seed) {
    this(world, seed, WaveConfig.DEFAULT);
  }

  public WaveSim(World world, long seed, WaveConfig config) {
    this(world, seed, WaveSim::createFlyingEnemy, config);
  }

  private static Enemy createFlyingEnemy(
      int id, double x, double y, int wave, Random rng, WaveConfig config) {
    float hp = (float) (config.hpBase() * (1. + config.hpGrowth() * (wave - 1)));
    float damage = (float) (config.attackDamageBase()
        * (1. + config.attackDamageGrowth() * (wave - 1)));

    return new FlyingEnemy(id, x, y, hp, config.rad(), config.velMax(),
        config.forceMax(), damage, config.attackRange(), config.attackInterval());
  }

  public WaveSim(World world, long seed, EnemyFactory enemyFactory, WaveConfig config) {
    this.world = Objects.requireNonNull(world, "World required.");
    this.enemyFactory = Objects.requireNonNull(enemyFactory, "Enemy factory required.");
    this.config = Objects.requireNonNull(config, "Wave config required.");
    this.rng = new Random(seed);
    this.interWaveCooldown = 0;
  }

  public WaveSim(World world, long seed, EnemyFactory enemyFactory) {
    this(world, seed, enemyFactory, WaveConfig.DEFAULT);
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

    if (waveActive && spawnCooldown-- <= 0 && pending > 0)
      spawnEnemy();

    world.tick();

    if (waveActive && pending == 0 && world.enemies().isEmpty()) {
      world.completeWave();
      waveActive = false;
      interWaveCooldown = config.interWaveDelay();
    }
  }

  private void startWave() {
    int count = (int) Math.round(
        config.baseCount() * (1. + config.countGrowth() * (world.wave() - 1)));

    pending = Math.max(1, count);
    spawnCooldown = 0;
    waveActive = true;
  }

  private void spawnEnemy() {
    Vec2D core = world.base().core().pos();
    double radius = config.spawnRadius()
        + config.radiusGrowth() * (world.wave() - 1);
    double angle = rng.nextDouble() * 2. * Math.PI;
    double x = core.x + Math.cos(angle) * radius;
    double y = core.y + Math.sin(angle) * radius;

    Enemy enemy = enemyFactory.create(
        nextEnemyId++, x, y, world.wave(), rng, config);

    if (enemy != null)
      world.addEnemy(enemy);

    --pending;
    spawnCooldown = config.spawnInterval();
  }
}
