package br.com.fiap.territory;

import br.com.fiap.territory.dominio.Alerta;
import br.com.fiap.territory.dominio.NivelAlerta;
import br.com.fiap.territory.dominio.TipoSensor;
import br.com.fiap.territory.excecao.DadosInvalidosException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AlertaTest {

    @Test
    void deveCriarAlertaComJustificativa() {

        String justificativa =
                "Previsão de 39 °C ultrapassou o limite de 38 °C.";


        Alerta alerta = new Alerta(
                "TEMP-01",
                TipoSensor.TEMPERATURA,
                NivelAlerta.CRITICO,
                39.0,
                38.0,
                justificativa
        );


        assertEquals(NivelAlerta.CRITICO, alerta.getNivel());
        assertEquals(39.0, alerta.getValorPrevisto());
        assertEquals(38.0, alerta.getLimiarUltrapassado());
        assertEquals(justificativa, alerta.getJustificativa());
        assertNotNull(alerta.getRegistradoEm());
    }

    @Test
    void deveRejeitarNivelNormal() {

        assertThrows(
                DadosInvalidosException.class,
                () -> new Alerta(
                        "TEMP-01",
                        TipoSensor.TEMPERATURA,
                        NivelAlerta.NORMAL,
                        28.0,
                        30.0,
                        "Sem risco."
                )
        );
    }

    @Test
    void deveRejeitarJustificativaVazia() {

        assertThrows(
                DadosInvalidosException.class,
                () -> new Alerta(
                        "CARGA-01",
                        TipoSensor.CARGA_REDE,
                        NivelAlerta.CRITICO,
                        98.0,
                        95.0,
                        ""
                )
        );
    }
}
