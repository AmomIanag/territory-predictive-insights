package br.com.fiap.territory.previsao;

import br.com.fiap.territory.dominio.Medicao;
import br.com.fiap.territory.dominio.TipoSensor;
import br.com.fiap.territory.excecao.DadosInvalidosException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class EstrategiaPrevisao {

    private final TipoPrevisao tipoPrevisao;

    protected EstrategiaPrevisao(TipoPrevisao tipoPrevisao) {
        if (tipoPrevisao == null) {
            throw new DadosInvalidosException(
                    "A estratégia de previsão é obrigatória."
            );
        }

        this.tipoPrevisao = tipoPrevisao;
    }

    public TipoPrevisao getTipoPrevisao() {
        return tipoPrevisao;
    }

    public abstract ResultadoPrevisao prever(
            List<Medicao> historico
    );

    protected List<Medicao> prepararHistorico(
            List<Medicao> historico,
            int minimoLeituras
    ) {
        if (historico == null
                || historico.size() < minimoLeituras) {
            throw new DadosInvalidosException(
                    "Histórico insuficiente para "
                            + tipoPrevisao.getDescricao()
                            + ". São necessárias pelo menos "
                            + minimoLeituras + " leituras."
            );
        }

        Medicao primeira = historico.get(0);

        if (primeira == null) {
            throw new DadosInvalidosException(
                    "O histórico contém medição nula."
            );
        }

        String identificador = primeira.getIdentificadorSensor();
        TipoSensor tipoSensor = primeira.getTipoSensor();

        for (Medicao medicao : historico) {

            if (medicao == null) {
                throw new DadosInvalidosException(
                        "O histórico contém medição nula."
                );
            }

            if (!identificador.equals(
                    medicao.getIdentificadorSensor()
            ) || tipoSensor != medicao.getTipoSensor()) {
                throw new DadosInvalidosException(
                        "O histórico deve conter medições "
                                + "de um único sensor."
                );
            }
        }

        List<Medicao> ordenadas = new ArrayList<>(historico);

        ordenadas.sort(
                Comparator.comparing(
                        Medicao::getRegistradaEm
                ).reversed()
        );

        return ordenadas;
    }

    protected double arredondarUmaCasa(double valor) {
        return Math.round(valor * 10.0) / 10.0;
    }
}
