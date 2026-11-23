package com.github.betacoders.Demo;

import com.github.betacoders.collections.Vec2D;
import com.github.betacoders.world.Base;
import com.github.betacoders.world.Cells;
import com.github.betacoders.world.Link;
import com.github.betacoders.world.Packet;
import processing.core.PApplet;

/**
 * Desenha a simulação na tela.
 * Células, links e pacotes vêm direto da Base do time. Inimigos e tiros
 * ainda não existem na Base, então vêm do DemoWorld (temporário).
 */
public final class Renderer {
  // A janela onde vamos desenhar.
  private final PApplet g;

  // Tamanho do mundo em pixels.
  private final double worldW;
  private final double worldH;

  public Renderer(PApplet g, double worldW, double worldH) {
    this.g = g;
    this.worldW = worldW;
    this.worldH = worldH;
  }

  /**
   * Desenha tudo. A ORDEM importa: o que vem depois fica por cima.
   * Por isso o fundo (grade) vem primeiro e os tiros por último.
   */
  public void draw(Base base, DemoWorld extras) {
    drawGrid();
    drawLinks(base);
    drawCells(base);
    drawPackets(base);
    drawEnemies(extras);
    drawShots(extras);
  }

  /** Linhas finas de 50 em 50 pixels, só para dar noção de espaço. */
  private void drawGrid() {
    g.stroke(30, 34, 46); // cor das linhas (bem escura, quase invisível)
    g.strokeWeight(1); // espessura
    for (int x = 0; x <= worldW; x += 50)
      g.line(x, 0, x, (float) worldH); // linhas verticais
    for (int y = 0; y <= worldH; y += 50)
      g.line(0, y, (float) worldW, y); // linhas horizontais
  }

  /** Desenha cada link como uma linha entre as duas células. */
  private void drawLinks(Base base) {
    for (Cells c : base.graph()) {
      // O grafo não tem uma lista de arestas, então perguntamos a cada
      // célula quais são os vizinhos dela. Como cada link aparece duas
      // vezes (A->B e B->A), só desenhamos quando o id de A é o menor.
      base.graph().forEachNeighbor(c, (neighbor, link) -> {
        if (c.id() < neighbor.id())
          drawLink(link);
      });
    }
  }

  private void drawLink(Link l) {
    Vec2D a = l.a().pos();
    Vec2D b = l.b().pos();

    // A cor mostra o congestionamento: verde = livre, vermelho = cheio.
    // t vai de 0 (nenhum pacote) a 1 (3 ou mais pacotes no link).
    float t = (float) Math.min(1., l.load() / 3.);
    g.stroke(70 + 185 * t, 190 - 130 * t, 90, 200);

    // Link com mais pacotes também fica mais grosso.
    g.strokeWeight(2 + l.load());

    // O Processing trabalha com float; nossas posições são double,
    // por isso o "(float)".
    g.line((float) a.x, (float) a.y, (float) b.x, (float) b.y);
  }

  /** Desenha cada célula como um círculo. */
  private void drawCells(Base base) {
    for (Cells c : base.graph()) {
      // ellipse() recebe o DIÂMETRO, e a célula guarda o RAIO (rad()).
      float d = (float) (c.rad() * 2);

      g.stroke(10, 12, 18); // contorno escuro
      g.strokeWeight(2);
      setCellColor(c); // cor de preenchimento conforme o tipo
      g.ellipse((float) c.pos().x, (float) c.pos().y, d, d);
    }
  }

  /** Escolhe a cor de preenchimento de acordo com o tipo da célula. */
  private void setCellColor(Cells c) {
    // "instanceof" pergunta qual é o tipo real da célula.
    if (c instanceof Cells.Core)
      g.fill(90, 140, 255); // azul
    else if (c instanceof Cells.Relay)
      g.fill(150, 155, 170); // cinza
    else if (c instanceof Cells.Extractor)
      g.fill(70, 200, 150); // verde
    else
      g.fill(240, 120, 70); // laranja (Turret)
  }

  /** Pacotes de munição: bolinhas amarelas andando pelos links. */
  private void drawPackets(Base base) {
    g.stroke(10, 12, 18);
    g.strokeWeight(1);
    g.fill(255, 225, 90);
    for (Packet p : base.packets()) {
      // pos() calcula onde o pacote está agora, entre duas células.
      Vec2D pos = p.pos();
      g.ellipse((float) pos.x, (float) pos.y, 9, 9);
    }
  }

  /** TEMPORÁRIO: inimigos da demo, bolinhas vermelhas. */
  private void drawEnemies(DemoWorld extras) {
    g.stroke(10, 12, 18);
    g.strokeWeight(1);
    g.fill(220, 60, 70);
    for (DemoWorld.Enemy e : extras.enemies())
      g.ellipse((float) e.pos.x, (float) e.pos.y, 14, 14);
  }

  /** TEMPORÁRIO: tiros da demo, um traço claro da torre até o inimigo. */
  private void drawShots(DemoWorld extras) {
    g.strokeWeight(2);
    for (DemoWorld.Shot s : extras.shots()) {
      // O quarto valor é a transparência (0 a 255). Ela diminui junto com o
      // tempo de vida do tiro (ttl), então o traço vai esmaecendo.
      float alpha = 255f * s.ttl / DemoWorld.Shot.LIFETIME;
      g.stroke(255, 240, 160, alpha);
      g.line((float) s.from.x, (float) s.from.y, (float) s.to.x, (float) s.to.y);
    }
  }
}