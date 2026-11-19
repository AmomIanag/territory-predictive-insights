package br.com.fiap.territory;

import br.com.fiap.territory.dominio.Medicao;
import br.com.fiap.territory.dominio.TipoSensor;
import br.com.fiap.territory.excecao.DadosInvalidosException;
import br.com.fiap.territory.previsao.*;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PrevisaoMediaMovelTest {

    private Medicao medicao(
            int dia,
            String identificador,
            TipoSensor tipo,
            double valor
    ) {
        return new Medicao(
                identificador,
                tipo,
                valor,
                LocalDateTime.of(2026, 11, dia, 12, 0)
        );
    }

    @Test
    void deveCalcularMediaDasCincoMaisRecentes() {
        // Arrange
        PrevisaoMediaMovel estrategia = new PrevisaoMediaMovel();

        List<Medicao> historico = List.of(
                medicao(1, "TEMP-01", TipoSensor.TEMPERATURA, 10),
                medicao(2, "TEMP-01", TipoSensor.TEMPERATURA, 30),
                medicao(3, "TEMP-01", TipoSensor.TEMPERATURA, 31),
                medicao(4, "TEMP-01", TipoSensor.TEMPERATURA, 32),
                medicao(5, "TEMP-01", TipoSensor.TEMPERATURA, 33),
                medicao(6, "TEMP-01", TipoSensor.TEMPERATURA, 34),
                medicao(7, "TEMP-01", TipoSensor.TEMPERATURA, 35)
        );

        // Act
        ResultadoPrevisao resultado = estrategia.prever(historico);

        // Assert
        assertEquals(33.0, resultado.getValorPrevisto());
        assertEquals(5, resultado.getQuantidadeLeituras());
        assertEquals(TipoPrevisao.MEDIA_MOVEL,
                resultado.getTipoPrevisao());
        assertFalse(resultado.getExplicacao().isBlank());
    }

    @Test
    void deveRejeitarHistoricoInsuficiente() {
        // Arrange
        PrevisaoMediaMovel estrategia = new PrevisaoMediaMovel();

        List<Medicao> historico = List.of(
                medicao(1, "TEMP-01", TipoSensor.TEMPERATURA, 30),
                medicao(2, "TEMP-01", TipoSensor.TEMPERATURA, 31),
                medicao(3, "TEMP-01", TipoSensor.TEMPERATURA, 32),
                medicao(4, "TEMP-01", TipoSensor.TEMPERATURA, 33)
        );

        // Act e Assert
        assertThrows(
                DadosInvalidosException.class,
                () -> estrategia.prever(historico)
        );
    }

    @Test
    void deveRejeitarHistoricoComSensoresDiferentes() {
        // Arrange
        PrevisaoMediaMovel estrategia = new PrevisaoMediaMovel();

        List<Medicao> historico = List.of(
                medicao(1, "TEMP-01", TipoSensor.TEMPERATURA, 30),
                medicao(2, "TEMP-01", TipoSensor.TEMPERATURA, 31),
                medicao(3, "TEMP-01", TipoSensor.TEMPERATURA, 32),
                medicao(4, "TEMP-01", TipoSensor.TEMPERATURA, 33),
                medicao(5, "ENER-01", TipoSensor.CONSUMO_ENERGIA, 80)
        );

        // Act e Assert
        assertThrows(
                DadosInvalidosException.class,
                () -> estrategia.prever(historico)
        );
    }
}
