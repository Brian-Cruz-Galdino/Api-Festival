package org.acme;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class ApiKeyAuthFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getPath();

        // Rotas públicas que não precisam de API Key
        if (isPublicRoute(path, requestContext.getMethod())) {
            return;
        }

        String apiKey = requestContext.getHeaderString("X-API-Key");

        if (apiKey == null || apiKey.isBlank()) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\": \"API key obrigatória\"}").build());
            return;
        }

        ApiKey key = ApiKey.findByChave(apiKey);
        if (key == null || key.isExpirada()) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\": \"API key inválida ou expirada\"}").build());
        }
    }

    private boolean isPublicRoute(String path, String method) {
        // Rotas públicas
        return path.contains("/q/") ||
                (path.equals("/api/v1/auth/login") && "POST".equals(method)) ||
                (path.equals("/api/v1/usuarios") && "POST".equals(method)) ||
                (path.startsWith("/api/v1/apikeys/generate") && "POST".equals(method));
    }
}