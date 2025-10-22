package org.acme;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

// Imports necessários para as funções CRUD
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

    // Classe interna original para respostas (sem alterações)
    public static class UsuarioResponse {
        public Long id;
        public String nome;
        public String email;
        public String tipo;

        public UsuarioResponse(Usuario usuario) {
            this.id = usuario.id;
            this.nome = usuario.nome;
            this.email = usuario.email;
            this.tipo = (usuario.tipo != null) ? usuario.tipo.toString() : null; // Tratamento para tipo nulo
        }
    }

    // Classe interna para o payload de atualização (sem alterações)
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

        // Garante que o tipo default seja aplicado se não vier no JSON
        if (usuario.tipo == null) {
            usuario.tipo = Usuario.TipoUsuario.CLIENTE;
        }

        // A senha é hasheada automaticamente pelo setSenha da entidade Usuario
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
            // Retorna 404 Not Found se o usuário não existir
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("{\"message\": \"Usuário não encontrado\"}")
                           .build();
        }

        return Response.ok(new UsuarioResponse(usuario)).build();
    }

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
        if (request.email != null && !request.email.isBlank() && !request.email.equalsIgnoreCase(entity.email)) {
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

        // Persiste as alterações (embora o @Transactional possa fazer isso automaticamente)
        entity.persist();

        return Response.ok(new UsuarioResponse(entity)).build();
    }

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
        List<ApiKey> keys = ApiKey.find("usuario.id", id).list();
        for (ApiKey key : keys) {
            key.status = ApiKey.StatusApiKey.REVOGADA;
            // O @Transactional deve persistir a mudança no status da key
        }

        // 2. Deletar o usuário
        boolean deleted = Usuario.deleteById(id); // Usar deleteById é mais idiomático com Panache
        if (!deleted) {
             // Pode acontecer se houver algum problema de concorrência ou FK, embora raro aqui
             return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("{\"message\": \"Falha ao deletar usuário.\"}")
                            .build();
        }

        return Response.noContent().build();
    }
}
