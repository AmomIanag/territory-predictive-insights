package br.com.fiap.territory.console;

import br.com.fiap.territory.dominio.*;
import br.com.fiap.territory.excecao.*;
import br.com.fiap.territory.previsao.*;
import br.com.fiap.territory.servico.ServicoMonitoramento;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

@Component
@ConditionalOnProperty(
        name = "territory.console.enabled",
        havingValue = "true"
)
public class MenuConsole implements CommandLineRunner {

    private static final Locale PT_BR =
            Locale.forLanguageTag("pt-BR");

    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final ServicoMonitoramento servico;
    private final DadosTerritorio dadosTerritorio;
    private final RegiaoMonitorada regiao;
    private final Scanner scanner;

    public MenuConsole(
            ServicoMonitoramento servico,
            DadosTerritorio dadosTerritorio
    ) {
        this.servico = servico;
        this.dadosTerritorio = dadosTerritorio;
        this.scanner = new Scanner(System.in);

        this.regiao = new RegiaoMonitorada(
                "REG-01",
                "Zona Norte",
                "São Paulo/SP"
        );

        List<Sensor> sensoresIniciais = List.of(
                new SensorTemperatura("TEMP-01"),
                new SensorConsumoEnergia("ENER-01"),
                new SensorCargaRede("CARGA-01")
        );

        for (Sensor sensor : sensoresIniciais) {
            regiao.adicionarSensor(sensor);
            servico.cadastrarSensor(sensor);
        }
    }

    @Override
    public void run(String... args) {

        System.out.println(
                "\nSistema territorial inicializado com sucesso."
        );

        boolean executando = true;

        while (executando) {
            mostrarMenu();

            if (!scanner.hasNextLine()) {
                System.out.println("Entrada encerrada.");
                break;
            }

            String opcao = scanner.nextLine().trim();

            try {
                switch (opcao) {
                    case "1" -> verificarSensores();
                    case "2" -> executarPrevisaoManual();
                    case "3" -> consultarMedicoes();
                    case "4" -> consultarAlertas();
                    case "5" -> gerenciarLimiares();
                    case "6" -> mostrarStatusCompleto();

                    case "0" -> {
                        System.out.println(
                                "Encerrando o monitoramento. Até logo!"
                        );
                        executando = false;
                    }

                    default -> System.out.println(
                            "Opção inválida. Tente novamente."
                    );
                }

            } catch (
                    DadosInvalidosException
                    | FonteIndisponivelException e
            ) {
                System.out.println(
                        "\nAviso: " + e.getMessage()
                );

            } catch (DataAccessException e) {
                System.out.println(
                        "\nErro de acesso ao banco H2: "
                                + e.getMessage()
                );
            }
        }
    }

    private void mostrarMenu() {
        System.out.println("""
                
                ==========================================
                     TERRITORY PREDICTIVE INSIGHTS
                       Monitoramento Territorial
                ==========================================
                1 - Verificar sensores e registrar leituras
                2 - Executar previsões
                3 - Consultar histórico de medições
                4 - Consultar histórico de alertas
                5 - Gerenciar dados do território
                6 - Visualizar status completo
                0 - Sair
                ==========================================
                """);

        System.out.print("Escolha uma opção: ");
    }

    private void verificarSensores() {

        System.out.println(
                "\n=== VERIFICAÇÃO DOS SENSORES ==="
        );

        for (Sensor sensor : servico.listarSensores()) {

            System.out.println(
                    "\nSensor: " + sensor.getIdentificador()
            );

            System.out.println(
                    "Tipo: " + sensor.retornarTipo().getDescricao()
            );

            if (!sensor.verificarFuncionamento()) {
                System.out.println(
                        "Estado: INDISPONÍVEL"
                );
                continue;
            }

            System.out.println("Estado: OPERACIONAL");

            try {
                Medicao medicao = servico.registrarLeitura(
                        sensor.getIdentificador()
                );

                System.out.printf(
                        PT_BR,
                        "Leitura registrada: %.1f %s%n",
                        medicao.getValor(),
                        medicao.getTipoSensor().getUnidade()
                );

                List<Medicao> historico =
                        servico.consultarHistorico(
                                sensor.getIdentificador()
                        );

                int quantidade = historico.size();

                if (quantidade < 5) {
                    System.out.println(
                            "Histórico insuficiente para previsão: "
                                    + quantidade + "/5 leituras."
                    );
                    continue;
                }

                System.out.println(
                        "\nPrevisão automática pela média móvel:"
                );

                mostrarResultadoPrevisao(
                        sensor,
                        TipoPrevisao.MEDIA_MOVEL
                );

                if (quantidade >= 6) {
                    System.out.println(
                            "\nPrevisão automática por comparação:"
                    );

                    mostrarResultadoPrevisao(
                            sensor,
                            TipoPrevisao.COMPARACAO
                    );
                }

            } catch (
                    DadosInvalidosException
                    | FonteIndisponivelException e
            ) {
                System.out.println(
                        "Não foi possível processar o sensor: "
                                + e.getMessage()
                );
            }
        }
    }

