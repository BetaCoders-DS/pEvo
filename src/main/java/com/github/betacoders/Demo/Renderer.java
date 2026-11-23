
package com.github.betacoders.Demo;

import com.github.betacoders.collections.Vec2D;
import com.github.betacoders.world.Cells;
import com.github.betacoders.world.Enemy;
import com.github.betacoders.world.Link;
import com.github.betacoders.world.Packet;
import com.github.betacoders.world.World;

import processing.core.PApplet;

/**
 * Renderer
 * Consulta os objetos que já existem na simulação.
 */
public final class Renderer {

  // A janela do Processing onde tudo será desenhado.
  private final PApplet g;

  // Dimensões originais do mundo.
  private final double worldW;
  private final double worldH;

  public Renderer(PApplet g, double worldW, double worldH) {
    this.g = g;
    this.worldW = worldW;
    this.worldH = worldH;
  }

  /**
   * Desenha um World dentro de uma área específica da janela.
   *
   * x e y representam o canto superior esquerdo do painel.
   * panelW e panelH representam o tamanho disponível para desenhar.
   */
/**
 * Desenha uma única base ocupando a janela inteira.
 *
 * Cada janela recebe uma World independente.
 * Portanto, este método nunca desenha várias bases ao mesmo tempo.
 */
public void draw(World world) {

    // Calcula a escala necessária para o mundo caber na janela.
    // O Math.min garante que o desenho não ultrapasse os limites.
    float scale = Math.min(
        g.width / (float) worldW,
        g.height / (float) worldH
    );

    // Guarda as configurações atuais de transformação do Processing.
    g.pushMatrix();

    // Calcula os espaços laterais para centralizar o mundo na janela.
    float offsetX = (g.width - (float) worldW * scale) / 2;
    float offsetY = (g.height - (float) worldH * scale) / 2;

    // Posiciona o mundo no centro da janela.
    g.translate(offsetX, offsetY);

    // Redimensiona tudo que será desenhado a partir daqui.
    g.scale(scale);

    // Desenha os elementos da base.
    // A ordem importa: os elementos desenhados depois ficam por cima.
    drawGrid();
    drawLinks(world);
    drawCells(world);
    drawPackets(world);
    drawEnemies(world);

    // Restaura as transformações anteriores do Processing.
    g.popMatrix();
}

  /**
   * Desenha a grade de fundo.
   * As linhas são desenhadas a cada 50 pixels.
   */
  private void drawGrid() {

    g.stroke(30, 34, 46);
    g.strokeWeight(1);

    // Linhas verticais.
    for (int x = 0; x <= worldW; x += 50) {
      g.line(x, 0, x, (float) worldH);
    }

    // Linhas horizontais.
    for (int y = 0; y <= worldH; y += 50) {
      g.line(0, y, (float) worldW, y);
    }
  }

  /**
   * Desenha as conexões entre as células.
   *
   * Cada conexão aparece duas vezes quando percorremos os vizinhos.
   */
  private void drawLinks(World world) {

    for (Cells c : world.graph()) {

      world.graph().forEachNeighbor(c, (neighbor, link) -> {

        // Só desenha quando o ID da primeira célula é menor.
        if (c.id() < neighbor.id()) {
          drawLink(link);
        }

      });
    }
  }

  /**
   * Desenha uma conexão como uma linha entre duas células.
   *
   * A cor representa o congestionamento:
   * verde = livre;
   * vermelho = com muitos pacotes.
   */
  private void drawLink(Link l) {

    Vec2D a = l.a().pos();
    Vec2D b = l.b().pos();

    // t varia de 0 a 1 conforme a quantidade de pacotes.
    float t = (float) Math.min(1.0, l.load() / 3.0);

    g.stroke(
        70 + 185 * t,
        190 - 130 * t,
        90,
        200
    );

    // Quanto maior o congestionamento, mais grossa a linha.
    g.strokeWeight(2 + l.load());

    // Desenha a linha entre as duas células.
    g.line(
        (float) a.x,
        (float) a.y,
        (float) b.x,
        (float) b.y
    );
  }

