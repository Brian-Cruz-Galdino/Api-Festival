package org.acme;

import jakarta.validation.constraints.*;

public class CreateIngressoRequest {

    @NotBlank(message = "O nome do comprador é obrigatório")
    @Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres")
    public String nomeComprador;

    @NotBlank(message = "O email do comprador é obrigatório")
    @Email(message = "O email deve ser válido")
    public String emailComprador;

    @NotNull(message = "A quantidade é obrigatória")
    @Min(value = 1, message = "A quantidade deve ser pelo menos 1")
    public Integer quantidade;

    @NotNull(message = "O ID do evento é obrigatório")
    public Long eventoId;

    // Getters e Setters
    public String getNomeComprador() { return nomeComprador; }
    public void setNomeComprador(String nomeComprador) { this.nomeComprador = nomeComprador; }

    public String getEmailComprador() { return emailComprador; }
    public void setEmailComprador(String emailComprador) { this.emailComprador = emailComprador; }

    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }

    public Long getEventoId() { return eventoId; }
    public void setEventoId(Long eventoId) { this.eventoId = eventoId; }
}