    private void executarPrevisaoManual() {

        System.out.println(
                "\n=== EXECUTAR PREVISÃO ==="
        );

        Sensor sensor = selecionarSensor();

        if (sensor == null) {
            return;
        }

        System.out.println(
                "\n1 - Média móvel (5 leituras)"
        );

        System.out.println(
                "2 - Comparação entre períodos (6 leituras)"
        );

        System.out.print("Selecione a estratégia: ");

        String escolha = lerLinha();

        TipoPrevisao tipo = switch (escolha) {
            case "1" -> TipoPrevisao.MEDIA_MOVEL;
            case "2" -> TipoPrevisao.COMPARACAO;

            default -> throw new DadosInvalidosException(
                    "Estratégia de previsão inválida."
            );
        };

        mostrarResultadoPrevisao(sensor, tipo);
    }

    private void mostrarResultadoPrevisao(
            Sensor sensor,
            TipoPrevisao tipo
    ) {
        ResultadoPrevisao resultado =
                servico.executarPrevisao(
                        sensor.getIdentificador(),
                        tipo
                );

        NivelAlerta nivel =
                servico.classificarPrevisao(resultado);

        System.out.println(
                "Estratégia: " + tipo.getDescricao()
        );

        System.out.printf(
                PT_BR,
                "Valor previsto: %.1f %s%n",
                resultado.getValorPrevisto(),
                resultado.getTipoSensor().getUnidade()
        );

        System.out.println(
                "Classificação: " + nivel.getDescricao()
        );

        System.out.println(
                "Explicação: " + resultado.getExplicacao()
        );

        if (nivel == NivelAlerta.NORMAL) {
            System.out.println(
                    "Nenhum alerta necessário."
            );
        } else {
            System.out.println(
                    "ALERTA " + nivel.name()
                            + " gerado e salvo no banco."
            );
        }
    }

    private void consultarMedicoes() {

        System.out.println(
                "\n=== HISTÓRICO DE MEDIÇÕES ==="
        );

        System.out.println("1 - Histórico por sensor");
        System.out.println("2 - Todas as medições");
        System.out.print("Escolha: ");

        String escolha = lerLinha();

        List<Medicao> medicoes;

        if (escolha.equals("1")) {
            Sensor sensor = selecionarSensor();

            if (sensor == null) {
                return;
            }

            medicoes = servico.consultarHistorico(
                    sensor.getIdentificador()
            );

        } else if (escolha.equals("2")) {
            medicoes = servico.consultarTodasMedicoes();

        } else {
            throw new DadosInvalidosException(
                    "Opção de histórico inválida."
            );
        }

        if (medicoes.isEmpty()) {
            System.out.println(
                    "Nenhuma medição encontrada."
            );
            return;
        }

        System.out.println(
                "\nTotal de medições: " + medicoes.size()
        );

        for (Medicao medicao : medicoes) {
            System.out.printf(
                    PT_BR,
                    "#%d | %s | %s | %.1f %s | %s%n",
                    medicao.getId(),
                    medicao.getIdentificadorSensor(),
                    medicao.getTipoSensor().getDescricao(),
                    medicao.getValor(),
                    medicao.getTipoSensor().getUnidade(),
                    medicao.getRegistradaEm().format(FORMATO_DATA)
            );
        }
    }

    private void consultarAlertas() {

        System.out.println(
                "\n=== HISTÓRICO DE ALERTAS ==="
        );

        System.out.println("1 - Alertas por sensor");
        System.out.println("2 - Todos os alertas");
        System.out.print("Escolha: ");

        String escolha = lerLinha();

        List<Alerta> alertas;

        if (escolha.equals("1")) {
            Sensor sensor = selecionarSensor();

            if (sensor == null) {
                return;
            }

            alertas = servico.consultarAlertasPorSensor(
                    sensor.getIdentificador()
            );

        } else if (escolha.equals("2")) {
            alertas = servico.consultarAlertas();

        } else {
            throw new DadosInvalidosException(
                    "Opção de histórico inválida."
            );
        }

        if (alertas.isEmpty()) {
            System.out.println(
                    "Nenhum alerta registrado."
            );
            return;
        }

        System.out.println(
                "\nTotal de alertas: " + alertas.size()
        );

        for (Alerta alerta : alertas) {

            System.out.println(
                    "\n-------------------------------------"
            );

            System.out.println(
                    "Alerta #" + alerta.getId()
                            + " - " + alerta.getNivel().getDescricao()
            );

            System.out.println(
                    "Sensor: " + alerta.getIdentificadorSensor()
            );

            System.out.printf(
                    PT_BR,
                    "Valor previsto: %.1f %s%n",
                    alerta.getValorPrevisto(),
                    alerta.getTipoSensor().getUnidade()
            );

            System.out.printf(
                    PT_BR,
                    "Limiar: %.1f %s%n",
                    alerta.getLimiarUltrapassado(),
                    alerta.getTipoSensor().getUnidade()
            );

            System.out.println(
                    "Justificativa: " + alerta.getJustificativa()
            );

            System.out.println(
                    "Data: " + alerta.getRegistradoEm()
                            .format(FORMATO_DATA)
            );
        }
    }

