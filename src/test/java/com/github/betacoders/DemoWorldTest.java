package com.github.betacoders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.github.betacoders.Demo.DemoWorld;

/**
 * Testes básicos do DemoWorld: conferem se a demonstração se comporta
 * de forma coerente
 */
public class DemoWorldTest {
  // O projeto conta o tempo em ticks. Aqui, 60 ticks = 1 segundo.
  private static final int TICKS_PER_SECOND = 60;

  /** Roda a simulação por alguns segundos, sem abrir janela. */
  private static void run(DemoWorld w, int seconds) {
    for (int i = 0; i < seconds * TICKS_PER_SECOND; ++i)
      w.tick();
  }

  @Test
  public void montaABaseCompleta() {
    DemoWorld w = new DemoWorld(1000, 700);

    // 1 Core + 4 Relays + 2 Extractors + 4 Turrets = 11 células
    assertEquals(11, w.base().graph().size());
    assertEquals(16, w.base().graph().edgeCount());
    // No início, a base está com a vida cheia.
    assertEquals(w.base().core().hpMax(), w.base().core().hp());
  }

  @Test
  public void torresMatamInimigosComAMunicaoRecebida() {
    DemoWorld w = new DemoWorld(1000, 700);

    run(w, 30);

    // Se a munição não chegasse nas torres, ninguém morreria.
    assertTrue(w.kills() > 0);
  }

  @Test
  public void mesmaSementeDaMesmoResultado() {
    DemoWorld a = new DemoWorld(1000, 700);
    DemoWorld b = new DemoWorld(1000, 700);

    run(a, 30);
    run(b, 30);

    // A semente do Random é fixa, então os dois mundos devem ser idênticos.
    assertEquals(a.kills(), b.kills());
    assertEquals(a.base().core().hp(), b.base().core().hp());
  }

  @Test
  public void resetVoltaAoEstadoInicial() {
    DemoWorld w = new DemoWorld(1000, 700);
    run(w, 10);

    w.reset();

    assertEquals(0, w.kills());
    assertEquals(0, w.ticks());
    assertEquals(11, w.base().graph().size());
    assertEquals(0, w.enemies().size());
  }
}