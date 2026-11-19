package br.com.fiap.territory;

import br.com.fiap.territory.dominio.*;
import br.com.fiap.territory.excecao.*;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SensorTest {

    @Test
    void deveLerValoresDentroDasFaixas() {
        // Arrange
        Sensor temperatura = new SensorTemperatura("TEMP-01");
        Sensor energia = new SensorConsumoEnergia("ENER-01");
        Sensor carga = new SensorCargaRede("CARGA-01");

        // Act
        double valorTemperatura = temperatura.lerValor();
        double valorEnergia = energia.lerValor();
        double valorCarga = carga.lerValor();

        // Assert
        assertTrue(valorTemperatura >= 25 && valorTemperatura <= 40);
        assertTrue(valorEnergia >= 40 && valorEnergia <= 120);
        assertTrue(valorCarga >= 50 && valorCarga <= 100);
    }

    @Test
    void deveLancarExcecaoQuandoSensorFalha() {
        // Arrange
        SensorTemperatura sensor = new SensorTemperatura("TEMP-01");
        sensor.definirFuncionamento(false);

        // Act e Assert
        assertThrows(
                FonteIndisponivelException.class,
                sensor::lerValor
        );
    }

    @Test
    void deveIdentificarRegiaoOperacional() {
        // Arrange
        RegiaoMonitorada regiao = new RegiaoMonitorada(
                "REG-01",
                "Zona Norte",
                "São Paulo"
        );
        regiao.adicionarSensor(new SensorTemperatura("TEMP-01"));

        // Act
        String status = regiao.verificarStatus();

        // Assert
        assertEquals("OPERACIONAL", status);
    }

    @Test
    void deveIdentificarRegiaoComSensorIndisponivel() {
        // Arrange
        RegiaoMonitorada regiao = new RegiaoMonitorada(
                "REG-01",
                "Zona Norte",
                "São Paulo"
        );

        SensorCargaRede sensor = new SensorCargaRede("CARGA-01");
        sensor.definirFuncionamento(false);
        regiao.adicionarSensor(sensor);

        // Act
        String status = regiao.verificarStatus();

        // Assert
        assertEquals("ATENCAO", status);
    }

    @Test
    void deveImpedirSensoresComIdentificadoresDuplicados() {
        // Arrange
        RegiaoMonitorada regiao = new RegiaoMonitorada(
                "REG-01",
                "Zona Norte",
                "São Paulo"
        );

        regiao.adicionarSensor(new SensorTemperatura("SENSOR-01"));

        // Act e Assert
        assertThrows(
                DadosInvalidosException.class,
                () -> regiao.adicionarSensor(
                        new SensorCargaRede("SENSOR-01")
                )
        );
    }

    @Test
    void deveRejeitarIdentificadorVazio() {
        // Arrange, Act e Assert
        assertThrows(
                DadosInvalidosException.class,
                () -> new SensorTemperatura("")
        );
    }
}
