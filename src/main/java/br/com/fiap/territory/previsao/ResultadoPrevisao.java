package br.com.fiap.territory.previsao;

import br.com.fiap.territory.dominio.TipoSensor;
import br.com.fiap.territory.excecao.DadosInvalidosException;

public class ResultadoPrevisao {

    private final String identificadorSensor;
    private final TipoSensor tipoSensor;
    private final TipoPrevisao tipoPrevisao;
    private final double valorPrevisto;
    private final int quantidadeLeituras;
    private final String explicacao;

    public ResultadoPrevisao(
            String identificadorSensor,
            TipoSensor tipoSensor,
            TipoPrevisao tipoPrevisao,
            double valorPrevisto,
            int quantidadeLeituras,
            String explicacao
    ) {
        if (identificadorSensor == null
                || identificadorSensor.isBlank()) {
            throw new DadosInvalidosException(
                    "O identificador do sensor é obrigatório."
            );
        }

        if (tipoSensor == null || tipoPrevisao == null) {
            throw new DadosInvalidosException(
                    "O tipo de sensor e a estratégia são obrigatórios."
            );
        }

        if (!Double.isFinite(valorPrevisto)) {
            throw new DadosInvalidosException(
                    "O valor previsto deve ser finito."
            );
        }

        if (quantidadeLeituras <= 0) {
            throw new DadosInvalidosException(
                    "A quantidade de leituras deve ser positiva."
            );
        }

        if (explicacao == null || explicacao.isBlank()) {
            throw new DadosInvalidosException(
                    "A previsão deve possuir uma explicação."
            );
        }

        this.identificadorSensor = identificadorSensor;
        this.tipoSensor = tipoSensor;
        this.tipoPrevisao = tipoPrevisao;
        this.valorPrevisto = valorPrevisto;
        this.quantidadeLeituras = quantidadeLeituras;
        this.explicacao = explicacao;
    }

    public String getIdentificadorSensor() {
        return identificadorSensor;
    }

    public TipoSensor getTipoSensor() {
        return tipoSensor;
    }

    public TipoPrevisao getTipoPrevisao() {
        return tipoPrevisao;
    }

    public double getValorPrevisto() {
        return valorPrevisto;
    }

    public int getQuantidadeLeituras() {
        return quantidadeLeituras;
    }

    public String getExplicacao() {
        return explicacao;
    }
}
