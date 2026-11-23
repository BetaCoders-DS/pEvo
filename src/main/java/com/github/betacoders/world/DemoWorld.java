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


  // --- INIMIGOS E TIROS

   /** Um inimigo: anda em linha reta até o Core. */
  public static final class Enemy {
    public final Vec2D pos; // posição atual (muda a cada tick)
    public float hp; // vida atual
    public final float hpMax; // vida máxima (usada na barra de vida)
    private final double speed; // velocidade em px por tick

    Enemy(double x, double y, float hp, double speed) {
      this.pos = new Vec2D(x, y);
      this.hp = hp;
      this.hpMax = hp;
      this.speed = speed;
    }
  }

  /** Traço de um tiro. Existe só para o Renderer desenhar um flash curto. */
  public static final class Shot {
    /** Quantos ticks o traço dura na tela. */
    public static final int LIFETIME = 7;

    public final Vec2D from; // de onde saiu (a torre)
    public final Vec2D to; // onde acertou (o inimigo)
    public int ttl; // "time to live": ticks até o traço sumir

    Shot(Vec2D from, Vec2D to) {
      // copy() porque o inimigo continua andando e a posição dele muda;
      // o traço precisa guardar o ponto exato em que o tiro acertou.
      this.from = from.copy();
      this.to = to.copy();
      this.ttl = LIFETIME;
    }
  }

  // Um inimigo novo aparece a cada tantos ticks (84 ticks = 1,4 s).
  private static final int ENEMY_SPAWN_EVERY = 84;

   // Velocidade com que o pacote anda pelos links, em pixels por TICK
  // (2.2 px/tick a 60 ticks por segundo = ~130 px/s).
  private static final double PACKET_SPEED = 2.2;

  // Para qual torre mandar a próxima munição (revezamento entre elas).
  private int nextTurret;

  // O Random usa a MESMA semente da Base: toda execução gera os mesmos
  // inimigos, nos mesmos lugares. Isso deixa os testes repetíveis.
  private final Random rng = new Random(SEED);

  private final ArrayList<Enemy> enemies = new ArrayList<>();
  private final ArrayList<Shot> shots = new ArrayList<>();
  private int kills; // inimigos mortos
  private int ticks; // ticks de simulação
  private int spawnTimer; // conta os ticks até o próximo inimigo


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
    nextTurret = 0;
    enemies.clear();
    shots.clear();
    kills = 0;
    ticks = 0;
    spawnTimer = 0;

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

    // O Core fica no meio.
    Cells.Core core = add(new Cells.Core(nextId++, cx, cy));

    // Relays: ficam em volta do Core e distribuem a munição.
    Cells r1 = add(new Cells.Relay(nextId++, cx - 80, cy - 50));
    Cells r2 = add(new Cells.Relay(nextId++, cx + 80, cy - 50));
    Cells r3 = add(new Cells.Relay(nextId++, cx - 80, cy + 50));
    Cells r4 = add(new Cells.Relay(nextId++, cx + 80, cy + 50));

    // Extractors: produzem munição. Parâmetros: id, x, y, capacidade (5)
    // e intervalo em TICKS entre cada munição produzida (50).
    Cells.Extractor e1 = add(new Cells.Extractor(nextId++, cx, cy - 120, 5, 50));
    Cells.Extractor e2 = add(new Cells.Extractor(nextId++, cx, cy + 120, 5, 50));
    extractors.add(e1);
    extractors.add(e2);

    // Turrets: atiram nos inimigos. Parâmetros: id, x, y, capacidade de
    // munição (6), alcance em px (170), dano por tiro (12) e intervalo
    // entre tiros em TICKS (60, ou seja, 1 tiro por segundo).
    Cells.Turret t1 = add(new Cells.Turret(nextId++, cx - 180, cy - 100, 6, 170., 12f, 60));
    Cells.Turret t2 = add(new Cells.Turret(nextId++, cx + 180, cy - 100, 6, 170., 12f, 60));
    Cells.Turret t3 = add(new Cells.Turret(nextId++, cx - 180, cy + 100, 6, 170., 12f, 60));
    Cells.Turret t4 = add(new Cells.Turret(nextId++, cx + 180, cy + 100, 6, 170., 12f, 60));
    turrets.add(t1);
    turrets.add(t2);
    turrets.add(t3);
    turrets.add(t4);

    // Core <-> Relays
    connect(core, r1);
    connect(core, r2);
    connect(core, r3);
    connect(core, r4);

    // Relays entre si
    connect(r1, r2);
    connect(r3, r4);
    connect(r1, r3);
    connect(r2, r4);

    // Extractors -> Relays
    connect(e1, r1);
    connect(e1, r2);
    connect(e2, r3);
    connect(e2, r4);

    // Turrets -> Relays
    connect(t1, r1);
    connect(t2, r2);
    connect(t3, r3);
    connect(t4, r4);
  }


   // ---- Simulação ----

  /**
   * Avança a simulação UM tick.
   */
  public void tick() {
    // Cada célula avança um tick. Isso faz o Extractor produzir munição e
    // a Turret diminuir o tempo de espera entre tiros.
    for (Cells c : base.graph())
      c.tick();

    sendPackets();
    movePackets();
  }

  /** Cada extrator que tiver munição manda um pacote para uma torre. */
  private void sendPackets() {
    for (Cells.Extractor ex : extractors) {
      if (ex.stored() <= 0)
        continue; // ainda não produziu nada

      Cells.Turret target = nextTurretNeedingAmmo();
      if (target == null)
        continue; // todas as torres já estão cheias

      // O A* da Base devolve a lista de células do extrator até a torre.
      List<Cells> path = base.pathfinder().findPath(ex, target);

      // path.size() < 2 significa que não existe caminho. Só gastamos a
      // munição do extrator (consume) se o pacote realmente puder sair.
      if (path.size() >= 2 && ex.consume())
        base.packets().add(new Packet(path, PACKET_SPEED, base.graph()));
    }
  }

  /**
   * Escolhe a próxima torre que ainda aceita munição, em revezamento.
   * Devolve null se todas estiverem cheias.
   */
  private Cells.Turret nextTurretNeedingAmmo() {
    for (int i = 0; i < turrets.size(); ++i) {
      // O "% size" faz a contagem voltar ao início da lista.
      Cells.Turret t = turrets.get((nextTurret + i) % turrets.size());

      if (t.alive() && t.stored() < t.cap()) {
        nextTurret = (nextTurret + i + 1) % turrets.size();
        return t;
      }
    }
    return null;
  }

  /** Faz cada pacote andar um tick e remove os que já chegaram. */
  private void movePackets() {
    List<Packet> packets = base.packets();

    // Percorremos de TRÁS PARA FRENTE porque remover um item da lista
    // desloca os que vêm depois dele; de trás para frente isso não
    // atrapalha os que ainda faltam visitar.
    for (int i = packets.size() - 1; i >= 0; --i) {
      Packet p = packets.get(i);
      p.tick(base.graph());

      // done() fica true quando o pacote chegou (e entregou a munição
      // para a torre) ou quando o caminho foi interrompido.
      if (p.done())
        packets.remove(i);
    }
  }

  //Getters

  /** A Base real do time. É daqui que o Renderer lê células, links e pacotes. */
  public Base base() {
    return base;
  }

  public double width() {
    return width;
  }

  public double height() {
    return height;
  }
}