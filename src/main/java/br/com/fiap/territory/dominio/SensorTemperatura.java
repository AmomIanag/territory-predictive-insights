package br.com.fiap.territory.dominio;

import br.com.fiap.territory.excecao.DadosInvalidosException;
import br.com.fiap.territory.excecao.FonteIndisponivelException;

import java.util.Random;

public class SensorTemperatura implements Sensor {

    private final String identificador;
    private final Random random;
    private boolean funcionando;

    public SensorTemperatura(String identificador) {
        if (identificador == null || identificador.isBlank()) {
            throw new DadosInvalidosException(
                    "O identificador do sensor não pode ser vazio."
            );
        }

        this.identificador = identificador;
        this.random = new Random();
        this.funcionando = true;
    }

    @Override
    public String getIdentificador() {
        return identificador;
    }

    @Override
    public double lerValor() {
        if (!verificarFuncionamento()) {
            throw new FonteIndisponivelException(
                    "Sensor de temperatura indisponível: "
                            + identificador
            );
        }

        double valor = 25.0 + random.nextDouble() * 15.0;
        return Math.round(valor * 10.0) / 10.0;
    }

    @Override
    public boolean verificarFuncionamento() {
        return funcionando;
    }

    @Override
    public TipoSensor retornarTipo() {
        return TipoSensor.TEMPERATURA;
    }

    public void definirFuncionamento(boolean funcionando) {
        this.funcionando = funcionando;
    }
}
