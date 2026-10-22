package br.edu.fiap.cartoes.presentation.dto;
import java.time.Instant;

import br.edu.fiap.cartoes.domain.model.Cartao;
import br.edu.fiap.cartoes.domain.model.StatusCartao;
import br.edu.fiap.cartoes.domain.model.TipoCartao;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
/** Camada DTO: contrato HTTP pronto; não é entidade nem modelo de tabela. Cartão didático sem número real, CVV ou dados financeiros. */
@Schema(description = "Cartão didático sem número real, CVV ou dados financeiros.")
public record CartaoResponse(
     @Schema(description = "ID do cartão", example = "1") Long id,
     @Schema(description = "Pedido de origem único", example = "1") Long solicitacaoId,
     @Schema(description = "Tipo por meio da solicitação", example = "1") Long tipoCartaoId,
     @Schema(description = "Nome do tipo", example = "ESSENCIAL") String tipoCartaoNome,
     @Schema(description = "Data de emissão UTC", example = "2026-10-20T12:00:00Z") Instant emitidoEm,
     @Schema(description = "ATIVO ou BLOQUEADO", example = "ATIVO") StatusCartao status
) {
    public static CartaoResponse de(Cartao cartao) {
        return new CartaoResponse(cartao.getId(),cartao.getSolicitacao().getId(), cartao.getSolicitacao().getTipoCartao().getId(), cartao.getSolicitacao().getTipoCartao().getNome(), cartao.getEmitidoEm(),cartao.getStatus());
    }
}
