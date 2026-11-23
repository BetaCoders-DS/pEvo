# pEvo

Evolução de bases em Java, com ondas de inimigos e visualização em Processing.

## Organização

- `EvoEngine`: ponto central que conecta a evolução aos mundos e ao `WaveSim`.
- `evolution`: genomas, criação de bases, mutações, avaliação e seleção de gerações.
- `world`: bases, células, combate, inimigos e controle das ondas pelo `WaveSim`.
- `sketch`: janela do Processing e organização dos painéis usando `Demo.Renderer`.

O fluxo é `App → EvoEngine → EvolutionExperiment → WaveSim → World`.
Cada indivíduo tem seu próprio mundo e seus próprios inimigos. Todos recebem a
mesma seed de cenário, mantida entre gerações para comparar as bases sob as mesmas
ondas. O `WaveSim` cria inimigos voadores e aplica o crescimento de vida e dano
definido em `WaveConfig`.

Uma única janela chama `EvoEngine.step()` uma vez por quadro para toda a população.
Quando todas as bases são derrotadas, o experimento registra o ranking e cria uma
nova geração. Os painéis buscam os novos mundos automaticamente.

## Visualização

- A grade mostra todos os indivíduos ao mesmo tempo e se ajusta ao tamanho da janela.
- Clique em um quadro para ampliar aquele indivíduo.
- Use **Esc** ou o botão **Voltar à grade** para mostrar todos novamente.
- A população inteira continua evoluindo durante a visualização ampliada.
- Cada quadro identifica a base, a onda atual e se o indivíduo está ativo ou derrotado.

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
Também verificam o layout, a seleção dos painéis, o recorte dos desenhos e o avanço
da população durante a alternância entre grade e indivíduo ampliado.
