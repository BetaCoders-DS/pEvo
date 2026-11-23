
package com.github.betacoders;

import com.github.betacoders.evolution.EvolutionConfig;
import com.github.betacoders.sketch.Sketch;

import processing.core.PApplet;

/**
 * App
 *
 * Inicializa o experimento evolutivo e abre a população em uma única janela.
 */
public class App {

    public static void main(String[] args) {

        // Carrega as configurações padrão da evolução.
        EvolutionConfig config = EvolutionConfig.standard();

        // Cria um único controlador para toda a população.
        EvoEngine engine = new EvoEngine(config);

        PApplet.runSketch(new String[] { "pEvo" }, new Sketch(engine));
    }
}
