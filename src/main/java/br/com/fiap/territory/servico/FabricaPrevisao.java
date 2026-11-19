package br.com.fiap.territory.servico;

import br.com.fiap.territory.excecao.DadosInvalidosException;
import br.com.fiap.territory.previsao.*;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

@Component
public class FabricaPrevisao {

    private final Map<
            TipoPrevisao,
            Supplier<EstrategiaPrevisao>
            > criadores;

    public FabricaPrevisao() {
        criadores = new EnumMap<>(TipoPrevisao.class);

        criadores.put(
                TipoPrevisao.MEDIA_MOVEL,
                PrevisaoMediaMovel::new
        );

        criadores.put(
                TipoPrevisao.COMPARACAO,
                PrevisaoComparacao::new
        );
    }

    public EstrategiaPrevisao criar(TipoPrevisao tipo) {

        if (tipo == null) {
            throw new DadosInvalidosException(
                    "O tipo de previsão é obrigatório."
            );
        }

        Supplier<EstrategiaPrevisao> criador =
                criadores.get(tipo);

        if (criador == null) {
            throw new DadosInvalidosException(
                    "Estratégia não cadastrada: " + tipo
            );
        }

        return criador.get();
    }
}
