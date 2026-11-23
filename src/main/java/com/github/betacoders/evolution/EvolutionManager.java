
package com.github.betacoders.evolution;

import com.github.betacoders.world.Simulation;
import com.github.betacoders.world.World;

/**
 * EvolutionManager
 *
 * Controla a população de bases e conecta as simulações
 * independentes ao sistema de evolução.
 *
 * Cada posição da população representa uma base diferente.
 * As janelas apenas exibem essas bases.
 */
public final class EvolutionManager {

    // Controla a população e o processo de seleção natural.
    private final EvolutionExperiment experiment;

    public EvolutionManager(EvolutionConfig config) {

        // Cria o experimento evolutivo.
        // Cada World recebida gera uma Simulation independente.
        experiment = new EvolutionExperiment(
            config,
            (world, scenarioSeed) -> new RenderSimulation(world)
        );
    }

    /**
     * Avança a população em um passo de simulação.
     *
     * Este método deve ser chamado por apenas uma janela,
     * para que a população não avance várias vezes por quadro.
     */
    public synchronized void step() {

        // O próprio EvolutionExperiment verifica quando
        // todas as bases terminaram e inicia a próxima geração.
        experiment.step();
    }

    /**
     * Retorna a simulação correspondente a uma janela.
     *
     * O índice identifica qual indivíduo da população
     * aquela janela deve exibir.
     */
    public synchronized Simulation simulationAt(int index) {

        Simulation[] simulations = experiment.simulations();

        if (index < 0 || index >= simulations.length) {
            throw new IndexOutOfBoundsException(
                "Índice de simulação inválido: " + index
            );
        }

        return simulations[index];
    }

    /**
     * Retorna a quantidade de bases da população.
     */
    public int populationSize() {
        return experiment.simulations().length;
    }

    /**
     * Retorna a geração atual.
     */
    public synchronized int generation() {
        return experiment.generation();
    }

    /**
     * Implementação da interface Simulation.
     *
     * Cada objeto recebe uma World própria.
     * O tick atualiza somente aquela World.
     */
    private static final class RenderSimulation implements Simulation {

        private final World world;

        private RenderSimulation(World world) {
            this.world = world;
        }

        @Override
        public World world() {
            return world;
        }

        @Override
        public void tick() {

            // Atualiza somente a base pertencente a esta simulação.
            world.tick();
        }
    }
}
