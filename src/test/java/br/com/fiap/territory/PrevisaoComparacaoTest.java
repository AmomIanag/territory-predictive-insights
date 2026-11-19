package br.com.fiap.territory;

import br.com.fiap.territory.dominio.Medicao;
import br.com.fiap.territory.dominio.TipoSensor;
import br.com.fiap.territory.excecao.DadosInvalidosException;
import br.com.fiap.territory.previsao.*;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PrevisaoComparacaoTest {

    private Medicao temperatura(int dia, double valor) {
        return new Medicao(
                "TEMP-01",
                TipoSensor.TEMPERATURA,
                valor,
                LocalDateTime.of(2026, 11, dia, 12, 0)
        );
    }

    private Medicao cargaRede(int dia, double valor) {
        return new Medicao(
                "CARGA-01",
                TipoSensor.CARGA_REDE,
                valor,
                LocalDateTime.of(2026, 11, dia, 12, 0)
        );
    }

    @Test
    void deveProjetarTendenciaDeAlta() {
        // Arrange
        PrevisaoComparacao estrategia = new PrevisaoComparacao();

        List<Medicao> historico = List.of(
                temperatura(1, 30),
                temperatura(2, 31),
                temperatura(3, 32),
                temperatura(4, 35),
                temperatura(5, 36),
                temperatura(6, 37)
        );

        // Act
        ResultadoPrevisao resultado = estrategia.prever(historico);

        // Assert
        assertEquals(41.0, resultado.getValorPrevisto());
        assertEquals(6, resultado.getQuantidadeLeituras());
        assertEquals(TipoPrevisao.COMPARACAO,
                resultado.getTipoPrevisao());
    }

    @Test
    void deveProjetarTendenciaDeQueda() {
        // Arrange
        PrevisaoComparacao estrategia = new PrevisaoComparacao();

        List<Medicao> historico = List.of(
                temperatura(1, 40),
                temperatura(2, 39),
                temperatura(3, 38),
                temperatura(4, 35),
                temperatura(5, 34),
                temperatura(6, 33)
        );

        // Act
        ResultadoPrevisao resultado = estrategia.prever(historico);

        // Assert
        assertEquals(29.0, resultado.getValorPrevisto());
    }

    @Test
    void deveLimitarCargaDaRedeACemPorCento() {
        // Arrange
        PrevisaoComparacao estrategia = new PrevisaoComparacao();

        List<Medicao> historico = List.of(
                cargaRede(1, 70),
                cargaRede(2, 72),
                cargaRede(3, 74),
                cargaRede(4, 95),
                cargaRede(5, 97),
                cargaRede(6, 99)
        );

        // Act
        ResultadoPrevisao resultado = estrategia.prever(historico);

        // Assert
        assertEquals(100.0, resultado.getValorPrevisto());
    }

    @Test
    void deveRejeitarHistoricoInsuficiente() {
        // Arrange
        PrevisaoComparacao estrategia = new PrevisaoComparacao();

        List<Medicao> historico = List.of(
                temperatura(1, 30),
                temperatura(2, 31),
                temperatura(3, 32),
                temperatura(4, 33),
                temperatura(5, 34)
        );

        // Act e Assert
        assertThrows(
                DadosInvalidosException.class,
                () -> estrategia.prever(historico)
        );
    }
}
