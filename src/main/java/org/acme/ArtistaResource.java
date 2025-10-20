package org.acme;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import io.quarkus.hibernate.orm.panache.PanacheQuery; // ← IMPORT ADICIONADO

@Path("/api/v1/artistas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Artistas", description = "Operações relacionadas a artistas")
public class ArtistaResource {

    @Context
    UriInfo uriInfo;

    private ArtistaRepresentation rep(Artista a) {
        return ArtistaRepresentation.from(a, uriInfo);
    }

    private List<ArtistaRepresentation> repList(List<Artista> artistas) {
        return artistas.stream().map(this::rep).collect(Collectors.toList());
    }

    @GET
    @Operation(summary = "Listar todos os artistas com paginação")
    public Response getAll(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size,
            @QueryParam("sort") @DefaultValue("nome") String sort,
            @QueryParam("direction") @DefaultValue("asc") String direction) {

        String orderBy = "ORDER BY " + sort + " " + direction;
        PanacheQuery<Artista> query = Artista.find(orderBy);
        List<Artista> artistas = query.page(page, size).list();
        long totalCount = query.count();

        List<ArtistaRepresentation> representations = repList(artistas);
        PageResponse<ArtistaRepresentation> response =
                new PageResponse<>(representations, page, size, totalCount);

        return Response.ok(response).build();
    }

    @GET
    @Path("/search")
    @Operation(summary = "Buscar artistas com paginação e filtros avançados")
    public Response searchArtistas(
            @QueryParam("nome") String nome,
            @QueryParam("genero") String genero,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size,
            @QueryParam("sort") @DefaultValue("nome") String sort,
            @QueryParam("direction") @DefaultValue("asc") String direction) {

        // Construir query dinâmica
        StringBuilder queryBuilder = new StringBuilder();
        Map<String, Object> params = new HashMap<>();

        List<String> conditions = new java.util.ArrayList<>();

        if (nome != null && !nome.trim().isEmpty()) {
            conditions.add("nome LIKE :nome");
            params.put("nome", "%" + nome + "%");
        }

        if (genero != null && !genero.trim().isEmpty()) {
            conditions.add("generoMusical LIKE :genero");
            params.put("genero", "%" + genero + "%");
        }

        String whereClause = "";
        if (!conditions.isEmpty()) {
            whereClause = "WHERE " + String.join(" AND ", conditions);
        }

        String orderBy = "ORDER BY " + sort + " " + direction;
        String fullQuery = whereClause + " " + orderBy;

        // Consulta paginada
        PanacheQuery<Artista> panacheQuery = Artista.find(fullQuery, params);
        List<Artista> artistas = panacheQuery.page(page, size).list();
        long totalCount = panacheQuery.count();

        List<ArtistaRepresentation> representations = repList(artistas);
        PageResponse<ArtistaRepresentation> response =
                new PageResponse<>(representations, page, size, totalCount);

        return Response.ok(response).build();
    }

    @GET
    @Path("{id}")
    @Operation(summary = "Buscar artista por ID")
    public Response getById(@PathParam("id") long id) {
        Artista entity = Artista.findById(id);
        if (entity == null)
            return Response.status(404).build();
        return Response.ok(rep(entity)).build();
    }

    @POST
    @Operation(summary = "Criar novo artista")
    @Transactional
    public Response insert(@Valid Artista artista) {
        artista.persist();
        return Response.created(URI.create("/api/v1/artistas/" + artista.id)).entity(rep(artista)).build();
    }

    @PUT
    @Path("{id}")
    @Operation(summary = "Atualizar artista")
    @Transactional
    public Response update(@PathParam("id") long id, @Valid Artista newArtista) {
        Artista entity = Artista.findById(id);
        if (entity == null)
            return Response.status(404).build();

        entity.nome = newArtista.nome;
        entity.generoMusical = newArtista.generoMusical;
        entity.biografia = newArtista.biografia;

        return Response.ok(rep(entity)).build();
    }

    @DELETE
    @Path("{id}")
    @Operation(summary = "Excluir artista")
    @Transactional
    public Response delete(@PathParam("id") long id) {
        Artista entity = Artista.findById(id);
        if (entity == null)
            return Response.status(404).build();

        Artista.deleteById(id);
        return Response.noContent().build();
    }

    @GET
    @Path("{id}/eventos")
    @Operation(summary = "Listar eventos do artista")
    public Response getEventosByArtista(@PathParam("id") long id) {
        Artista artista = Artista.findById(id);
        if (artista == null) {
            return Response.status(404).build();
        }

        List<EventoRepresentation> representations = artista.eventos.stream()
                .map(evento -> EventoRepresentation.from(evento, uriInfo))
                .collect(Collectors.toList());

        return Response.ok(representations).build();
    }

    // Endpoints de busca simples (mantidos para compatibilidade)
    @GET
    @Path("/busca/nome/{nome}")
    @Operation(summary = "Buscar artistas por nome", description = "Busca artistas que contenham o nome especificado")
    public Response buscarPorNome(@PathParam("nome") String nome) {
        List<Artista> artistas = Artista.list("nome LIKE ?1", "%" + nome + "%");
        return Response.ok(repList(artistas)).build();
    }

    @GET
    @Path("/genero/{genero}")
    @Operation(summary = "Buscar artistas por gênero", description = "Busca artistas do gênero musical especificado")
    public Response buscarPorGenero(@PathParam("genero") String genero) {
        List<Artista> artistas = Artista.list("generoMusical LIKE ?1", "%" + genero + "%");
        return Response.ok(repList(artistas)).build();
    }
}