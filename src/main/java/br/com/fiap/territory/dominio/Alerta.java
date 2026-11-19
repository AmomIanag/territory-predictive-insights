package br.com.fiap.territory.dominio;

import br.com.fiap.territory.excecao.DadosInvalidosException;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "alertas")
public class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "identificador_sensor", nullable = false)
    private String identificadorSensor;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_sensor", nullable = false)
    private TipoSensor tipoSensor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NivelAlerta nivel;

    @Column(name = "valor_previsto", nullable = false)
    private double valorPrevisto;

    @Column(name = "limiar_ultrapassado", nullable = false)
    private double limiarUltrapassado;

    @Column(nullable = false, length = 500)
    private String justificativa;

    @Column(name = "registrado_em", nullable = false)
    private LocalDateTime registradoEm;

    protected Alerta() {
        // Construtor exigido pelo JPA
    }

    public Alerta(
            String identificadorSensor,
            TipoSensor tipoSensor,
            NivelAlerta nivel,
            double valorPrevisto,
            double limiarUltrapassado,
            String justificativa
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

        if (nivel == null || nivel == NivelAlerta.NORMAL) {
            throw new DadosInvalidosException(
                    "Um alerta deve possuir nível "
                            + "ATENCAO, ALERTA ou CRITICO."
            );
        }

        if (!Double.isFinite(valorPrevisto)
                || !Double.isFinite(limiarUltrapassado)) {
            throw new DadosInvalidosException(
                    "Os valores do alerta devem ser finitos."
            );
        }

        if (limiarUltrapassado < 0
                || (tipoSensor != TipoSensor.TEMPERATURA
                && valorPrevisto < 0)) {
            throw new DadosInvalidosException(
                    "Valores negativos inválidos para o alerta."
            );
        }

        if (valorPrevisto < limiarUltrapassado) {
            throw new DadosInvalidosException(
                    "O valor previsto não atingiu "
                            + "o limiar informado."
            );
        }

        if (justificativa == null
                || justificativa.isBlank()
                || justificativa.length() > 500) {
            throw new DadosInvalidosException(
                    "A justificativa deve ter "
                            + "entre 1 e 500 caracteres."
            );
        }

        this.identificadorSensor = identificadorSensor.trim();
        this.tipoSensor = tipoSensor;
        this.nivel = nivel;
        this.valorPrevisto = valorPrevisto;
        this.limiarUltrapassado = limiarUltrapassado;
        this.justificativa = justificativa.trim();
        this.registradoEm = LocalDateTime.now();
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

    public NivelAlerta getNivel() {
        return nivel;
    }

    public double getValorPrevisto() {
        return valorPrevisto;
    }

    public double getLimiarUltrapassado() {
        return limiarUltrapassado;
    }

    public String getJustificativa() {
        return justificativa;
    }

    public LocalDateTime getRegistradoEm() {
        return registradoEm;
    }
}
