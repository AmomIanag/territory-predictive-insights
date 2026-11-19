package br.com.fiap.territory.dominio;

import br.com.fiap.territory.excecao.DadosInvalidosException;

public abstract class ComponenteTerritorial {

    private final String id;
    private final String nome;
    private final String regiao;
    private String status;

    protected ComponenteTerritorial(
            String id,
            String nome,
            String regiao
    ) {
        validarTexto(id, "ID");
        validarTexto(nome, "Nome");
        validarTexto(regiao, "Região");

        this.id = id;
        this.nome = nome;
        this.regiao = regiao;
        this.status = "NAO_VERIFICADO";
    }

    private void validarTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new DadosInvalidosException(
                    campo + " não pode ser vazio."
            );
        }
    }

    public abstract String verificarStatus();

    protected void atualizarStatus(String novoStatus) {
        validarTexto(novoStatus, "Status");
        this.status = novoStatus;
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getRegiao() {
        return regiao;
    }

    public String getStatus() {
        return status;
    }
}
