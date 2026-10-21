
package br.edu.fiap.cartoes.entity;

import br.edu.fiap.cartoes.exception.RegraNegocioException;
import jakarta.persistence.*;
import java.time.Instant;

/**
 * Representa uma solicitação de cartão bancário.
 * Contém os dados e as regras de mudança de estado.
 */
@Entity
@Table(name = "solicitacoes_cartao")
public class SolicitacaoCartao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tipo_cartao_id", nullable = false)
    private TipoCartao tipoCartao;

    @Column(name = "solicitada_em", nullable = false)
    private Instant solicitadaEm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusSolicitacao status;

    /** Construtor exigido pelo JPA. */
    protected SolicitacaoCartao() {
    }

    /** Cria uma solicitação com estado inicial PENDENTE. */
    public SolicitacaoCartao(Usuario usuario, TipoCartao tipoCartao) {
        this.usuario = usuario;
        this.tipoCartao = tipoCartao;
        this.solicitadaEm = Instant.now();
        this.status = StatusSolicitacao.PENDENTE;
    }

    /** Permite cancelar somente solicitações pendentes. */
    public void cancelar() {

        if (status != StatusSolicitacao.PENDENTE) {
            throw new RegraNegocioException(
                    "Somente solicitações pendentes podem ser canceladas."
            );
        }

        this.status = StatusSolicitacao.CANCELADA;
    }

    public void marcarEmitida() {
        if (status != StatusSolicitacao.PENDENTE) {
            throw new RegraNegocioException(
                    "Somente solicitações pendentes podem ser emitidas."
            );
        }

        this.status = StatusSolicitacao.EMITIDA;
    }

    // Getters e setters originais

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public TipoCartao getTipoCartao() {
        return tipoCartao;
    }

    public void setTipoCartao(TipoCartao tipoCartao) {
        this.tipoCartao = tipoCartao;
    }

    public Instant getSolicitadaEm() {
        return solicitadaEm;
    }

    public void setSolicitadaEm(Instant solicitadaEm) {
        this.solicitadaEm = solicitadaEm;
    }

    public StatusSolicitacao getStatus() {
        return status;
    }

    public void setStatus(StatusSolicitacao status) {
        this.status = status;
    }
}
