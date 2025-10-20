package org.acme;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@Path("/api/v1/usuarios")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Usuários", description = "Gerenciamento de usuários")
public class UsuarioResource {

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
}