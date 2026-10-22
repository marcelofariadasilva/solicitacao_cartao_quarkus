package br.edu.fiap.cartoes.application.usecase;
import br.edu.fiap.cartoes.application.port.CartaoRepository;
import br.edu.fiap.cartoes.domain.exception.RecursoNaoEncontradoException;
import br.edu.fiap.cartoes.domain.exception.RegraNegocioException;
import br.edu.fiap.cartoes.domain.model.Cartao;
import br.edu.fiap.cartoes.domain.model.TipoCartao;
import br.edu.fiap.cartoes.presentation.dto.CartaoResponse;
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
public class CartaoUseCase {

    private final CartaoRepository entity;

    @Inject
    public CartaoUseCase(CartaoRepository entity) {
        this.entity = entity;
    }

    /** JPQL com relacionamento cartão -> solicitação -> usuário e tipo; somente cartões próprios. */
    public List<CartaoResponse> listar(Long usuarioId) {

        List<Cartao> cartoes = entity.listarPorUsuario(usuarioId);
        if (cartoes.isEmpty())
            throw new RecursoNaoEncontradoException("Não foi localizado cartões para o Usuario");

        return cartoes.stream().map(CartaoResponse::de).toList();
    }
}
