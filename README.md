# pEvo

Evolução de bases em Java, com ondas de inimigos e visualização em Processing.

## Organização

- `EvoEngine`: ponto central que conecta a evolução aos mundos e ao `WaveSim`.
- `evolution`: genomas, criação de bases, mutações, avaliação e seleção de gerações.
- `world`: bases, células, combate, inimigos e controle das ondas pelo `WaveSim`.
- `sketch`: janelas do Processing, que exibem os mundos usando `Demo.Renderer`.

O fluxo é `App → EvoEngine → EvolutionExperiment → WaveSim → World`.
Cada indivíduo tem seu próprio mundo e seus próprios inimigos. Todos recebem a
mesma seed de cenário, mantida entre gerações para comparar as bases sob as mesmas
ondas. O `WaveSim` cria inimigos voadores e aplica o crescimento de vida e dano
definido em `WaveConfig`.

Somente a primeira janela chama `EvoEngine.step()`; as demais exibem seu indivíduo.
Quando todas as bases são derrotadas, o experimento registra o ranking e cria uma
nova geração. As janelas buscam os novos mundos automaticamente.

## Executar

Requer JDK 17 ou superior e Maven.

```powershell
mvn test
mvn exec:java "-Dexec.mainClass=com.github.betacoders.App"
```

Para configurar as ondas, use `new EvoEngine(config, waveConfig)`. Para repetir
uma execução, chame `engine.restart(seed)` antes de avançar a população.

Os testes cobrem isolamento dos indivíduos, reprodução por seed, ondas,
crescimento dos inimigos, alcance de ataque e troca de geração após combate real.
