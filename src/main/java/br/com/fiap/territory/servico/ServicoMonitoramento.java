package br.com.fiap.territory.servico;

import br.com.fiap.territory.dominio.*;
import br.com.fiap.territory.excecao.*;
import br.com.fiap.territory.persistencia.*;
import br.com.fiap.territory.previsao.*;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ServicoMonitoramento {

    private final MedicaoRepository medicaoRepository;
    private final AlertaRepository alertaRepository;
    private final FabricaPrevisao fabricaPrevisao;
    private final DadosTerritorio dadosTerritorio;

    private final Map<String, Sensor> sensores;

    public ServicoMonitoramento(
            MedicaoRepository medicaoRepository,
            AlertaRepository alertaRepository,
            FabricaPrevisao fabricaPrevisao,
            DadosTerritorio dadosTerritorio
    ) {
        this.medicaoRepository = medicaoRepository;
        this.alertaRepository = alertaRepository;
        this.fabricaPrevisao = fabricaPrevisao;
        this.dadosTerritorio = dadosTerritorio;
        this.sensores = new LinkedHashMap<>();
    }

    public void cadastrarSensor(Sensor sensor) {

        if (sensor == null
                || sensor.getIdentificador() == null
                || sensor.getIdentificador().isBlank()
                || sensor.retornarTipo() == null) {

            throw new DadosInvalidosException(
                    "Sensor inválido para cadastro."
            );
        }

        String identificador = sensor.getIdentificador();

        if (sensores.containsKey(identificador)) {
            throw new DadosInvalidosException(
                    "Já existe um sensor cadastrado com ID: "
                            + identificador
            );
        }

        sensores.put(identificador, sensor);
    }

    public List<Sensor> listarSensores() {
        return List.copyOf(
                new ArrayList<>(sensores.values())
        );
    }

    private Sensor buscarSensor(String identificador) {

        if (identificador == null
                || identificador.isBlank()) {
            throw new DadosInvalidosException(
                    "O identificador do sensor é obrigatório."
            );
        }

        Sensor sensor = sensores.get(identificador);

        if (sensor == null) {
            throw new DadosInvalidosException(
                    "Sensor não cadastrado: " + identificador
            );
        }

        return sensor;
    }

    public Medicao registrarLeitura(String identificador) {

        Sensor sensor = buscarSensor(identificador);

        if (!sensor.verificarFuncionamento()) {
            throw new FonteIndisponivelException(
                    "O sensor " + identificador
                            + " está indisponível."
            );
        }

        double valor = sensor.lerValor();

        Medicao medicao = new Medicao(
                sensor.getIdentificador(),
                sensor.retornarTipo(),
                valor
        );

        return medicaoRepository.save(medicao);
    }

    public ResultadoPrevisao executarPrevisao(
            String identificador,
            TipoPrevisao tipoPrevisao
    ) {
        Sensor sensor = buscarSensor(identificador);

        EstrategiaPrevisao estrategia =
                fabricaPrevisao.criar(tipoPrevisao);

        List<Medicao> historico = consultarHistorico(
                identificador
        );

        ResultadoPrevisao resultado =
                estrategia.prever(historico);

        if (resultado.getTipoSensor()
                != sensor.retornarTipo()) {

            throw new DadosInvalidosException(
                    "O histórico possui tipo incompatível "
                            + "com o sensor cadastrado."
            );
        }

        NivelAlerta nivel = classificarPrevisao(resultado);

        if (nivel != NivelAlerta.NORMAL) {
            gerarAlerta(resultado, nivel);
        }

        return resultado;
    }

    public NivelAlerta classificarPrevisao(
            ResultadoPrevisao resultado
    ) {
        if (resultado == null) {
            throw new DadosInvalidosException(
                    "O resultado da previsão é obrigatório."
            );
        }

        return dadosTerritorio.avaliarNivel(
                resultado.getTipoSensor(),
                resultado.getValorPrevisto()
        );
    }

    private Alerta gerarAlerta(
            ResultadoPrevisao resultado,
            NivelAlerta nivel
    ) {
        double limiar = switch (nivel) {

            case ATENCAO -> dadosTerritorio.getLimiarAtencao(
                    resultado.getTipoSensor()
            );

            case ALERTA -> dadosTerritorio.getLimiarAlerta(
                    resultado.getTipoSensor()
            );

            case CRITICO -> dadosTerritorio.getLimiarCritico(
                    resultado.getTipoSensor()
            );

            case NORMAL -> throw new DadosInvalidosException(
                    "Não é permitido gerar alerta NORMAL."
            );
        };

        String justificativa = String.format(
                Locale.forLanguageTag("pt-BR"),
                "Sensor %s (%s): valor previsto %.1f %s; "
                        + "limiar %s: %.1f %s. "
                        + "Estratégia: %s. %s",
                resultado.getIdentificadorSensor(),
                resultado.getTipoSensor().getDescricao(),
                resultado.getValorPrevisto(),
                resultado.getTipoSensor().getUnidade(),
                nivel.name(),
                limiar,
                resultado.getTipoSensor().getUnidade(),
                resultado.getTipoPrevisao().getDescricao(),
                resultado.getExplicacao()
        );

        Alerta alerta = new Alerta(
                resultado.getIdentificadorSensor(),
                resultado.getTipoSensor(),
                nivel,
                resultado.getValorPrevisto(),
                limiar,
                justificativa
        );

        return alertaRepository.save(alerta);
    }

    public List<Medicao> consultarHistorico(
            String identificador
    ) {
        if (identificador == null
                || identificador.isBlank()) {
            throw new DadosInvalidosException(
                    "Informe o identificador do sensor."
            );
        }

        return medicaoRepository
                .findByIdentificadorSensorOrderByRegistradaEmDescIdDesc(
                        identificador
                );
    }

    public List<Medicao> consultarTodasMedicoes() {
        return medicaoRepository
                .findAllByOrderByRegistradaEmDescIdDesc();
    }

    public List<Alerta> consultarAlertas() {
        return alertaRepository
                .findAllByOrderByRegistradoEmDescIdDesc();
    }

    public List<Alerta> consultarAlertasPorSensor(
            String identificador
    ) {
        if (identificador == null
                || identificador.isBlank()) {
            throw new DadosInvalidosException(
                    "Informe o identificador do sensor."
            );
        }

        return alertaRepository
                .findByIdentificadorSensorOrderByRegistradoEmDescIdDesc(
                        identificador
                );
    }
}
