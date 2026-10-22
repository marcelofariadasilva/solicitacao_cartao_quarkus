package br.edu.fiap.cartoes.presentation.dto;
import br.edu.fiap.cartoes.domain.model.TipoCartao;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
/** Camada DTO: contrato HTTP pronto; não é entidade nem modelo de tabela. Tipo disponível retornado pela única rota JDBC. */
@Schema(description = "Tipo disponível retornado pela única rota JDBC.")
public record TipoCartaoResponse(
     @Schema(description = "Identificador do tipo", example = "1") Long id,
     @Schema(description = "Nome do tipo", example = "ESSENCIAL") String nome,
     @Schema(description = "Descrição", example = "Cartão para o dia a dia") String descricao,
     @Schema(description = "Pode ser solicitado", example = "true") boolean disponivel
) {
    public static TipoCartaoResponse de(TipoCartao tipo) {
        return new TipoCartaoResponse(tipo.getId(), tipo.getNome(), tipo.getDescricao(), tipo.isDisponivel());
    }
}
