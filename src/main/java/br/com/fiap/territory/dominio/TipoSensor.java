package br.com.fiap.territory.dominio;

public enum TipoSensor {

    TEMPERATURA("Temperatura ambiente", "°C"),
    CONSUMO_ENERGIA("Consumo de energia", "kW"),
    CARGA_REDE("Carga da rede elétrica", "%");

    private final String descricao;
    private final String unidade;

    TipoSensor(String descricao, String unidade) {
        this.descricao = descricao;
        this.unidade = unidade;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getUnidade() {
        return unidade;
    }
}
