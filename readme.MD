# Territory Predictive Insights

**Global Solution 2026.2 — Object-Oriented Programming | FIAP**  
**Tema:** CitizenTech — da cidade ao campo, tecnologia para a vida cotidiana  
**Recorte:** Calor e energia — monitoramento territorial com previsão e alertas explicáveis

## Disclaimer

Eu (Amom) deixei no tópico de divisão de tarefas o trabalho de cada um, porém eles não conseguiam dar git clone e commitar, então eles me mandavam os códigos e eu commitava por eles, eles me ajudaram bastante e eu não fiz tudo sozinho.

## 1. Visão geral

O **Territory Predictive Insights** é uma aplicação de console em Java que demonstra o monitoramento de uma região urbana por meio de sensores simulados de temperatura ambiente, demanda elétrica e carga da rede elétrica. O sistema registra medições no banco de dados H2, consulta o histórico, calcula estimativas simples de tendência e emite alertas automáticos quando os valores previstos alcançam os limites configurados.

O projeto foi desenvolvido para a **Global Solution 2026.2** da FIAP, no contexto **CitizenTech**, com foco no emprego de **programação orientada a objetos**, **exceções customizadas**, **persistência**, **padrão criacional** e **testes automatizados**.

> **Importante:** os dados, a região e os limiares são mockados (sao demonstrativos).

## 2. Problema, público beneficiado e decisão apoiada

Temperaturas elevadas e o aumento da demanda de energia podem afetar o conforto térmico, a continuidade de serviços e a qualidade de vida em comunidades urbanas. Em cenários reais, sinais relevantes podem ficar dispersos, dificultando a identificação antecipada de possíveis riscos.

**Público beneficiado:** equipes técnicas municipais, organizações comunitárias, associações de bairro e responsáveis pelo acompanhamento de infraestrutura local. Indiretamente, a população da região monitorada pode ser beneficiada por decisões mais bem informadas.

**Decisão apoiada:** identificar quais indicadores precisam de atenção, priorizar verificações humanas e decidir quando investigar ou acionar responsáveis pelo território. O sistema **não toma decisões operacionais sozinho**: apresenta o indicador, a estimativa, o limiar atingido e uma explicação para auxiliar a análise humana.

**Cenário demonstrativo:** Zona Norte de São Paulo/SP, identificada no aplicativo como `REG-01`. A região é utilizada apenas como referência geral, sem dados pessoais, endereços individuais ou medições reais.

## 3. Tecnologias

| Tecnologia | Uso no projeto |
|---|---|
| Java 17 | Linguagem e conceitos de orientaçao a objetos |
| Spring Boot 4.1.2 | Inicialização e gerenciamento de componentes |
| Maven / Maven Wrapper | Compilação, dependências e execução |
| Spring Data JPA / Hibernate | Repositórios e mapeamento das entidades|
| H2 Database (arquivo) | Persistência de medições e alertas entre reinicializações |
| JUnit 5 | Pra testes automatizados |
| Mockito | Simulação de repositórios nos testes de serviço |
| IntelliJ IDEA | Ambiente de desenvolvimento (prefiro ele do que o eclipse, acho o eclipse meio arcaico) |

Não é necessário instalar servidor de banco externo, configurar API REST ou utilizar interface gráfica.

## 4. Funcionalidades implementadas

- **Leituras simuladas:** três sensores geram valores dentro de faixas de teste e registram cada medição válida no H2.
- **Histórico de medições:** consulta geral ou por identificador de sensor, com valor e data/hora.
- **Média móvel:** média das **cinco medições mais recentes** de um mesmo sensor.
- **Comparação entre períodos:** compara as médias de **duas janelas de três medições** e projeta uma vez a diferença observada.
- **Previsões fundamentadas:** não gera estimativa com histórico inferior ao mínimo necessário.
- **Alertas automáticos:** classificação em `ATENCAO`, `ALERTA` ou `CRITICO` quando a estimativa atinge um limiar, com armazenamento e justificativa.
- **Histórico de alertas:** consulta geral ou por sensor.
- **Proteção de limiares:** alteração condicionada a código de acesso e à validação de valores crescentes.
- **Status territorial:** apresenta sensores cadastrados, funcionamento e totais de registros.
- **Menu de console:** interação via `Scanner` e mensagens claras de erro.

### 4.1. Sensores disponíveis

