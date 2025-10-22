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

import io.quarkus.hibernate.orm.panache.PanacheQuery; // Import já existente

import java.net.URI;
import java.time.LocalDateTime; // Import necessário para dataCompra
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Path("/api/v1/eventos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Eventos", description = "Operações relacionadas a eventos")
public class EventoResource {

    @Context
    UriInfo uriInfo;

    // Função auxiliar para converter Evento em EventoRepresentation (sem alterações)
    private EventoRepresentation rep(Evento e) {
        return EventoRepresentation.from(e, uriInfo);
    }

    // Função auxiliar para converter lista de Eventos (sem alterações)
    private List<EventoRepresentation> repList(List<Evento> eventos) {
        return eventos.stream().map(this::rep).collect(Collectors.toList());
    }

    @GET
    @Operation(summary = "Listar todos os eventos com paginação")
    public Response getAll(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size,
            @QueryParam("sort") @DefaultValue("nome") String sort,
            @QueryParam("direction") @DefaultValue("asc") String direction) {

        // Validação simples dos parâmetros de ordenação para evitar SQL Injection básico
        String sortField = sort.matches("^[a-zA-Z0-9_]+$") ? sort : "nome";
        String sortDirection = "desc".equalsIgnoreCase(direction) ? "desc" : "asc";
        String orderBy = "ORDER BY " + sortField + " " + sortDirection;

        PanacheQuery<Evento> query = Evento.find(orderBy);
        List<Evento> eventos = query.page(page, size).list();
        long totalCount = query.count();

        List<EventoRepresentation> representations = repList(eventos);
        PageResponse<EventoRepresentation> response =
                new PageResponse<>(representations, page, size, totalCount);

        return Response.ok(response).build();
    }

    @GET
    @Path("/search")
    @Operation(summary = "Buscar eventos com paginação e filtros avançados")
    public Response searchEvents(
            @QueryParam("nome") String nome,
            @QueryParam("local") String local,
            @QueryParam("status") Evento.StatusEvento status,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size,
            @QueryParam("sort") @DefaultValue("nome") String sort,
            @QueryParam("direction") @DefaultValue("asc") String direction) {

        // Validação simples dos parâmetros de ordenação
        String sortField = sort.matches("^[a-zA-Z0-9_]+$") ? sort : "nome";
        String sortDirection = "desc".equalsIgnoreCase(direction) ? "desc" : "asc";

        // Construir query dinâmica
        Map<String, Object> params = new HashMap<>();
        List<String> conditions = new ArrayList<>();

        if (nome != null && !nome.trim().isEmpty()) {
            conditions.add("lower(nome) LIKE lower(:nome)"); // Usar lower para case-insensitive
            params.put("nome", "%" + nome + "%");
        }

        if (local != null && !local.trim().isEmpty()) {
            conditions.add("lower(local) LIKE lower(:local)"); // Usar lower para case-insensitive
            params.put("local", "%" + local + "%");
        }

        if (status != null) {
            conditions.add("status = :status");
            params.put("status", status);
        }

        String whereClause = conditions.isEmpty() ? "" : "WHERE " + String.join(" AND ", conditions);
        String orderBy = "ORDER BY " + sortField + " " + sortDirection;
        String fullQuery = whereClause + " " + orderBy;

        // Consulta paginada
        PanacheQuery<Evento> panacheQuery = Evento.find(fullQuery, params);
        List<Evento> eventos = panacheQuery.page(page, size).list();
        long totalCount = panacheQuery.count(); // Conta total de acordo com os filtros

        List<EventoRepresentation> representations = repList(eventos);
        PageResponse<EventoRepresentation> response =
                new PageResponse<>(representations, page, size, totalCount);

        return Response.ok(response).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Buscar evento por ID")
    public Response getById(@PathParam("id") long id) {
        Evento entity = Evento.findById(id);
        if (entity == null) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("{\"message\": \"Evento não encontrado\"}")
                           .build();
        }
        return Response.ok(rep(entity)).build();
    }

    @POST
    @Operation(summary = "Criar novo evento")
    @Transactional
    public Response insert(@Valid Evento evento) {
        // Lógica para associar artistas existentes (se IDs forem enviados)
        if (evento.artistas != null && !evento.artistas.isEmpty()) {
            List<Artista> artistasPersistidos = new ArrayList<>();
            for (Artista artistaInput : evento.artistas) {
                // Assume que o JSON pode enviar apenas o ID do artista
                if (artistaInput != null && artistaInput.id != null) {
                    Artista artistaPersistido = Artista.findById(artistaInput.id);
                    if (artistaPersistido != null) {
                        artistasPersistidos.add(artistaPersistido);
                        // Garante a bidirecionalidade da relação
                        artistaPersistido.eventos.add(evento);
                    } else {
                        // Opcional: retornar erro se um ID de
