package br.com.fiap.territory;

import br.com.fiap.territory.dominio.DadosTerritorio;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TerritoryPredictiveInsightsFevApplication {

    public static void main(String[] args) {
        SpringApplication.run(
                TerritoryPredictiveInsightsFevApplication.class,
                args
        );
    }

    @Bean
    public DadosTerritorio dadosTerritorio(
            @Value("${territory.codigo-acesso}") String codigo
    ) {
        return new DadosTerritorio(codigo);
    }
}
