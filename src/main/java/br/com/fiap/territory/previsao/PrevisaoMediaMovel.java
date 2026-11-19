package br.com.fiap.territory.previsao;

import br.com.fiap.territory.dominio.Medicao;

import java.util.List;

public class PrevisaoMediaMovel extends EstrategiaPrevisao {

    private static final int JANELA = 5;

    public PrevisaoMediaMovel() {
        super(TipoPrevisao.MEDIA_MOVEL);
    }

    @Override
    public ResultadoPrevisao prever(List<Medicao> historico) {

        List<Medicao> ordenadas = prepararHistorico(
                historico,
                JANELA
        );

        List<Medicao> recentes = ordenadas.subList(0, JANELA);

        double soma = recentes.stream()
                .mapToDouble(Medicao::getValor)
                .sum();

        double valorPrevisto = arredondarUmaCasa(
                soma / JANELA
        );

        Medicao referencia = recentes.get(0);

        String explicacao =
                "Estimativa pela média das últimas "
                        + JANELA
                        + " leituras do sensor. "
                        + "Representa o comportamento recente, "
                        + "sem considerar fatores externos.";

        return new ResultadoPrevisao(
                referencia.getIdentificadorSensor(),
                referencia.getTipoSensor(),
                getTipoPrevisao(),
                valorPrevisto,
                JANELA,
                explicacao
        );
    }
}
