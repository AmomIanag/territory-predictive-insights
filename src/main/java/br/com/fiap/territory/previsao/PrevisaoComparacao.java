package br.com.fiap.territory.previsao;

import br.com.fiap.territory.dominio.Medicao;
import br.com.fiap.territory.dominio.TipoSensor;

import java.util.List;
import java.util.Locale;

public class PrevisaoComparacao extends EstrategiaPrevisao {

    private static final int TAMANHO_PERIODO = 3;
    private static final int MINIMO_LEITURAS = 6;

    public PrevisaoComparacao() {
        super(TipoPrevisao.COMPARACAO);
    }

    @Override
    public ResultadoPrevisao prever(List<Medicao> historico) {

        List<Medicao> ordenadas = prepararHistorico(
                historico,
                MINIMO_LEITURAS
        );

        List<Medicao> recentes = ordenadas.subList(0, 3);
        List<Medicao> anteriores = ordenadas.subList(3, 6);

        double mediaRecente = calcularMedia(recentes);
        double mediaAnterior = calcularMedia(anteriores);

        double variacao = mediaRecente - mediaAnterior;

        double projecaoBruta = mediaRecente + variacao;

        Medicao referencia = recentes.get(0);

        double projecaoLimitada = limitarValor(
                referencia.getTipoSensor(),
                projecaoBruta
        );

        double valorPrevisto = arredondarUmaCasa(
                projecaoLimitada
        );

        String explicacao = String.format(
                Locale.forLanguageTag("pt-BR"),
                "Comparação entre duas janelas de %d leituras. "
                        + "Média anterior: %.1f. "
                        + "Média recente: %.1f. "
                        + "Variação: %+.1f. "
                        + "A tendência foi projetada uma vez, "
                        + "respeitando a faixa do sensor.",
                TAMANHO_PERIODO,
                mediaAnterior,
                mediaRecente,
                variacao
        );

        return new ResultadoPrevisao(
                referencia.getIdentificadorSensor(),
                referencia.getTipoSensor(),
                getTipoPrevisao(),
                valorPrevisto,
                MINIMO_LEITURAS,
                explicacao
        );
    }

    private double calcularMedia(List<Medicao> medicoes) {
        return medicoes.stream()
                .mapToDouble(Medicao::getValor)
                .average()
                .orElseThrow();
    }

    private double limitarValor(
            TipoSensor tipo,
            double valor
    ) {
        return switch (tipo) {
            case TEMPERATURA ->
                    Math.max(-50.0, Math.min(70.0, valor));

            case CONSUMO_ENERGIA ->
                    Math.max(0.0, valor);

            case CARGA_REDE ->
                    Math.max(0.0, Math.min(100.0, valor));
        };
    }
}
