package br.com.fiap.territory;

import br.com.fiap.territory.dominio.DadosTerritorio;
import br.com.fiap.territory.dominio.NivelAlerta;
import br.com.fiap.territory.dominio.TipoSensor;
import br.com.fiap.territory.excecao.DadosInvalidosException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DadosTerritorioTest {

    @Test
    void deveInicializarLimiaresPadrao() {
        // Arrange
        DadosTerritorio dados = new DadosTerritorio("4321");

        // Act
        double atencao = dados.getLimiarAtencao(
                TipoSensor.TEMPERATURA
        );

        double alerta = dados.getLimiarAlerta(
                TipoSensor.TEMPERATURA
        );

        double critico = dados.getLimiarCritico(
                TipoSensor.TEMPERATURA
        );

        // Assert
        assertEquals(30.0, atencao);
        assertEquals(34.0, alerta);
        assertEquals(38.0, critico);
    }

    @Test
    void deveClassificarOsQuatroNiveis() {
        // Arrange
        DadosTerritorio dados = new DadosTerritorio("4321");

        // Act e Assert
        assertEquals(
                NivelAlerta.NORMAL,
                dados.avaliarNivel(TipoSensor.TEMPERATURA, 29.9)
        );

        assertEquals(
                NivelAlerta.ATENCAO,
                dados.avaliarNivel(TipoSensor.TEMPERATURA, 30.0)
        );

        assertEquals(
                NivelAlerta.ALERTA,
                dados.avaliarNivel(TipoSensor.TEMPERATURA, 34.0)
        );

        assertEquals(
                NivelAlerta.CRITICO,
                dados.avaliarNivel(TipoSensor.TEMPERATURA, 38.0)
        );
    }

    @Test
    void deveClassificarCargaDaRede() {
        // Arrange
        DadosTerritorio dados = new DadosTerritorio("4321");

        // Act
        NivelAlerta nivel = dados.avaliarNivel(
                TipoSensor.CARGA_REDE,
                97.0
        );

        // Assert
        assertEquals(NivelAlerta.CRITICO, nivel);
    }

    @Test
    void devePermitirAlteracaoComCodigoCorreto() {
        // Arrange
        DadosTerritorio dados = new DadosTerritorio("4321");

        // Act
        dados.alterarLimiares(
                TipoSensor.TEMPERATURA,
                28.0,
                32.0,
                36.0,
                "4321"
        );

        // Assert
        assertEquals(
                28.0,
                dados.getLimiarAtencao(TipoSensor.TEMPERATURA)
        );

        assertEquals(
                32.0,
                dados.getLimiarAlerta(TipoSensor.TEMPERATURA)
        );

        assertEquals(
                36.0,
                dados.getLimiarCritico(TipoSensor.TEMPERATURA)
        );
    }

    @Test
    void deveBloquearAlteracaoComCodigoIncorreto() {
        // Arrange
        DadosTerritorio dados = new DadosTerritorio("4321");

        // Act e Assert
        assertThrows(
                DadosInvalidosException.class,
                () -> dados.alterarLimiares(
                        TipoSensor.TEMPERATURA,
                        28.0,
                        32.0,
                        36.0,
                        "9999"
                )
        );

        assertEquals(
                30.0,
                dados.getLimiarAtencao(TipoSensor.TEMPERATURA)
        );
    }

    @Test
    void deveRejeitarLimiaresForaDeOrdem() {
        // Arrange
        DadosTerritorio dados = new DadosTerritorio("4321");

        // Act e Assert
        assertThrows(
                DadosInvalidosException.class,
                () -> dados.alterarLimiares(
                        TipoSensor.CONSUMO_ENERGIA,
                        100.0,
                        80.0,
                        120.0,
                        "4321"
                )
        );

        assertEquals(
                70.0,
                dados.getLimiarAtencao(
                        TipoSensor.CONSUMO_ENERGIA
                )
        );
    }

    @Test
    void deveRejeitarLimiaresNaoFinitos() {
        // Arrange
        DadosTerritorio dados = new DadosTerritorio("4321");

        // Act e Assert
        assertThrows(
                DadosInvalidosException.class,
                () -> dados.alterarLimiares(
                        TipoSensor.TEMPERATURA,
                        Double.NaN,
                        34.0,
                        38.0,
                        "4321"
                )
        );

        assertThrows(
                DadosInvalidosException.class,
                () -> dados.alterarLimiares(
                        TipoSensor.TEMPERATURA,
                        30.0,
                        34.0,
                        Double.POSITIVE_INFINITY,
                        "4321"
                )
        );
    }

    @Test
    void deveRejeitarPrevisaoInvalida() {
        // Arrange
        DadosTerritorio dados = new DadosTerritorio("4321");

        // Act e Assert
        assertThrows(
                DadosInvalidosException.class,
                () -> dados.avaliarNivel(
                        TipoSensor.CONSUMO_ENERGIA,
                        -20.0
                )
        );

        assertThrows(
                DadosInvalidosException.class,
                () -> dados.avaliarNivel(
                        TipoSensor.TEMPERATURA,
                        Double.NaN
                )
        );
    }

    @Test
    void deveRejeitarTipoSensorNulo() {
        // Arrange
        DadosTerritorio dados = new DadosTerritorio("4321");

        // Act e Assert
        assertThrows(
                DadosInvalidosException.class,
                () -> dados.avaliarNivel(null, 35.0)
        );
    }

    @Test
    void deveRejeitarCodigoAcessoVazio() {
        // Arrange, Act e Assert
        assertThrows(
                DadosInvalidosException.class,
                () -> new DadosTerritorio("")
        );
    }
}
