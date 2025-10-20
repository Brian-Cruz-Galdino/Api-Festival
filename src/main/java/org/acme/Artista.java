package org.acme;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "artista")
public class Artista extends PanacheEntity {

    @NotBlank(message = "O nome do artista é obrigatório")
    @Size(min = 2, max = 50, message = "O nome deve ter entre 2 e 50 caracteres")
    public String nome;

    @NotBlank(message = "O gênero musical é obrigatório")
    @Column(name = "genero_musical")
    public String generoMusical;

    @Size(max = 500, message = "A biografia deve ter no máximo 500 caracteres")
    public String biografia;

    @ManyToMany(mappedBy = "artistas")
    @JsonIgnore
    public List<Evento> eventos = new ArrayList<>();

    // Construtor padrão
    public Artista() {
    }

    // Getters e Setters
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getGeneroMusical() { return generoMusical; }
    public void setGeneroMusical(String generoMusical) { this.generoMusical = generoMusical; }

    public String getBiografia() { return biografia; }
    public void setBiografia(String biografia) { this.biografia = biografia; }

    public List<Evento> getEventos() { return eventos; }
    public void setEventos(List<Evento> eventos) { this.eventos = eventos; }
}