| Identificador | Tipo | Unidade | Faixa simulada aproximada |
|---|---|---|---|
| `TEMP-01` | Temperatura ambiente | °C | 25 a 40 |
| `ENER-01` | Demanda elétrica simulada | kW | 40 a 120 |
| `CARGA-01` | Carga da rede elétrica | % | 50 a 100 |

O indicador rotulado como “consumo de energia” no menu é expresso em **kW**, ou seja, potência/demanda simulada, não energia acumulada em kWh.

### 4.2. Limiares demonstrativos iniciais

| Indicador | Atenção | Alerta | Crítico |
|---|---:|---:|---:|
| Temperatura (°C) | 30 | 34 | 38 |
| Demanda elétrica (kW) | 70 | 90 | 110 |
| Carga da rede (%) | 70 | 85 | 95 |

esses valores abaixo do primeiro limiar são classificados como `NORMAL`. Os limites podem ser modificados durante a execução mediante o código de acesso de demonstração. **Esses valores não são padrões técnicos oficiais.**

## 5. Programação orientada a objetos

| Conceito | Aplicação |
|---|---|
| **Abstração** | `ComponenteTerritorial` define atributos e o método abstrato `verificarStatus()`; `EstrategiaPrevisao` define o contrato abstrato `prever()`. |
| **Herança** | `RegiaoMonitorada` herda de `ComponenteTerritorial`; `PrevisaoMediaMovel` e `PrevisaoComparacao` herdam de `EstrategiaPrevisao` e chamam `super()` nos construtores. |
| **Interface** | `Sensor` estabelece leitura, identificação, tipo e verificação de funcionamento, com três implementações concretas. |
| **Polimorfismo** | O serviço utiliza `Sensor` independentemente da implementação e executa `prever()` por meio de `EstrategiaPrevisao`. |
| **Encapsulamento** | `DadosTerritorio` mantém limiares privados e valida o código antes de alterá-los; objetos do domínio validam os dados de entrada. |
| **Exceções próprias** | `DadosInvalidosException` e `FonteIndisponivelException` identificam entradas inconsistentes e falhas de sensores, informando o motivo ao usuário. |

### 5.1. Padrão criacional Factory

A classe `FabricaPrevisao` implementa o **Factory Pattern** para centralizar a criação das estratégias `PrevisaoMediaMovel` e `PrevisaoComparacao`. Ela associa valores de `TipoPrevisao` a construtores em um `EnumMap`, evitando espalhar instâncias concretas e condicionais de seleção por todo o serviço.

**Por que usar esse padrão nesse ponto?** A escolha da estratégia é uma responsabilidade de criação, não de monitoramento nem de persistência. O serviço recebe uma abstração (`EstrategiaPrevisao`) e executa `prever()` sem depender da classe concreta. Isso facilita evolução, leitura e testes. Não foi necessário empregar outros padrões criacionais apenas para cumprir quantidade.

## 6. Organização do código

```text
src/
├── main/
│   ├── java/br/com/fiap/territory/
│   │   ├── TerritoryPredictiveInsightsFevApplication.java
│   │   ├── dominio/       # Sensores, região, limiares, Medicao e Alerta
│   │   ├── previsao/      # Estratégias e resultados de previsão
│   │   ├── excecao/       # Exceções customizadas
│   │   ├── persistencia/  # MedicaoRepository e AlertaRepository
│   │   ├── servico/       # ServicoMonitoramento e FabricaPrevisao
│   │   └── console/       # MenuConsole
│   └── resources/application.properties
└── test/
    ├── java/br/com/fiap/territory/  # Testes JUnit e Mockito
    └── resources/application.properties
```

**Fluxo funcional:** sensor → validação → entidade `Medicao` → H2 → consulta ao histórico → estratégia de previsão → comparação com limiares → entidade `Alerta` → H2 e consulta pelo menu.

## 7. Como executar

### 7.1. Pré-requisitos

- **JDK 17** instalado e configurado.
- Acesso ao terminal (PowerShell no Windows, ou terminal equivalente).
- Git, caso queira clonar o repositório. A instalação separada do Maven não é necessária porque o projeto inclui o **Maven Wrapper**.

### 7.2. Abrir o projeto

Clone o repositório por sua URL real ou baixe o ZIP em **Code > Download ZIP** no GitHub. Abra a pasta raiz no IntelliJ IDEA e permita a importação do Maven.

