package br.com.fiap.territory.previsao;

public enum TipoPrevisao {

    MEDIA_MOVEL("Média móvel"),
    COMPARACAO("Comparação entre períodos");

    private final String descricao;

    TipoPrevisao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
