package br.edu.fiap.cartoes.usecase;

import br.edu.fiap.cartoes.dto.CartaoResponse;
import br.edu.fiap.cartoes.dto.SolicitacaoRequest;
import br.edu.fiap.cartoes.dto.SolicitacaoResponse;
import br.edu.fiap.cartoes.entity.Cartao;
import br.edu.fiap.cartoes.entity.SolicitacaoCartao;
import br.edu.fiap.cartoes.entity.StatusSolicitacao;
import br.edu.fiap.cartoes.entity.TipoCartao;
import br.edu.fiap.cartoes.entity.Usuario;
import br.edu.fiap.cartoes.exception.RegraNegocioException; // ⚠️ nome da exceção 409
import br.edu.fiap.cartoes.exception.RecursoNaoEncontradoException;
import br.edu.fiap.cartoes.repository.CartaoRepository;
import br.edu.fiap.cartoes.repository.SolicitacaoCartaoRepository;
import br.edu.fiap.cartoes.repository.TipoCartaoRepository;
import br.edu.fiap.cartoes.repository.UsuarioRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class SolicitacaoUseCase {

    private final SolicitacaoCartaoRepository solicitacaoRepository;
    private final TipoCartaoRepository tipoCartaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CartaoRepository cartaoRepository;

    @Inject
    public SolicitacaoUseCase(SolicitacaoCartaoRepository solicitacaoRepository,
                              TipoCartaoRepository tipoCartaoRepository,
                              UsuarioRepository usuarioRepository,
                              CartaoRepository cartaoRepository) {
        this.solicitacaoRepository = solicitacaoRepository;
        this.tipoCartaoRepository = tipoCartaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.cartaoRepository = cartaoRepository;
    }

    /** Buscar titular e tipo por JPA; tipo indisponível ou pedido pendente duplicado gera 409. */
    @Transactional
    public SolicitacaoResponse criar(Long usuarioId, SolicitacaoRequest request) {
        Usuario usuario = usuarioRepository.buscarPorId(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));

        TipoCartao tipo = tipoCartaoRepository.buscarPorId(request.tipoCartaoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Tipo de cartão não encontrado"));

        if (!tipo.isDisponivel()) { // ⚠️ pode ser getDisponivel()
            throw new RegraNegocioException("Tipo de cartão indisponível");
        }

        boolean jaTemPendente = solicitacaoRepository
                .listarPorUsuarioEStatus(usuarioId, StatusSolicitacao.PENDENTE)
                .stream()
                .anyMatch(s -> s.getTipoCartao().getId().equals(tipo.getId()));

        if (jaTemPendente) {
            throw new RegraNegocioException("Já existe uma solicitação pendente para este tipo");
        }

        SolicitacaoCartao nova = new SolicitacaoCartao(usuario, tipo); // ⚠️ conferir construtor
        solicitacaoRepository.salvar(nova);

        return paraResponse(nova);
    }

    /** JPQL própria com filtro de titular e status opcional, nunca listar todos os usuários. */
    @Transactional
    public List<SolicitacaoResponse> listar(Long usuarioId, StatusSolicitacao status) {
        List<SolicitacaoCartao> solicitacoes = (status == null)
                ? solicitacaoRepository.listarPorUsuario(usuarioId)
                : solicitacaoRepository.listarPorUsuarioEStatus(usuarioId, status);

        return solicitacoes.stream()
                .map(this::paraResponse)
                .toList();
    }

    /** Buscar por id E titular; retornar 404 para pedido de outro usuário. */
    @Transactional
    public SolicitacaoResponse buscar(Long usuarioId, Long id) {
        return paraResponse(buscarDoTitular(usuarioId, id));
    }

    /** Em transação, buscar o próprio pedido e chamar cancelar; estado diferente de PENDENTE gera 409. */
    @Transactional
    public SolicitacaoResponse cancelar(Long usuarioId, Long id) {
        SolicitacaoCartao solicitacao = buscarDoTitular(usuarioId, id); // ⚠️ trocar pela busca com bloqueio
        solicitacao.cancelar(); // a entidade lança o 409 se não estiver PENDENTE
        solicitacaoRepository.salvar(solicitacao);
        return paraResponse(solicitacao);
    }

    @Transactional
    public CartaoResponse emitir(Long usuarioId, Long id) {
        SolicitacaoCartao solicitacao = buscarDoTitular(usuarioId, id);

        if (cartaoRepository.buscarPorSolicitacaoId(solicitacao.getId()).isPresent()) {
            throw new RegraNegocioException("Solicitação já emitida");
        }

        solicitacao.marcarEmitida();
        solicitacaoRepository.salvar(solicitacao);

        Cartao cartao = new Cartao(solicitacao);
        cartaoRepository.salvar(cartao);

        return new CartaoResponse(
                cartao.getId(),
                solicitacao.getId(),
                solicitacao.getTipoCartao().getId(),
                solicitacao.getTipoCartao().getNome(),
                cartao.getEmitidoEm(),
                cartao.getStatus()
        );
    }

    // ---------- auxiliares ----------

    /** Pedido inexistente ou de outro usuário: 404 nos dois casos. */
    private SolicitacaoCartao buscarDoTitular(Long usuarioId, Long id) {
        return solicitacaoRepository.buscarPorId(id)
                .filter(s -> s.getUsuario().getId().equals(usuarioId))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Solicitação não encontrada"));
    }

    private SolicitacaoResponse paraResponse(SolicitacaoCartao s) {
        return new SolicitacaoResponse( // ⚠️ conferir ordem dos campos do record
                s.getId(),
                s.getUsuario().getId(),
                s.getTipoCartao().getId(),
                s.getTipoCartao().getNome(),
                s.getSolicitadaEm(),
                s.getStatus()
        );
    }
}