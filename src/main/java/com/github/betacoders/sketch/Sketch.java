
package com.github.betacoders.sketch;

import com.github.betacoders.Demo.Renderer;
import com.github.betacoders.EvoEngine;
import com.github.betacoders.world.World;

import processing.core.PApplet;

/**
 * Sketch
 * Representa uma janela individual do jogo.
 *
 * Cada instância desta classe representa um indivíduo
 * da população evolutiva.
 *
 * Cada janela desenha somente uma base.
 */
public class Sketch extends PApplet {

    // Controlador compartilhado por todas as janelas.
    private final EvoEngine engine;

    // Identifica qual indivíduo esta janela representa.
    private final int individualIndex;

    // Responsável por desenhar a base nesta janela.
    private Renderer renderer;

    // Guarda a última geração exibida no título.
    private int displayedGeneration = -1;

    public Sketch(EvoEngine engine, int individualIndex) {

        this.engine = engine;
        this.individualIndex = individualIndex;
    }

    @Override
    public void settings() {

        // Todas as janelas possuem o tamanho do mundo.
        size(1000, 700);
    }

    @Override
    public void setup() {

        frameRate(60);

        // Cria o Renderer associado especificamente a esta janela.
        renderer = new Renderer(this, engine.config().width(), engine.config().height());

        // Define o título inicial da janela.
        surface.setTitle(
            "Base " + (individualIndex + 1)
        );
    }

    @Override
    public void draw() {

        /*
         * Todas as janelas compartilham o mesmo controlador.
         *
         * O synchronized impede que uma janela desenhe
         * enquanto outra estiver atualizando a população.
         */
        synchronized (engine) {

            // Somente a primeira janela avança a simulação.
            // Assim, a população inteira recebe apenas um tick
            // por quadro, em vez de um tick para cada janela.
            if (individualIndex == 0) {
                engine.step();
            }

            // Busca o mundo atual, inclusive após a troca de geração.
            World world = engine.worldAt(individualIndex);

            // Atualiza o título somente quando a geração mudar.
            int currentGeneration = engine.generation();

            if (displayedGeneration != currentGeneration) {

                surface.setTitle(
                    "Base " + (individualIndex + 1)
                    + " - Geração " + currentGeneration
                );

                displayedGeneration = currentGeneration;
            }

            // Limpa a tela antes de desenhar a base novamente.
            background(18, 20, 28);

            // Desenha somente a World desta janela.
            renderer.draw(world);
        }
    }
}
