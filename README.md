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

### Tema Bloons top down

O campo usa sprites em pixel art vistos de cima: uma casinha de telhado vermelho
para a sede (núcleo que precisa ser protegido),
Dart Monkey para os atiradores, Engineer Monkey para os conectores e Banana Farm
para os extratores. Os inimigos aparecem como balões coloridos; a cada cinco
ondas, parte deles usa o visual de MOAB.

A câmera enquadra a base e os inimigos para aproveitar as miniaturas. A vida do
núcleo aparece na barra inferior de cada painel. Na visão ampliada, passe o mouse
sobre um atirador para ver seu alcance e acompanhe os estoques abaixo das torres.

Os PNGs ficam em `src/main/resources/bloons`, carregados do classpath uma única
vez, e o programa funciona sem acesso à internet. `CREDITS.txt` e `manifest.json`
nesse diretório registram os autores, as fontes e os hashes dos sprites.
Os recursos vêm do conjunto top down de Derek_and_Harum1, espelhado no
[BTD-CE](https://github.com/EverydayCodeNet/BTD-CE). Bloons é de Ninja Kiwi.
A casinha da sede usa tiles do [Tiny Town de Kenney](https://kenney.nl/assets/tiny-town),
distribuídos sob CC0. Seu papel é sobreviver: a base é derrotada quando a vida
da sede chega a zero.
Esta troca é visual: as regras e os atributos continuam definidos pelo modelo
do pEvo e pelo `WaveConfig`.

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