  /**
   * Desenha todas as células do mundo.
   *
   * Cada célula é representada por um círculo.
   * Também o alcance das torres, a munição e a vida.
   */
  private void drawCells(World world) {

    for (Cells c : world.graph()) {

      float x = (float) c.pos().x;
      float y = (float) c.pos().y;

      // O raio é convertido em diâmetro para desenhar o círculo.
      float d = (float) (c.rad() * 2);

      // Se for uma torre, desenha seu alcance antes da própria célula.
      // A transparência deixa o círculo de alcance discreto.
      if (c instanceof Cells.Turret t) {

        g.noStroke();
        g.fill(255, 120, 80, 18);

        g.ellipse(
            x,
            y,
            (float) (t.range() * 2),
            (float) (t.range() * 2)
        );
      }

      // Desenha a célula propriamente dita.
      g.stroke(10, 12, 18);
      g.strokeWeight(2);

      setCellColor(c);

      g.ellipse(x, y, d, d);

      // Torres e extratores possuem armazenamento de munição.
      // As bolinhas mostram quantas unidades estão armazenadas.
      if (c instanceof Cells.Turret t) {

        drawAmmo(x, y, d, t.stored(), t.cap());

      } else if (c instanceof Cells.Extractor e) {

        drawAmmo(x, y, d, e.stored(), e.cap());
      }

      // A barra de vida só aparece quando a célula sofre dano.
      if (c.hp() < c.hpMax()) {

        drawBar(
            x - d / 2,
            y - d / 2 - 9,
            d,
            4,
            c.hp() / c.hpMax(),
            90,
            220,
            110
        );
      }
    }
  }

  /**
   * Define a cor da célula de acordo com seu tipo.
   */
  private void setCellColor(Cells c) {

    if (c instanceof Cells.Core) {

      g.fill(90, 140, 255); // Azul: núcleo.

    } else if (c instanceof Cells.Relay) {

      g.fill(150, 155, 170); // Cinza: conexão.

    } else if (c instanceof Cells.Extractor) {

      g.fill(70, 200, 150); // Verde: extrator.

    } else {

      g.fill(240, 120, 70); // Laranja: torre.
    }
  }

  /**
   * Desenha os pacotes de munição.
   *
   * Os pacotes vêm diretamente da Base associada ao World.
   * A posição é calculada pelo próprio objeto Packet.
   */
  private void drawPackets(World world) {

    g.stroke(10, 12, 18);
    g.strokeWeight(1);
    g.fill(255, 225, 90);

    for (Packet p : world.base().packets()) {

      Vec2D pos = p.pos();

      // Cada pacote é representado por uma bolinha amarela.
      g.ellipse(
          (float) pos.x,
          (float) pos.y,
          9,
          9
      );
    }
  }

  /**
   * Desenha os inimigos que já foram adicionados ao World.
   *
   * Por enquanto, todos são representados por círculos vermelhos.
   * A interface Enemy não possui um método de vida máxima,
   * então não desenhamos a barra de vida nesta versão.
   */
  private void drawEnemies(World world) {

    g.stroke(10, 12, 18);
    g.strokeWeight(1);
    g.fill(220, 60, 70);

    for (Enemy e : world.enemies()) {

      // Não desenha inimigos que já morreram.
      if (!e.alive()) {
        continue;
      }

      Vec2D pos = e.pos();

      g.ellipse(
          (float) pos.x,
          (float) pos.y,
          14,
          14
      );
    }
  }

  /**
   * Desenha as bolinhas que representam a munição armazenada.
   *
   * As bolinhas amarelas representam munição disponível.
   * As bolinhas escuras representam espaços vazios.
   */
  private void drawAmmo(float x, float y, float d, int stored, int cap) {

    float size = 5;

    // Calcula o início da fileira para deixá-la centralizada.
    float startX = x - (cap * (size + 2) - 2) / 2;

    g.noStroke();

    for (int i = 0; i < cap; ++i) {

      if (i < stored) {

        g.fill(255, 225, 90); // Munição disponível.

      } else {

        g.fill(60, 64, 78); // Espaço vazio.
      }

      g.ellipse(
          startX + i * (size + 2) + size / 2,
          y + d / 2 + 8,
          size,
          size
      );
    }
  }

  /**
   * Desenha uma barra de progresso.
   *
   * É utilizada para mostrar a vida das células.
   * frac representa a porcentagem restante, entre 0 e 1.
   */
  private void drawBar(
      float x,
      float y,
      float w,
      float h,
      float frac,
      int r,
      int gr,
      int b) {

    g.noStroke();

    // Fundo escuro da barra.
    g.fill(40, 20, 24);
    g.rect(x, y, w, h);

    // Parte preenchida da barra.
    g.fill(r, gr, b);

    // Garante que a porcentagem fique entre 0 e 1.
    frac = Math.max(0f, Math.min(1f, frac));

    g.rect(x, y, w * frac, h);
  }
}
