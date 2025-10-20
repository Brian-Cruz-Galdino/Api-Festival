package org.acme;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "evento")
public class Evento extends PanacheEntity {

    @NotBlank(message = "O nome do evento é obrigatório")
    @Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres")
    public String nome;

    @Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres")
    public String descricao;

    @NotNull(message = "A data do evento é obrigatória")
    @Future(message = "A data do evento deve ser futura")
    @Column(name = "data_evento")
    public LocalDate dataEvento;

    @NotBlank(message = "O local do evento é obrigatório")
    public String local;

    @NotNull(message = "A capacidade máxima é obrigatória")
    @Min(value = 1, message = "A capacidade deve ser pelo menos 1")
    @Column(name = "capacidade_maxima")
    public Integer capacidadeMaxima;

    @NotNull(message = "O preço do ingresso é obrigatório")
    @DecimalMin(value = "0.0", inclusive = false, message = "O preço deve ser maior que zero")
    @Column(name = "preco_ingresso")
    public Double precoIngresso;

    @Enumerated(EnumType.STRING)
    public StatusEvento status = StatusEvento.DISPONIVEL;

    @ManyToMany
    @JoinTable(
            name = "evento_artista",
            joinColumns = @JoinColumn(name = "evento_id"),
            inverseJoinColumns = @JoinColumn(name = "artista_id")
    )
    public List<Artista> artistas = new ArrayList<>();

    @OneToMany(mappedBy = "evento", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    public List<Ingresso> ingressos = new ArrayList<>();

    public enum StatusEvento {
        DISPONIVEL,
        ESGOTADO,
        CANCELADO,
        ADIADO
    }

    // Construtor padrão
    public Evento() {
    }

    // Getters e Setters
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public LocalDate getDataEvento() { return dataEvento; }
    public void setDataEvento(LocalDate dataEvento) { this.dataEvento = dataEvento; }

    public String getLocal() { return local; }
    public void setLocal(String local) { this.local = local; }

    public Integer getCapacidadeMaxima() { return capacidadeMaxima; }
    public void setCapacidadeMaxima(Integer capacidadeMaxima) { this.capacidadeMaxima = capacidadeMaxima; }

    public Double getPrecoIngresso() { return precoIngresso; }
    public void setPrecoIngresso(Double precoIngresso) { this.precoIngresso = precoIngresso; }

    public StatusEvento getStatus() { return status; }
    public void setStatus(StatusEvento status) { this.status = status; }

    public List<Artista> getArtistas() { return artistas; }
    public void setArtistas(List<Artista> artistas) { this.artistas = artistas; }

    public List<Ingresso> getIngressos() { return ingressos; }
    public void setIngressos(List<Ingresso> ingressos) { this.ingressos = ingressos; }
}