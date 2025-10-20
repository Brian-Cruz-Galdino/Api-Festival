package org.acme;

import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Representação de um artista para API REST")
public class ArtistaRepresentation {

    @Schema(description = "ID único do artista")
    public Long id;

    @Schema(description = "Nome do artista")
    public String nome;

    @Schema(description = "Gênero musical")
    public String generoMusical;

    @Schema(description = "Biografia do artista")
    public String biografia;

    public ArtistaRepresentation() {
    }

    public static ArtistaRepresentation from(Artista artista, UriInfo uriInfo) {
        ArtistaRepresentation rep = new ArtistaRepresentation();
        rep.id = artista.id;
        rep.nome = artista.nome;
        rep.generoMusical = artista.generoMusical;
        rep.biografia = artista.biografia;

        return rep;
    }
}