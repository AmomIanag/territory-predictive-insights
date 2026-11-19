package br.com.fiap.territory;

import br.com.fiap.territory.dominio.*;
import br.com.fiap.territory.excecao.*;
import br.com.fiap.territory.persistencia.*;
import br.com.fiap.territory.previsao.*;
import br.com.fiap.territory.servico.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ServicoMonitoramentoTest {

    private MedicaoRepository medicaoRepository;
    private AlertaRepository alertaRepository;
    private ServicoMonitoramento servico;

    @BeforeEach
    void preparar() {

        medicaoRepository = mock(MedicaoRepository.class);
        alertaRepository = mock(AlertaRepository.class);

        servico = new ServicoMonitoramento(
                medicaoRepository,
                alertaRepository,
                new FabricaPrevisao(),
                new DadosTerritorio("4321")
        );
    }

    private List<Medicao> criarHistorico(
            String identificador,
            TipoSensor tipo,
            double... valores
    ) {
        List<Medicao> historico = new ArrayList<>();

        for (int i = 0; i < valores.length; i++) {
            historico.add(
                    new Medicao(
                            identificador,
                            tipo,
                            valores[i],
                            LocalDateTime.of(
                                    2026, 11, i + 1, 10, 0
                            )
                    )
            );
        }

        return historico;
    }

    @Test
    void deveRegistrarLeituraValida() {
        // Arrange
        Sensor sensor = mock(Sensor.class);

        when(sensor.getIdentificador())
                .thenReturn("TEMP-01");

        when(sensor.retornarTipo())
                .thenReturn(TipoSensor.TEMPERATURA);

        when(sensor.verificarFuncionamento())
                .thenReturn(true);

        when(sensor.lerValor())
                .thenReturn(36.5);

        when(medicaoRepository.save(any(Medicao.class)))
                .thenAnswer(invocacao ->
                        invocacao.getArgument(0)
                );

        servico.cadastrarSensor(sensor);

        // Act
        Medicao resultado = servico.registrarLeitura(
                "TEMP-01"
        );

        // Assert
        assertEquals(36.5, resultado.getValor());

        assertEquals(
                TipoSensor.TEMPERATURA,
                resultado.getTipoSensor()
        );

        verify(medicaoRepository, times(1))
                .save(any(Medicao.class));

        verifyNoInteractions(alertaRepository);
    }

    @Test
    void deveRejeitarSensorIndisponivel() {
        // Arrange
        Sensor sensor = mock(Sensor.class);

        when(sensor.getIdentificador())
                .thenReturn("TEMP-01");

        when(sensor.retornarTipo())
                .thenReturn(TipoSensor.TEMPERATURA);

        when(sensor.verificarFuncionamento())
                .thenReturn(false);

        servico.cadastrarSensor(sensor);

        // Act e Assert
        assertThrows(
                FonteIndisponivelException.class,
                () -> servico.registrarLeitura("TEMP-01")
        );

        verify(medicaoRepository, never())
                .save(any(Medicao.class));
    }

    @Test
    void deveRejeitarMedicaoInvalida() {
        // Arrange
        Sensor sensor = mock(Sensor.class);

        when(sensor.getIdentificador())
                .thenReturn("ENER-01");

        when(sensor.retornarTipo())
                .thenReturn(TipoSensor.CONSUMO_ENERGIA);

        when(sensor.verificarFuncionamento())
                .thenReturn(true);

        when(sensor.lerValor())
                .thenReturn(-5.0);

        servico.cadastrarSensor(sensor);

        // Act e Assert
        assertThrows(
                DadosInvalidosException.class,
                () -> servico.registrarLeitura("ENER-01")
        );

        verify(medicaoRepository, never())
                .save(any(Medicao.class));
    }

    @Test
    void deveGerarAlertaAutomaticamente() {
        // Arrange
        servico.cadastrarSensor(
                new SensorTemperatura("TEMP-01")
        );

        List<Medicao> historico = criarHistorico(
                "TEMP-01",
                TipoSensor.TEMPERATURA,
                36, 36, 36, 36, 36
        );

        when(medicaoRepository
                .findByIdentificadorSensorOrderByRegistradaEmDescIdDesc(
                        "TEMP-01"
                )).thenReturn(historico);

        when(alertaRepository.save(any(Alerta.class)))
                .thenAnswer(invocacao ->
                        invocacao.getArgument(0)
                );

        // Act
        ResultadoPrevisao resultado = servico.executarPrevisao(
                "TEMP-01",
                TipoPrevisao.MEDIA_MOVEL
        );

        // Assert
        assertEquals(36.0, resultado.getValorPrevisto());

        ArgumentCaptor<Alerta> captor =
                ArgumentCaptor.forClass(Alerta.class);

        verify(alertaRepository, times(1))
                .save(captor.capture());

        Alerta alerta = captor.getValue();

        assertEquals(NivelAlerta.ALERTA, alerta.getNivel());
        assertEquals(34.0, alerta.getLimiarUltrapassado());

        assertTrue(
                alerta.getJustificativa().contains("TEMP-01")
        );

        assertTrue(
                alerta.getJustificativa().contains("36,0")
        );

        assertTrue(
                alerta.getJustificativa().contains("34,0")
        );
    }

    @Test
    void deveGerarAlertaCriticoComComparacao() {
        // Arrange
        servico.cadastrarSensor(
                new SensorTemperatura("TEMP-01")
        );

        List<Medicao> historico = criarHistorico(
                "TEMP-01",
                TipoSensor.TEMPERATURA,
                30, 31, 32, 35, 36, 37
        );

        when(medicaoRepository
                .findByIdentificadorSensorOrderByRegistradaEmDescIdDesc(
                        "TEMP-01"
                )).thenReturn(historico);

        when(alertaRepository.save(any(Alerta.class)))
                .thenAnswer(invocacao ->
                        invocacao.getArgument(0)
                );

        // Act
        ResultadoPrevisao resultado = servico.executarPrevisao(
                "TEMP-01",
                TipoPrevisao.COMPARACAO
        );

        // Assert
        assertEquals(41.0, resultado.getValorPrevisto());

        ArgumentCaptor<Alerta> captor =
                ArgumentCaptor.forClass(Alerta.class);

        verify(alertaRepository)
                .save(captor.capture());

        assertEquals(
                NivelAlerta.CRITICO,
                captor.getValue().getNivel()
        );

        assertEquals(
                38.0,
                captor.getValue().getLimiarUltrapassado()
        );

        assertTrue(
                captor.getValue()
                        .getJustificativa()
                        .contains("COMPARACAO")
                        || captor.getValue()
                        .getJustificativa()
                        .contains("Comparação")
        );
    }

    @Test
    void naoDeveGerarAlertaQuandoNivelNormal() {
        // Arrange
        servico.cadastrarSensor(
                new SensorTemperatura("TEMP-01")
        );

        List<Medicao> historico = criarHistorico(
                "TEMP-01",
                TipoSensor.TEMPERATURA,
                27, 28, 29, 28, 28
        );

        when(medicaoRepository
                .findByIdentificadorSensorOrderByRegistradaEmDescIdDesc(
                        "TEMP-01"
                )).thenReturn(historico);

        // Act
        ResultadoPrevisao resultado = servico.executarPrevisao(
                "TEMP-01",
                TipoPrevisao.MEDIA_MOVEL
        );

        // Assert
        assertEquals(
                NivelAlerta.NORMAL,
                servico.classificarPrevisao(resultado)
        );

        verify(alertaRepository, never())
                .save(any(Alerta.class));
    }

    @Test
    void naoDeveAlertarComHistoricoInsuficiente() {
        // Arrange
        servico.cadastrarSensor(
                new SensorTemperatura("TEMP-01")
        );

        List<Medicao> historico = criarHistorico(
                "TEMP-01",
                TipoSensor.TEMPERATURA,
                30, 31, 32, 33
        );

        when(medicaoRepository
                .findByIdentificadorSensorOrderByRegistradaEmDescIdDesc(
                        "TEMP-01"
                )).thenReturn(historico);

        // Act e Assert
        assertThrows(
                DadosInvalidosException.class,
                () -> servico.executarPrevisao(
                        "TEMP-01",
                        TipoPrevisao.MEDIA_MOVEL
                )
        );

        verify(alertaRepository, never())
                .save(any(Alerta.class));
    }

    @Test
    void deveConsultarHistoricoPersistido() {
        // Arrange
        List<Medicao> historico = criarHistorico(
                "TEMP-01",
                TipoSensor.TEMPERATURA,
                30, 32, 34
        );

        when(medicaoRepository
                .findByIdentificadorSensorOrderByRegistradaEmDescIdDesc(
                        "TEMP-01"
                )).thenReturn(historico);

        // Act
        List<Medicao> resultado =
                servico.consultarHistorico("TEMP-01");

        // Assert
        assertEquals(3, resultado.size());

        verify(medicaoRepository, times(1))
                .findByIdentificadorSensorOrderByRegistradaEmDescIdDesc(
                        "TEMP-01"
                );
    }

    @Test
    void deveImpedirCadastroDuplicado() {
        // Arrange
        servico.cadastrarSensor(
                new SensorTemperatura("TEMP-01")
        );

        // Act e Assert
        assertThrows(
                DadosInvalidosException.class,
                () -> servico.cadastrarSensor(
                        new SensorTemperatura("TEMP-01")
                )
        );

        assertEquals(1, servico.listarSensores().size());
    }
}
