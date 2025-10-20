package org.acme;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.mindrot.jbcrypt.BCrypt;

@Entity
@Table(name = "usuario")
public class Usuario extends PanacheEntity {

    @NotBlank(message = "O nome é obrigatório")
    @Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres")
    public String nome;

    @NotBlank(message = "O email é obrigatório")
    @Email(message = "O email deve ser válido")
    @Column(unique = true)
    public String email;

    private String senha;

    @Enumerated(EnumType.STRING)
    public TipoUsuario tipo = TipoUsuario.CLIENTE;

    public enum TipoUsuario {
        CLIENTE,
        ADMIN,
        ORGANIZADOR
    }

    // Construtores
    public Usuario() {}

    public Usuario(String nome, String email, String senha) {
        this.nome = nome;
        this.email = email;
        this.setSenha(senha);
    }

    // Getters e Setters
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) {
        if (senha != null && !senha.trim().isEmpty()) {
            this.senha = BCrypt.hashpw(senha, BCrypt.gensalt());
        }
    }

    public TipoUsuario getTipo() { return tipo; }
    public void setTipo(TipoUsuario tipo) { this.tipo = tipo; }

    // Método para verificar senha
    public boolean verificarSenha(String senha) {
        return BCrypt.checkpw(senha, this.senha);
    }

    // Método estático para buscar por email
    public static Usuario findByEmail(String email) {
        return find("email", email).firstResult();
    }
}