No Windows, execute no terminal da raiz do projeto:

```powershell
.\mvnw.cmd spring-boot:run
```

Em Linux/macOS:

```bash
./mvnw spring-boot:run
```

Espere a mensagem `Sistema territorial inicializado com sucesso.`. O menu aparecerá no console.

### 7.3. Menu

```text
1 - Verificar sensores e registrar leituras
2 - Executar previsões
3 - Consultar histórico de medições
4 - Consultar histórico de alertas
5 - Gerenciar dados do território
6 - Visualizar status completo
0 - Sair
```

**O teste que eu fiz e recomendo:** escolha a opção `1` repetidas vezes para acumular histórico. Com **cinco** leituras por sensor, a média móvel é executada; com **seis**, a comparação também fica disponível. Use `2` para selecionar manualmente sensor e estratégia, `3` e `4` para consultar registros e `0` para encerrar.

### 7.4. Banco de dados e configuração

A aplicação utiliza o H2 em arquivo:

```properties
spring.datasource.url=jdbc:h2:file:./territory-db
spring.jpa.hibernate.ddl-auto=update
```

O banco é criado automaticamente no diretório de execução (normalmente a raiz do projeto). Ao encerrar com `0` e iniciar novamente no **mesmo diretório**, as medições e os alertas anteriores permanecem disponíveis. Os arquivos locais do H2 não precisam ser versionados.

O código de acesso demonstrativo padrão é `4321`, definido na configuração para facilitar a correção acadêmica. Para usar outro código sem alterar o arquivo de configuração, no PowerShell execute **antes** de iniciar o aplicativo:

```powershell
$env:TERRITORY_CODIGO_ACESSO="seu-codigo-de-teste"
.\mvnw.cmd spring-boot:run
```

Esse mecanismo **não é autenticação de produção**. Além disso, as alterações dos limiares são mantidas apenas em memória durante a execução; ao reiniciar, os limites retornam aos valores iniciais.

## 8. Como executar os testes

No terminal da raiz do projeto, em Windows:

```powershell
.\mvnw.cmd clean test
```

No Linux/macOS:

```bash
./mvnw clean test
```

O projeto dispõe de testes unitários de sensores, validações, medições, alertas, estratégias, Factory e serviço, além de testes de persistência com H2. Os testes de serviço usam **Mockito** para simular os repositórios e validar a geração de alertas sem depender do banco. Há verificações de exceções com `assertThrows` e estrutura **AAA** (*Arrange, Act, Assert*).

Na última validação comunicada pela equipe, **45 testes passaram** com `Failures: 0` e `Errors: 0`. Os testes com Spring utilizam configuração em `src/test/resources/application.properties`, incluindo H2 em memória e menu desabilitado, para não interferir no banco principal nem aguardar entradas no console.

## 9. Evidências de validação funcional

Na execução demonstrativa o console exibiu:

- **21 medições** armazenadas e recuperadas após reiniciar o aplicativo;
- **15 alertas** persistidos, com justificativas e níveis de risco;
- previsões com média móvel e comparação entre períodos;
- ocorrências de alertas **ATENÇÃO**, **ALERTA** e **CRÍTICO**;
- alteração válida de limiares com o código demonstrativo;
- bloqueio da alteração ao informar um código incorreto;
- inicialização do Spring e conexão H2 sem falha na execução apresentada.

Os números acima são **evidências de uma execução específica**, não valores fixos do aplicativo: em uma instalação nova, o histórico começa vazio.

## 10. Decisões técnicas e limitações

| Decisão | Justificativa / consequência |
|---|---|
| Aplicação de console, sem API REST | Atende ao escopo da disciplina e destaca lógica orientada a objetos. |
| Sensores simulados | Permitem repetir a demonstração sem dispositivos físicos ou fontes externas. |
| H2 em arquivo para execução normal | Mantém o histórico após encerramento e reinício. |
| H2 em memória para testes | Isola a execução dos testes do histórico principal. |
| Bloqueio de previsão sem histórico mínimo | Evita apresentar valores sem amostra suficiente para o algoritmo. |
| Factory para estratégias | Evita acoplamento do serviço a classes concretas de previsão. |
| Limiares alteráveis mediante código | Demonstra encapsulamento e validação, mas não substitui controle de acesso real. |

