package br.com.fiap.territory;

import br.com.fiap.territory.dominio.*;
import br.com.fiap.territory.persistencia.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:territory_test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class PersistenciaRepositoryTest {

    @Autowired
    private MedicaoRepository medicaoRepository;

    @Autowired
    private AlertaRepository alertaRepository;

    @Test
    void deveSalvarEConsultarHistoricoPorSensor() {
        // Arrange
        Medicao medicao = new Medicao(
                "TEMP-DB-01",
                TipoSensor.TEMPERATURA,
                36.2
        );

        // Act
        Medicao salva = medicaoRepository.saveAndFlush(medicao);

        List<Medicao> historico = medicaoRepository
                .findByIdentificadorSensorOrderByRegistradaEmDescIdDesc(
                        "TEMP-DB-01"
                );

        // Assert
        assertNotNull(salva.getId());
        assertEquals(1, historico.size());
        assertEquals(36.2, historico.get(0).getValor());
        assertEquals(
                TipoSensor.TEMPERATURA,
                historico.get(0).getTipoSensor()
        );
    }

    @Test
    void deveSalvarEConsultarAlertas() {
        // Arrange
        Alerta alerta = new Alerta(
                "CARGA-DB-01",
                TipoSensor.CARGA_REDE,
                NivelAlerta.CRITICO,
                98.0,
                95.0,
                "Carga prevista de 98% ultrapassou "
                        + "o limite crítico de 95%."
        );

        // Act
        Alerta salvo = alertaRepository.saveAndFlush(alerta);

        List<Alerta> historico = alertaRepository
                .findByIdentificadorSensorOrderByRegistradoEmDescIdDesc(
                        "CARGA-DB-01"
                );

        // Assert
        assertNotNull(salvo.getId());
        assertEquals(1, historico.size());
        assertEquals(
                NivelAlerta.CRITICO,
                historico.get(0).getNivel()
        );
        assertEquals(98.0, historico.get(0).getValorPrevisto());
        assertFalse(historico.get(0).getJustificativa().isBlank());
    }
}
