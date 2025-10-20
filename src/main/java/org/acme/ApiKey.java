package org.acme;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "api_key")
public class ApiKey extends PanacheEntity {

    @Column(unique = true, nullable = false)
    public String chave;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    public Usuario usuario;

    @Column(name = "data_criacao")
    public LocalDateTime dataCriacao;

    @Column(name = "data_expiracao")
    public LocalDateTime dataExpiracao;

    @Enumerated(EnumType.STRING)
    public StatusApiKey status = StatusApiKey.ATIVA;

    public enum StatusApiKey {
        ATIVA,
        INATIVA,
        EXPIRADA,
        REVOGADA
    }

    // Construtores
    public ApiKey() {
        this.dataCriacao = LocalDateTime.now();
    }

    public ApiKey(String chave, Usuario usuario) {
        this();
        this.chave = chave;
        this.usuario = usuario;
        this.dataExpiracao = LocalDateTime.now().plusMonths(1);
    }

    // Método estático para buscar por chave
    public static ApiKey findByChave(String chave) {
        return find("chave = ?1 and status = ?2", chave, StatusApiKey.ATIVA).firstResult();
    }

    // Verificar se a chave está expirada
    public boolean isExpirada() {
        return dataExpiracao != null && LocalDateTime.now().isAfter(dataExpiracao);
    }
}