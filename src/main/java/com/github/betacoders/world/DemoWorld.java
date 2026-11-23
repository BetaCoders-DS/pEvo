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

  public DemoWorld(double width, double height) {
    this.width = width;
    this.height = height;
    reset();
  }

  /** Apaga tudo e monta uma Base nova do zero. */
  public void reset() {
    extractors.clear();
    turrets.clear();

    graph = new AdjacencyGraph<>();
    nextId = 0;
    build();

    // A Base descobre sozinha qual é o Core e cria o pathfinder (A*)
    // a partir do grafo. Por isso o grafo precisa estar PRONTO antes.
    base = new Base(graph, SEED);
  }

  /** Cria a célula e registra como vértice no grafo. */
  private <T extends Cells> T add(T cell) {
    graph.addVertex(cell);
    return cell;
  }

  /** Liga duas células: cria o Link e registra a aresta no grafo. */
  private void connect(Cells a, Cells b) {
    graph.addEdge(a, b, new Link(a, b));
  }

  /** Monta a base de teste: 1 Core, 4 Relays, 2 Extractors e 4 Turrets. */
  private void build() {
    // Centro do mundo, usado como referência para posicionar tudo.
    double cx = width / 2;
    double cy = height / 2;
