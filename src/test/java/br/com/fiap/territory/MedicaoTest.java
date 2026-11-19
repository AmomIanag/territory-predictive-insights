package br.com.fiap.territory;

import br.com.fiap.territory.dominio.Medicao;
import br.com.fiap.territory.dominio.TipoSensor;
import br.com.fiap.territory.excecao.DadosInvalidosException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MedicaoTest {

    @Test
    void deveCriarMedicaoValida() {

        String identificador = "TEMP-01";


        Medicao medicao = new Medicao(
                identificador,
                TipoSensor.TEMPERATURA,
                32.5
        );


        assertEquals(identificador, medicao.getIdentificadorSensor());
        assertEquals(TipoSensor.TEMPERATURA, medicao.getTipoSensor());
        assertEquals(32.5, medicao.getValor());
        assertNotNull(medicao.getRegistradaEm());
        assertNull(medicao.getId());
    }

    @Test
    void deveRejeitarConsumoNegativo() {

        assertThrows(
                DadosInvalidosException.class,
                () -> new Medicao(
                        "ENER-01",
                        TipoSensor.CONSUMO_ENERGIA,
                        -10.0
                )
        );
    }

    @Test
    void deveRejeitarCargaAcimaDeCem() {

        assertThrows(
                DadosInvalidosException.class,
                () -> new Medicao(
                        "CARGA-01",
                        TipoSensor.CARGA_REDE,
                        110.0
                )
        );
    }

    @Test
    void deveRejeitarValorNaoFinito() {

        assertThrows(
                DadosInvalidosException.class,
                () -> new Medicao(
                        "TEMP-01",
                        TipoSensor.TEMPERATURA,
                        Double.NaN
                )
        );
    }
}