**Limitações conhecidas:** (a) dados e valores de referência simulados; (b) somente três sensores de uma região genérica; (c) estimativas simples, sem horizonte temporal explícito nem margem de incerteza; (d) limiares personalizados não persistem após reiniciar; (e) executar a mesma previsão novamente pode gerar alertas redundantes; (f) não há exportação CSV implementada nesta versão. A exportação CSV é um **bônus opcional do enunciado** e não deve ser considerada entregue sem implementação e teste.

## 11. Reflexão crítica — responsabilidade no uso dos alertas

**Quando o sistema deve se calar?** Uma plataforma de monitoramento responsável não deve transformar qualquer dado disponível em um aviso de risco. Por isso, as estratégias implementadas exigem um histórico mínimo: cinco medições para a média móvel e seis para a comparação entre períodos. Antes disso, o aplicativo informa que não há leituras suficientes e deixa de produzir previsões. Mesmo depois que esse mínimo é alcançado, a estimativa continua sendo simples. Se houver falha no sensor, leituras inconsistentes, intervalos muito irregulares ou mudanças no contexto local, a interpretação precisa ser revista. Em uma aplicação real, também seria importante verificar atualização dos dados, qualidade da fonte e incerteza antes de emitir avisos à população.

**O que os dados não representam?** Um sensor de temperatura não descreve todos os microclimas de uma região inteira, e um indicador agregado de carga elétrica não identifica a condição de cada imóvel ou equipamento. Nesta versão, todos os valores são simulados e não foram calibrados com dados oficiais ou dispositivos físicos. As projeções por média móvel e comparação não permitem afirmar, com confiança estatística, o que acontecerá nos próximos dias, nem quantificar a probabilidade de falhas elétricas ou impactos na saúde. Os limiares são escolhidos para fins didáticos, não para embasar ações técnicas reais.

**Por que o alerta deve apoiar, e não substituir, a pessoa?** A plataforma oferece um sinal rastreável: identifica o sensor, informa o valor previsto, mostra o limiar alcançado e explica o cálculo empregado. Esse conjunto ajuda uma equipe a decidir onde investigar primeiro, mas não oferece contexto suficiente para determinar desligamentos, evacuações, racionamento ou outras medidas que afetem pessoas. Uma decisão responsável exige avaliação humana, confirmação por outras fontes e consideração dos efeitos sociais. O objetivo da CitizenTech é apoiar a comunidade com transparência e não automatizar decisões sobre ela sem supervisão.

## 12. Uso de inteligência artificial

Foi utilizada **IA generativa como apoio ao aprendizado e à implementação**, incluindo organização do desenvolvimento (principalmente clean code e identação pro código ficar certinho) por etapas, explicações de conceitos de POO, sugestões de estrutura de código, elaboração de cenários de teste e apoio à documentação (correções de gramática e sugestões no readme).

O código foi inserido e executado no IntelliJ IDEA, com validação por testes automatizados e pela utilização do menu em console, mesmo que a gente usasse eclipse na aula eu prefiro o intellij porque ele tem a mesma interface no pycharm e me agrada mais. Qualquer dúvida sobre o código e necessidade de explicação pode ser pedida, e vamos ser capazes de explicar as decisões e os trechos de código apresentados. A IA não foi utilizada como fonte de medições reais nem como mecanismo de previsão do aplicativo.


## 13. Integrantes e divisão de tarefas

**Preencher com a equipe real antes da entrega** e ajustar a divisão conforme as contribuições e os commits efetivamente realizados.

| Integrante / RM | Responsabilidades efetivamente realizadas |
|---|---|
| **[Amom Ianaguivara Brito — 565718]** | [domínio, sensores, validações, exceções, readme completo] |
| **[Fernando Antônio — 562549]** | [JPA, H2, entidades e repositórios] |
| **[Victor Chen — 565363]** | [estratégias, alertas, testes e integração] |


## 14. Entrega

A entrega desta disciplina consiste no **link do repositório GitHub**, contendo o código `.java`, `pom.xml`, Maven Wrapper, configurações necessárias e este README. Para avaliação, o corretor deve conseguir clonar/baixar o projeto, executar `clean test` e iniciar o aplicativo com `spring-boot:run` seguindo as instruções acima.

**Antes de enviar:** confirmar nomes e RMs, conferir os commits da equipe, executar os testes em uma cópia limpa do repositório e verificar se o link do GitHub está acessível aos avaliadores.
