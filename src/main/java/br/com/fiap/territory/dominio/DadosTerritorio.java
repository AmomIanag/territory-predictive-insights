package br.com.fiap.territory.dominio;

import br.com.fiap.territory.excecao.DadosInvalidosException;

import java.util.EnumMap;

public class DadosTerritorio {

    private final String codigoAcesso;

    private final EnumMap<TipoSensor, Limiares> limiaresPorSensor;

    public DadosTerritorio(String codigoAcesso) {

        if (codigoAcesso == null || codigoAcesso.isBlank()) {
            throw new DadosInvalidosException(
                    "O código de acesso não pode ser vazio."
            );
        }

        this.codigoAcesso = codigoAcesso;

        this.limiaresPorSensor = new EnumMap<>(TipoSensor.class);

        limiaresPorSensor.put(
                TipoSensor.TEMPERATURA,
                new Limiares(30.0, 34.0, 38.0)
        );

        limiaresPorSensor.put(
                TipoSensor.CONSUMO_ENERGIA,
                new Limiares(70.0, 90.0, 110.0)
        );

        limiaresPorSensor.put(
                TipoSensor.CARGA_REDE,
                new Limiares(70.0, 85.0, 95.0)
        );
    }

    public void alterarLimiares(
            TipoSensor tipo,
            double atencao,
            double alerta,
            double critico,
            String codigoInformado
    ) {
        validarCodigo(codigoInformado);
        validarTipo(tipo);
        validarLimiares(atencao, alerta, critico);

        limiaresPorSensor.put(
                tipo,
                new Limiares(atencao, alerta, critico)
        );
    }

    private void validarCodigo(String codigoInformado) {

        if (!codigoAcesso.equals(codigoInformado)) {
            throw new DadosInvalidosException(
                    "Código de acesso incorreto. Alteração negada."
            );
        }
    }

    private void validarTipo(TipoSensor tipo) {

        if (tipo == null) {
            throw new DadosInvalidosException(
                    "O tipo de sensor não pode ser nulo."
            );
        }
    }

    private void validarLimiares(
            double atencao,
            double alerta,
            double critico
    ) {
        if (!Double.isFinite(atencao)
                || !Double.isFinite(alerta)
                || !Double.isFinite(critico)) {

            throw new DadosInvalidosException(
                    "Os limiares devem ser números finitos."
            );
        }

        if (atencao < 0) {
            throw new DadosInvalidosException(
                    "O limiar de atenção não pode ser negativo."
            );
        }

        if (!(atencao < alerta && alerta < critico)) {
            throw new DadosInvalidosException(
                    "Os limites devem seguir a ordem: "
                            + "ATENÇÃO < ALERTA < CRÍTICO."
            );
        }
    }

    private Limiares buscarLimiares(TipoSensor tipo) {
        validarTipo(tipo);
        return limiaresPorSensor.get(tipo);
    }

    public double getLimiarAtencao(TipoSensor tipo) {
        return buscarLimiares(tipo).atencao;
    }

    public double getLimiarAlerta(TipoSensor tipo) {
        return buscarLimiares(tipo).alerta;
    }

    public double getLimiarCritico(TipoSensor tipo) {
        return buscarLimiares(tipo).critico;
    }

    public NivelAlerta avaliarNivel(
            TipoSensor tipo,
            double valorPrevisto
    ) {
        Limiares limiares = buscarLimiares(tipo);

        if (!Double.isFinite(valorPrevisto)) {
            throw new DadosInvalidosException(
                    "O valor previsto deve ser um número finito."
            );
        }

        if (tipo != TipoSensor.TEMPERATURA
                && valorPrevisto < 0) {

            throw new DadosInvalidosException(
                    "O valor previsto não pode ser negativo "
                            + "para este sensor."
            );
        }

        if (valorPrevisto >= limiares.critico) {
            return NivelAlerta.CRITICO;
        }

        if (valorPrevisto >= limiares.alerta) {
            return NivelAlerta.ALERTA;
        }

        if (valorPrevisto >= limiares.atencao) {
            return NivelAlerta.ATENCAO;
        }

        return NivelAlerta.NORMAL;
    }

    private static class Limiares {

        private final double atencao;
        private final double alerta;
        private final double critico;

        private Limiares(
                double atencao,
                double alerta,
                double critico
        ) {
            this.atencao = atencao;
            this.alerta = alerta;
            this.critico = critico;
        }
    }
}
