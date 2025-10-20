package org.acme;

import org.eclipse.microprofile.openapi.annotations.Components;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeIn;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

import jakarta.ws.rs.core.Application;

@OpenAPIDefinition(
        info = @Info(
                title = "API de Gerenciamento de Eventos",
                version = "1.0.0",
                description = "API para gerenciamento de eventos, artistas e ingressos de festivais"
        ),
        security = @SecurityRequirement(name = "apiKey"),
        components = @Components(
                securitySchemes = {
                        @SecurityScheme(
                                securitySchemeName = "apiKey",
                                type = SecuritySchemeType.APIKEY,
                                apiKeyName = "X-API-Key",
                                in = SecuritySchemeIn.HEADER
                        )
                }
        )
)
public class OpenAPIConfig extends Application {
}