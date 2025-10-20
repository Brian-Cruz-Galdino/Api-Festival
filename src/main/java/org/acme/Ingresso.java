package org.acme;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ingresso")
public class Ingresso extends PanacheEntity {

    @NotBlank(message = "O nome do comprador é obrigatório")
    @Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres")
    @Column(name = "nome_comprador")
    public String nomeComprador;

    @NotBlank(message = "O email do comprador é obrigatório")
    @Email(message = "O email deve ser válido")
    @Column(name = "email_comprador")
    public String emailComprador;

    @Column(name = "data_compra")
    public LocalDateTime dataCompra;

    @NotNull(message = "A quantidade é obrigatória")
    @Min(value = 1, message = "A quantidade deve ser pelo menos 1")
    public Integer quantidade;

    @Column(name = "preco_total")
    public Double precoTotal;

    @Enumerated(EnumType.STRING)
    public StatusIngresso status = StatusIngresso.RESERVADO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id")
    public Evento evento;

    public enum StatusIngresso {
        RESERVADO,
        PAGO,
        CANCELADO,
        UTILIZADO
    }

    // Construtor padrão
    public Ingresso() {
    }

    // Getters e Setters
    public String getNomeComprador() { return nomeComprador; }
    public void setNomeComprador(String nomeComprador) { this.nomeComprador = nomeComprador; }

    public String getEmailComprador() { return emailComprador; }
    public void setEmailComprador(String emailComprador) { this.emailComprador = emailComprador; }

    public LocalDateTime getDataCompra() { return dataCompra; }
    public void setDataCompra(LocalDateTime dataCompra) { this.dataCompra = dataCompra; }

    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }

    public Double getPrecoTotal() { return precoTotal; }
    public void setPrecoTotal(Double precoTotal) { this.precoTotal = precoTotal; }

    public StatusIngresso getStatus() { return status; }
    public void setStatus(StatusIngresso status) { this.status = status; }

    public Evento getEvento() { return evento; }
    public void setEvento(Evento evento) { this.evento = evento; }
}