    private void gerenciarLimiares() {

        System.out.println(
                "\n=== DADOS DO TERRITÓRIO ==="
        );

        for (TipoSensor tipo : TipoSensor.values()) {

            System.out.println(
                    "\n" + tipo.getDescricao()
            );

            System.out.printf(
                    PT_BR,
                    "Atenção: %.1f | Alerta: %.1f | "
                            + "Crítico: %.1f %s%n",
                    dadosTerritorio.getLimiarAtencao(tipo),
                    dadosTerritorio.getLimiarAlerta(tipo),
                    dadosTerritorio.getLimiarCritico(tipo),
                    tipo.getUnidade()
            );
        }

        System.out.print(
                "\nDeseja alterar os limiares? (s/n): "
        );

        String resposta = lerLinha();

        if (!resposta.equalsIgnoreCase("s")) {
            return;
        }

        Sensor sensor = selecionarSensor();

        if (sensor == null) {
            return;
        }

        TipoSensor tipo = sensor.retornarTipo();

        System.out.println(
                "\nAlterando: " + tipo.getDescricao()
        );

        double atencao = lerNumero(
                "Novo limite de atenção: "
        );

        double alerta = lerNumero(
                "Novo limite de alerta: "
        );

        double critico = lerNumero(
                "Novo limite crítico: "
        );

        System.out.print("Código de acesso: ");

        String codigo = lerLinha();

        dadosTerritorio.alterarLimiares(
                tipo,
                atencao,
                alerta,
                critico,
                codigo
        );

        System.out.println(
                "Limiares atualizados com sucesso."
        );
    }

    private void mostrarStatusCompleto() {

        System.out.println(
                "\n=== STATUS COMPLETO DO TERRITÓRIO ==="
        );

        System.out.println(
                "Região: " + regiao.getNome()
                        + " - " + regiao.getRegiao()
        );

        System.out.println(
                "Status dos sensores: " + regiao.verificarStatus()
        );

        System.out.println(
                "Sensores cadastrados: "
                        + servico.listarSensores().size()
        );

        for (Sensor sensor : servico.listarSensores()) {
            System.out.println(
                    "- " + sensor.getIdentificador()
                            + " | "
                            + sensor.retornarTipo().getDescricao()
                            + " | "
                            + (sensor.verificarFuncionamento()
                            ? "OPERACIONAL" : "INDISPONÍVEL")
            );
        }

        System.out.println(
                "\nMedições armazenadas: "
                        + servico.consultarTodasMedicoes().size()
        );

        System.out.println(
                "Alertas armazenados: "
                        + servico.consultarAlertas().size()
        );

        System.out.println(
                "\nObservação: o status operacional indica "
                        + "funcionamento dos sensores, "
                        + "não ausência de riscos territoriais."
        );
    }

    private Sensor selecionarSensor() {

        List<Sensor> sensores = servico.listarSensores();

        System.out.println("\nSensores disponíveis:");

        for (int i = 0; i < sensores.size(); i++) {
            Sensor sensor = sensores.get(i);

            System.out.println(
                    (i + 1) + " - "
                            + sensor.getIdentificador()
                            + " ("
                            + sensor.retornarTipo().getDescricao()
                            + ")"
            );
        }

        System.out.println("0 - Voltar");
        System.out.print("Selecione o sensor: ");

        String entrada = lerLinha();

        int indice;

        try {
            indice = Integer.parseInt(entrada);
        } catch (NumberFormatException e) {
            throw new DadosInvalidosException(
                    "Informe um número de sensor válido."
            );
        }

        if (indice == 0) {
            return null;
        }

        if (indice < 1 || indice > sensores.size()) {
            throw new DadosInvalidosException(
                    "Sensor selecionado não existe."
            );
        }

        return sensores.get(indice - 1);
    }

    private double lerNumero(String mensagem) {

        System.out.print(mensagem);

        String entrada = lerLinha()
                .replace(",", ".");

        try {
            return Double.parseDouble(entrada);
        } catch (NumberFormatException e) {
            throw new DadosInvalidosException(
                    "Informe um número válido."
            );
        }
    }

    private String lerLinha() {

        if (!scanner.hasNextLine()) {
            throw new DadosInvalidosException(
                    "A entrada do console foi encerrada."
            );
        }

        return scanner.nextLine().trim();
    }
}
