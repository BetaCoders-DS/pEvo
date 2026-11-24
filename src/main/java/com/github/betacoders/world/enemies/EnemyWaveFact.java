package com.github.betacoders.world.enemies;

import java.util.Random;

import com.github.betacoders.collections.List;
import com.github.betacoders.collections.Vector;
import com.github.betacoders.world.Enemy;
import com.github.betacoders.world.WaveSim.WaveConfig;
import com.github.betacoders.world.WaveSim.WaveFactory;

/**
 * EnemyWaveFact
 */
public class EnemyWaveFact implements WaveFactory {

  @Override
  public List<Enemy> create(int wave, Random rng, WaveConfig config, int firstId) {
    List<Enemy> enemies = new Vector<>();
    if (wave == 10) {
      enemies.add(new ColossalEnemy(firstId, 0, 0));
      return enemies;
    }

    int count = Math.max(1, (int) Math.round(
        config.baseCount() * (1. + config.countGrowth() * (wave - 1))));

    float hp = (float) (config.hpBase() * (1. + config.hpGrowth() * (wave - 1)));

    float damage = (float) (config.attackDamageBase()
        * (1. + config.attackDamageGrowth() * (wave - 1)));

    double flyingChance = Math.min(0.4, (wave - 1) * 0.10);
    if (wave == 3)
      flyingChance = 1.0;

    for (int i = 0; i < count; ++i) {
      Enemy enem;

      if (rng.nextDouble() < flyingChance)
        enem = new FlyingEnemy(firstId + i, 0., 0., hp, config.rad(),
            config.velMax(), config.forceMax(), damage,
            config.attackRange(), config.attackInterval());
      else
        enem = new GroundEnemy(firstId + i, 0., 0., hp, GroundEnemy.MASS,
            GroundEnemy.RAD, GroundEnemy.VEL_MAX, GroundEnemy.FORCE_MAX,
            damage, config.attackRange(), config.attackInterval(),
            StraightNavigator.INSTANCE);

      enemies.add(enem);
    }

    return enemies;
  }

}
