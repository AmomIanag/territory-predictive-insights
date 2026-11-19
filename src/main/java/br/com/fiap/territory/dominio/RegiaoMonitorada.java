package br.com.fiap.territory.dominio;

import br.com.fiap.territory.excecao.DadosInvalidosException;

import java.util.ArrayList;
import java.util.List;

public class RegiaoMonitorada extends ComponenteTerritorial {

    private final List<Sensor> sensores;

    public RegiaoMonitorada(
            String id,
            String nome,
            String regiao
    ) {
        super(id, nome, regiao);
        this.sensores = new ArrayList<>();
    }

    public void adicionarSensor(Sensor sensor) {
        if (sensor == null) {
            throw new DadosInvalidosException(
                    "Não é possível adicionar um sensor nulo."
            );
        }

        boolean identificadorDuplicado = sensores.stream()
                .anyMatch(existente ->
                        existente.getIdentificador()
                                .equals(sensor.getIdentificador())
                );

        if (identificadorDuplicado) {
            throw new DadosInvalidosException(
                    "Já existe um sensor com o identificador: "
                            + sensor.getIdentificador()
            );
        }

        sensores.add(sensor);
    }

    public List<Sensor> getSensores() {
        return List.copyOf(sensores);
    }

    @Override
    public String verificarStatus() {

        if (sensores.isEmpty()) {
            atualizarStatus("SEM_SENSORES");
            return getStatus();
        }

        boolean algumSensorIndisponivel = sensores.stream()
                .anyMatch(sensor ->
                        !sensor.verificarFuncionamento()
                );

        if (algumSensorIndisponivel) {
            atualizarStatus("ATENCAO");
        } else {
            atualizarStatus("OPERACIONAL");
        }

        return getStatus();
    }
}
