package org.acme;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

// Imports que você precisa adicionar para as novas funções
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.DELETE;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@Path("/api/v1/usuarios")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Usuários", description = "Gerenciamento de usuários")
public class UsuarioResource {

    // Classe interna original para respostas
    public static class UsuarioResponse {
        public Long id;
        public String nome;
        public String email;
        public String tipo;

        public UsuarioResponse(Usuario usuario) {
            this.id = usuario.id;
            this.nome = usuario.nome;
            this.email = usuario.email;
            this.tipo = usuario.tipo.toString();
        }
    }
    
    // =======================================================
    // == NOVA CLASSE ADICIONADA PARA O UPDATE (EDIÇÃO) ======
    // =======================================================
    /**
     * Classe DTO usada para receber atualizações de usuário.
     * Não inclui a senha, que não deve ser atualizada por este endpoint.
     */
    public static class UsuarioUpdateRequest {
        @Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres")
        public String nome;

        @Email(message = "O email deve ser válido")
        public String email;
        
        public Usuario.TipoUsuario tipo;
    }


    @POST
    @Operation(summary = "Criar usuário", description = "Cria um novo usuário no sistema")
    @Transactional
    public Response criarUsuario(@Valid Usuario usuario) {
        // Verificar se email já existe
        if (Usuario.findByEmail(usuario.email) != null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"message\": \"Email já cadastrado\"}")
                    .build();
        }
        
        // Se o tipo não for enviado no JSON, a entidade Usuario usará o default 'CLIENTE'
        if (usuario.tipo == null) {
            usuario.tipo = Usuario.TipoUsuario.CLIENTE;
        }

        // A senha já é hasheada pela entidade Usuario (método setSenha)
        usuario.persist();
        return Response.created(URI.create("/api/v1/usuarios/" + usuario.id))
                .entity(new UsuarioResponse(usuario))
                .build();
    }

    @GET
    @Operation(summary = "Listar usuários", description = "Retorna todos os usuários cadastrados")
    public Response listarUsuarios() {
        List<Usuario> usuarios = Usuario.listAll();
        List<UsuarioResponse> responses = usuarios.stream()
                .map(UsuarioResponse::new)
                .collect(Collectors.toList());

        return Response.ok(responses).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Buscar usuário por ID", description = "Retorna um usuário específico")
    public Response buscarUsuario(@PathParam("id") Long id) {
        Usuario usuario = Usuario.findById(id);
        if (usuario == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(new UsuarioResponse(usuario)).build();
    }
    
    // =======================================================
    // == NOVO MÉTODO ADICIONADO (PUT / EDITAR) ==============
    // =======================================================
    @PUT
    @Path("/{id}")
    @Operation(summary = "Atualizar usuário", description = "Atualiza nome, email e tipo de um usuário")
    @Transactional
    public Response atualizarUsuario(@PathParam("id") Long id, @Valid UsuarioUpdateRequest request) {
        Usuario entity = Usuario.findById(id);
        if (entity == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("{\"message\": \"Usuário não encontrado\"}").build();
        }

        // Verifica se o novo email já existe em OUTRO usuário
        if (request.email != null && !request.email.isBlank() && !request.email.equals(entity.email)) {
            Usuario existing = Usuario.findByEmail(request.email);
            if (existing != null && !existing.id.equals(entity.id)) {
                return Response.status(Response.Status.CONFLICT).entity("{\"message\": \"Email já cadastrado por outro usuário\"}").build();
            }
            entity.email = request.email;
        }
        
        if (request.nome != null && !request.nome.isBlank()) {
            entity.nome = request.nome;
        }
        if (request.tipo != null) {
            entity.tipo = request.tipo;
        }
        
        // Nota: Não atualizamos a senha aqui por segurança.

        return Response.ok(new UsuarioResponse(entity)).build();
    }

    // =======================================================
    // == NOVO MÉTODO ADICIONADO (DELETE / EXCLUIR) ==========
    // =======================================================
    @DELETE
    @Path("/{id}")
    @Operation(summary = "Excluir usuário", description = "Exclui um usuário e revoga todas as suas API keys")
    @Transactional
    public Response deletarUsuario(@PathParam("id") Long id) {
        Usuario entity = Usuario.findById(id);
        if (entity == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("{\"message\": \"Usuário não encontrado\"}").build();
        }
        
        // 1. Revogar API keys associadas
        // (Baseado no seu ApiKey.java)
        List<ApiKey> keys = ApiKey.find("usuario.id", id).list();
        for (ApiKey key : keys) {
            key.status = ApiKey.StatusApiKey.REVOGADA; 
        }
        
        // 2. Deletar o usuário
        entity.delete();
        
        return Response.noContent().build();
    }
}
