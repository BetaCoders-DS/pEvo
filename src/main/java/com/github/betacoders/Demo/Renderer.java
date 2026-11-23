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

    /** Desenha cada célula como um círculo, com alcance, munição e vida. */
  private void drawCells(Base base) {
    for (Cells c : base.graph()) {
      float x = (float) c.pos().x;
      float y = (float) c.pos().y;
      float d = (float) (c.rad() * 2); // diâmetro

      // O "instanceof Cells.Turret t" confere o tipo E já cria a variável t
      // com esse tipo, para podermos chamar métodos exclusivos da torre.
      // Alcance da torre: círculo grande e bem transparente (alpha 18),
      // desenhado ANTES da célula para ficar por trás.
      if (c instanceof Cells.Turret t) {
        g.noStroke();
        g.fill(255, 120, 80, 18);
        g.ellipse(x, y, (float) (t.range() * 2), (float) (t.range() * 2));
      }

      // A célula em si.
      g.stroke(10, 12, 18);
      g.strokeWeight(2);
      setCellColor(c);
      g.ellipse(x, y, d, d);

      // Munição guardada (só torres e extratores têm).
      if (c instanceof Cells.Turret t)
        drawAmmo(x, y, d, t.stored(), t.cap());
      else if (c instanceof Cells.Extractor e)
        drawAmmo(x, y, d, e.stored(), e.cap());

      // Barra de vida acima da célula, só aparece se ela estiver machucada.
      if (c.hp() < c.hpMax())
        drawBar(x - d / 2, y - d / 2 - 9, d, 4, c.hp() / c.hpMax(), 90, 220, 110);
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

  /** TEMPORÁRIO: inimigos da demo, com uma barra de vida em cima. */
  private void drawEnemies(DemoWorld extras) {
    for (DemoWorld.Enemy e : extras.enemies()) {
      float x = (float) e.pos.x;
      float y = (float) e.pos.y;

      g.stroke(10, 12, 18);
      g.strokeWeight(1);
      g.fill(220, 60, 70);
      g.ellipse(x, y, 14, 14);

      // Barra acima do inimigo: largura 16, proporcional à vida restante.
      drawBar(x - 8, y - 15, 16, 3, e.hp / e.hpMax, 90, 220, 110);
    }
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