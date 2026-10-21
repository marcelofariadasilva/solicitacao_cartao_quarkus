package br.edu.fiap.cartoes.usecase;

import br.edu.fiap.cartoes.dto.CartaoResponse;
import br.edu.fiap.cartoes.entity.Cartao;
import br.edu.fiap.cartoes.entity.SolicitacaoCartao;
import br.edu.fiap.cartoes.entity.StatusSolicitacao;
import br.edu.fiap.cartoes.entity.TipoCartao;
import br.edu.fiap.cartoes.entity.Usuario;
import br.edu.fiap.cartoes.exception.RegraNegocioException;
import br.edu.fiap.cartoes.exception.RecursoNaoEncontradoException;
import br.edu.fiap.cartoes.repository.CartaoRepository;
import br.edu.fiap.cartoes.repository.SolicitacaoCartaoRepository;
import br.edu.fiap.cartoes.repository.TipoCartaoRepository;
import br.edu.fiap.cartoes.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SolicitacaoUseCaseTest {

    private SolicitacaoCartaoRepository solicitacaoRepository;
    private TipoCartaoRepository tipoCartaoRepository;
    private UsuarioRepository usuarioRepository;
    private CartaoRepository cartaoRepository;
    private SolicitacaoUseCase useCase;

    @BeforeEach
    void setup() {
        solicitacaoRepository = Mockito.mock(SolicitacaoCartaoRepository.class);
        tipoCartaoRepository = Mockito.mock(TipoCartaoRepository.class);
        usuarioRepository = Mockito.mock(UsuarioRepository.class);
        cartaoRepository = Mockito.mock(CartaoRepository.class);
        useCase = new SolicitacaoUseCase(
                solicitacaoRepository,
                tipoCartaoRepository,
                usuarioRepository,
                cartaoRepository
        );
    }

    @Test
    void deveEmitirComSucesso() {
        SolicitacaoCartao solicitacao = novaSolicitacao(1L, 7L, StatusSolicitacao.PENDENTE);
        when(solicitacaoRepository.buscarPorId(7L)).thenReturn(Optional.of(solicitacao));
        when(cartaoRepository.buscarPorSolicitacaoId(7L)).thenReturn(Optional.empty());
        when(cartaoRepository.salvar(any(Cartao.class))).thenAnswer(invocation -> {
            Cartao cartao = invocation.getArgument(0);
            cartao.setId(99L);
            return cartao;
        });

        CartaoResponse response = useCase.emitir(1L, 7L);

        assertEquals(99L, response.id());
        assertEquals(7L, response.solicitacaoId());
        assertEquals(StatusSolicitacao.EMITIDA, solicitacao.getStatus());
        verify(solicitacaoRepository).salvar(solicitacao);
        verify(cartaoRepository).salvar(any(Cartao.class));
    }

    @Test
    void deveRetornar404QuandoSolicitacaoNaoExiste() {
        when(solicitacaoRepository.buscarPorId(7L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> useCase.emitir(1L, 7L));
    }

    @Test
    void deveRetornar404QuandoSolicitacaoPertenceAOutroUsuario() {
        SolicitacaoCartao solicitacao = novaSolicitacao(2L, 7L, StatusSolicitacao.PENDENTE);
        when(solicitacaoRepository.buscarPorId(7L)).thenReturn(Optional.of(solicitacao));

        assertThrows(RecursoNaoEncontradoException.class, () -> useCase.emitir(1L, 7L));
    }

    @Test
    void deveRetornar409QuandoSolicitacaoCancelada() {
        SolicitacaoCartao solicitacao = novaSolicitacao(1L, 7L, StatusSolicitacao.CANCELADA);
        when(solicitacaoRepository.buscarPorId(7L)).thenReturn(Optional.of(solicitacao));
        when(cartaoRepository.buscarPorSolicitacaoId(7L)).thenReturn(Optional.empty());

        assertThrows(RegraNegocioException.class, () -> useCase.emitir(1L, 7L));
        verify(cartaoRepository, never()).salvar(any(Cartao.class));
    }

    @Test
    void deveRetornar409QuandoSolicitacaoJaEmitida() {
        SolicitacaoCartao solicitacao = novaSolicitacao(1L, 7L, StatusSolicitacao.EMITIDA);
        when(solicitacaoRepository.buscarPorId(7L)).thenReturn(Optional.of(solicitacao));
        when(cartaoRepository.buscarPorSolicitacaoId(7L)).thenReturn(Optional.of(Mockito.mock(Cartao.class)));

        assertThrows(RegraNegocioException.class, () -> useCase.emitir(1L, 7L));
        verify(cartaoRepository, never()).salvar(any(Cartao.class));
    }

    private SolicitacaoCartao novaSolicitacao(Long usuarioId, Long solicitacaoId, StatusSolicitacao status) {
        Usuario usuario = new Usuario("Maria", "maria@fiap.com.br", "SenhaForte123");
        usuario.setId(usuarioId);
        TipoCartao tipo = new TipoCartao("ESSENCIAL", "Cartão essencial", true);
        tipo.setId(3L);

        SolicitacaoCartao solicitacao = new SolicitacaoCartao(usuario, tipo);
        solicitacao.setId(solicitacaoId);
        solicitacao.setStatus(status);
        return solicitacao;
    }
}
