package br.com.fiap.territory;

import br.com.fiap.territory.excecao.DadosInvalidosException;
import br.com.fiap.territory.previsao.*;
import br.com.fiap.territory.servico.FabricaPrevisao;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FabricaPrevisaoTest {

    @Test
    void deveCriarEstrategiaMediaMovel() {
        // Arrange
        FabricaPrevisao fabrica = new FabricaPrevisao();

        // Act
        EstrategiaPrevisao estrategia = fabrica.criar(
                TipoPrevisao.MEDIA_MOVEL
        );

        // Assert
        assertNotNull(estrategia);
        assertEquals(
                TipoPrevisao.MEDIA_MOVEL,
                estrategia.getTipoPrevisao()
        );
    }

    @Test
    void deveCriarEstrategiaComparacao() {
        // Arrange
        FabricaPrevisao fabrica = new FabricaPrevisao();

        // Act
        EstrategiaPrevisao estrategia = fabrica.criar(
                TipoPrevisao.COMPARACAO
        );

        // Assert
        assertNotNull(estrategia);
        assertEquals(
                TipoPrevisao.COMPARACAO,
                estrategia.getTipoPrevisao()
        );
    }

    @Test
    void deveRejeitarTipoNulo() {
        // Arrange
        FabricaPrevisao fabrica = new FabricaPrevisao();

        // Act e Assert
        assertThrows(
                DadosInvalidosException.class,
                () -> fabrica.criar(null)
        );
    }
}
