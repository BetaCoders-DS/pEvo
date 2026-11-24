package com.github.betacoders;

import com.github.betacoders.evolution.EvolutionConfig;
import com.github.betacoders.sketch.Sketch;

import processing.core.PApplet;

public class App {
    public static void main(String[] args) {
        EvolutionConfig config = EvolutionConfig.standard();
        EvoEngine engine = new EvoEngine(config);
        PApplet.runSketch(new String[] { "pEvo" }, new Sketch(engine));
    }
}
