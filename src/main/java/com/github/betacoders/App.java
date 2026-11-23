
package com.github.betacoders;

import com.github.betacoders.evolution.EvolutionConfig;
import com.github.betacoders.evolution.EvolutionManager;
import com.github.betacoders.sketch.Sketch;

import processing.core.PApplet;

/**
 * App
 *
 * Inicializa o experimento evolutivo e abre
 * uma janela independente para cada indivíduo.
 */
public class App {

    public static void main(String[] args) {

        // Carrega as configurações padrão da evolução.
        EvolutionConfig config = EvolutionConfig.standard();

        // Cria um único controlador para toda a população.
        EvolutionManager manager = new EvolutionManager(config);

        // Abre uma janela para cada indivíduo da população.
        for (int i = 0; i < manager.populationSize(); i++) {

            // Cada janela recebe o mesmo controlador,
            // mas possui um índice diferente.
            Sketch sketch = new Sketch(manager, i);

            // Abre uma nova janela independente do Processing.
            PApplet.runSketch(
                new String[] { "Base " + (i + 1) },
                sketch
            );
        }
    }
}
