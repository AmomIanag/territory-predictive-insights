package br.com.fiap.territory.dominio;

public enum NivelAlerta {

    NORMAL("Normal"),
    ATENCAO("Atenção"),
    ALERTA("Alerta"),
    CRITICO("Crítico");

    private final String descricao;

    NivelAlerta(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
