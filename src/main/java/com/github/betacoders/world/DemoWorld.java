package com.github.betacoders.world;

import com.github.betacoders.collections.AdjacencyGraph;
import com.github.betacoders.collections.Graph;
import com.github.betacoders.collections.List;
import com.github.betacoders.world.Base;
import com.github.betacoders.collections.Vec2D; // posição (x, y)
import com.github.betacoders.world.Cells; // Core, Relay, Extractor, Turret
import com.github.betacoders.world.Link; // ligação entre duas células
import com.github.betacoders.world.Packet; // pacote de munição que anda pelo grafo


import java.util.ArrayList;
import java.util.Iterator; // permite remover itens de uma lista enquanto percorre
import java.util.Random; //(spawn dos inimigos)

/**
 * DemoWorld (TEMPORÁRIO).
 *
 * Esta classe monta uma Base de teste, o que a Base fará
 * no futuro (extratores gerando munição, inimigos, tiros)
 *
 * Quando a Base do time estiver completa, esta classe pode ser apagada.
 */
public final class DemoWorld {
  // Semente do Random da Base: mesma semente = mesma simulação.
  private static final long SEED = 42;

  // Tamanho do mundo em pixels.
  private final double width;
  private final double height;

  // Listas de apoio para a demo achar rápido os extratores e as torres.
  private final ArrayList<Cells.Extractor> extractors = new ArrayList<>();
  private final ArrayList<Cells.Turret> turrets = new ArrayList<>();

  // O grafo é montado aqui e depois entregue à Base.
  private Graph<Cells, Link> graph;
  private Base base;
  private int nextId; // contador para dar um id único a cada célula
