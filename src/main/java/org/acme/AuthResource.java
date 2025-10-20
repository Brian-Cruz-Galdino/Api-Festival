package org.acme;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/api/v1/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Autenticação", description = "Operações de autenticação")
public class AuthResource {

    public static class LoginRequest {
        public String email;
        public String senha;
    }

    public static class LoginResponse {
        public Long usuarioId;
        public String nome;
        public String email;
        public Usuario.TipoUsuario tipo;

        public LoginResponse(Usuario usuario) {
            this.usuarioId = usuario.id;
            this.nome = usuario.nome;
            this.email = usuario.email;
            this.tipo = usuario.tipo;
        }
    }

    @POST
    @Path("/login")
    @Operation(summary = "Fazer login", description = "Autentica um usuário e retorna informações básicas")
    public Response login(@Valid LoginRequest loginRequest) {
        Usuario usuario = Usuario.findByEmail(loginRequest.email);

        if (usuario == null || !usuario.verificarSenha(loginRequest.senha)) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\": \"Credenciais inválidas\"}")
                    .build();
        }

        return Response.ok(new LoginResponse(usuario)).build();
    }
}