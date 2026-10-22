package br.edu.fiap.cartoes.application.usecase;
import br.edu.fiap.cartoes.application.port.TipoCartaoRepository;
import br.edu.fiap.cartoes.domain.model.TipoCartao;
import br.edu.fiap.cartoes.presentation.dto.TipoCartaoResponse;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

import br.edu.fiap.cartoes.domain.exception.EtapaNaoImplementadaException;
import jakarta.inject.Inject;

/**
 * Camada de aplicação a implementar: coordena uma operação, suas regras e transação.
 * O controller já chama estas assinaturas. Na migração para Clean, substituam
 * a dependência de DTO HTTP por entradas/saídas da aplicação e portas de saída.
 */
@ApplicationScoped
public class TipoCartaoUseCase {

    private final TipoCartaoRepository entity;

    @Inject
    public TipoCartaoUseCase(TipoCartaoRepository entity) {
        this.entity = entity;
    }

    /** Delegar à única consulta JDBC e mapear a resposta. */
    public List<TipoCartaoResponse> listarDisponiveis() {

        List<TipoCartao>  tipoCartao = entity.listarTodos();
        return tipoCartao.stream().map(TipoCartaoResponse::de).toList();

    }
}
