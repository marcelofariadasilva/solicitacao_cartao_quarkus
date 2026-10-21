package br.edu.fiap.cartoes.entity;

import br.edu.fiap.cartoes.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SolicitacaoCartaoTest {

    @Test
    void deveMarcarComoEmitidaQuandoPendente() {
        SolicitacaoCartao solicitacao = novaSolicitacao();

        solicitacao.marcarEmitida();

        assertEquals(StatusSolicitacao.EMITIDA, solicitacao.getStatus());
    }

    @Test
    void naoDeveMarcarComoEmitidaQuandoCancelada() {
        SolicitacaoCartao solicitacao = novaSolicitacao();
        solicitacao.cancelar();

        assertThrows(RegraNegocioException.class, solicitacao::marcarEmitida);
    }

    @Test
    void naoDeveMarcarComoEmitidaQuandoJaEmitida() {
        SolicitacaoCartao solicitacao = novaSolicitacao();
        solicitacao.marcarEmitida();

        assertThrows(RegraNegocioException.class, solicitacao::marcarEmitida);
    }

    private SolicitacaoCartao novaSolicitacao() {
        Usuario usuario = new Usuario("Joao", "joao@fiap.com.br", "SenhaForte123");
        TipoCartao tipo = new TipoCartao("ESSENCIAL", "Cartão essencial", true);
        return new SolicitacaoCartao(usuario, tipo);
    }
}
