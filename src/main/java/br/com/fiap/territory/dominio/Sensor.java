package br.com.fiap.territory.dominio;

public interface Sensor {

    String getIdentificador();

    double lerValor();

    boolean verificarFuncionamento();

    TipoSensor retornarTipo();
}
