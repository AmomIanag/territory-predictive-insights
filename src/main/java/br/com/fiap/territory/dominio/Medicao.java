package br.com.fiap.territory.dominio;

import br.com.fiap.territory.excecao.DadosInvalidosException;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "medicoes")
public class Medicao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "identificador_sensor", nullable = false)
    private String identificadorSensor;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_sensor", nullable = false)
    private TipoSensor tipoSensor;

    @Column(nullable = false)
    private double valor;

    @Column(name = "registrada_em", nullable = false)
    private LocalDateTime registradaEm;

    protected Medicao() {
        // Construtor exigido pelo JPA
    }

    public Medicao(
            String identificadorSensor,
            TipoSensor tipoSensor,
            double valor
    ) {
        this(
                identificadorSensor,
                tipoSensor,
                valor,
                LocalDateTime.now()
        );
    }

    public Medicao(
            String identificadorSensor,
            TipoSensor tipoSensor,
            double valor,
            LocalDateTime registradaEm
    ) {
        if (identificadorSensor == null
                || identificadorSensor.isBlank()) {
            throw new DadosInvalidosException(
                    "O identificador do sensor é obrigatório."
            );
        }

        if (tipoSensor == null) {
            throw new DadosInvalidosException(
                    "O tipo do sensor é obrigatório."
            );
        }

        if (registradaEm == null) {
            throw new DadosInvalidosException(
                    "A data da medição é obrigatória."
            );
        }

        validarValor(tipoSensor, valor);

        this.identificadorSensor = identificadorSensor.trim();
        this.tipoSensor = tipoSensor;
        this.valor = valor;
        this.registradaEm = registradaEm;
    }

    private void validarValor(
            TipoSensor tipoSensor,
            double valor
    ) {
        if (!Double.isFinite(valor)) {
            throw new DadosInvalidosException(
                    "A medição deve ser um número finito."
            );
        }

        switch (tipoSensor) {

            case TEMPERATURA -> {
                if (valor < -50 || valor > 70) {
                    throw new DadosInvalidosException(
                            "Temperatura fora da faixa válida."
                    );
                }
            }

            case CONSUMO_ENERGIA -> {
                if (valor < 0) {
                    throw new DadosInvalidosException(
                            "O consumo não pode ser negativo."
                    );
                }
            }

            case CARGA_REDE -> {
                if (valor < 0 || valor > 100) {
                    throw new DadosInvalidosException(
                            "A carga da rede deve estar "
                                    + "entre 0% e 100%."
                    );
                }
            }
        }
    }

    public Long getId() {
        return id;
    }

    public String getIdentificadorSensor() {
        return identificadorSensor;
    }

    public TipoSensor getTipoSensor() {
        return tipoSensor;
    }

    public double getValor() {
        return valor;
    }

    public LocalDateTime getRegistradaEm() {
        return registradaEm;
    }
}
