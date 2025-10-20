package org.acme;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Path("/api/v1/apikeys")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "API Keys", description = "Gerenciamento de chaves de API")
public class ApiKeyResource {

    public static class ApiKeyResponse {
        public Long id;
        public String chave;
        public String dataCriacao;
        public String dataExpiracao;
        public String status;

        public ApiKeyResponse(ApiKey apiKey) {
            this.id = apiKey.id;
            this.chave = apiKey.chave;
            this.dataCriacao = apiKey.dataCriacao.toString();
            this.dataExpiracao = apiKey.dataExpiracao != null ? apiKey.dataExpiracao.toString() : null;
            this.status = apiKey.status.toString();
        }
    }

    @POST
    @Path("/generate/{usuarioId}")
    @Operation(summary = "Gerar nova API Key", description = "Gera uma nova chave de API para o usuário especificado")
    @Transactional
    public Response generateApiKey(@PathParam("usuarioId") Long usuarioId) {
        Usuario usuario = Usuario.findById(usuarioId);
        if (usuario == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"Usuário não encontrado\"}")
                    .build();
        }

        String apiKey = generateSecureKey();
        ApiKey novaChave = new ApiKey(apiKey, usuario);
        novaChave.persist();

        return Response.status(Response.Status.CREATED)
                .entity(new ApiKeyResponse(novaChave))
                .build();
    }

    @GET
    @Path("/usuario/{usuarioId}")
    @Operation(summary = "Listar API Keys do usuário", description = "Retorna todas as API Keys ativas do usuário")
    public Response listApiKeys(@PathParam("usuarioId") Long usuarioId) {
        List<ApiKey> apiKeys = ApiKey.find("usuario.id = ?1 and status = ?2",
                usuarioId, ApiKey.StatusApiKey.ATIVA).list();

        List<ApiKeyResponse> responses = apiKeys.stream()
                .map(ApiKeyResponse::new)
                .collect(Collectors.toList());

        return Response.ok(responses).build();
    }

    @DELETE
    @Path("/{apiKeyId}")
    @Operation(summary = "Revogar API Key", description = "Revoga uma API Key específica")
    @Transactional
    public Response revokeApiKey(@PathParam("apiKeyId") Long apiKeyId) {
        ApiKey apiKey = ApiKey.findById(apiKeyId);
        if (apiKey == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"API Key não encontrada\"}")
                    .build();
        }

        apiKey.status = ApiKey.StatusApiKey.REVOGADA;
        return Response.noContent().build();
    }

    private String generateSecureKey() {
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}