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
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import io.quarkus.hibernate.orm.panache.PanacheQuery; // ← IMPORT ADICIONADO

@Path("/api/v1/ingressos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Ingressos", description = "Operações relacionadas a ingressos")
public class IngressoResource {

    @Context
    UriInfo uriInfo;

    private IngressoRepresentation rep(Ingresso i) {
        return IngressoRepresentation.from(i, uriInfo);
    }

    private List<IngressoRepresentation> repList(List<Ingresso> ingressos) {
        return ingressos.stream().map(this::rep).collect(Collectors.toList());
    }

    @GET
    @Operation(summary = "Listar todos os ingressos com paginação")
    public Response getAll(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size,
            @QueryParam("sort") @DefaultValue("id") String sort,
            @QueryParam("direction") @DefaultValue("desc") String direction) {

        String orderBy = "ORDER BY " + sort + " " + direction;
        PanacheQuery<Ingresso> query = Ingresso.find(orderBy);
        List<Ingresso> ingressos = query.page(page, size).list();
        long totalCount = query.count();

        List<IngressoRepresentation> representations = repList(ingressos);
        PageResponse<IngressoRepresentation> response =
                new PageResponse<>(representations, page, size, totalCount);

        return Response.ok(response).build();
    }

    @GET
    @Path("/search")
    @Operation(summary = "Buscar ingressos com paginação e filtros avançados")
    public Response searchIngressos(
            @QueryParam("email") String email,
            @QueryParam("status") Ingresso.StatusIngresso status,
            @QueryParam("eventoId") Long eventoId,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size,
            @QueryParam("sort") @DefaultValue("id") String sort,
            @QueryParam("direction") @DefaultValue("desc") String direction) {

        // Construir query dinâmica
        StringBuilder queryBuilder = new StringBuilder();
        Map<String, Object> params = new HashMap<>();

        List<String> conditions = new java.util.ArrayList<>();

        if (email != null && !email.trim().isEmpty()) {
            conditions.add("emailComprador LIKE :email");
            params.put("email", "%" + email + "%");
        }

        if (status != null) {
            conditions.add("status = :status");
            params.put("status", status);
        }

        if (eventoId != null) {
            conditions.add("evento.id = :eventoId");
            params.put("eventoId", eventoId);
        }

        String whereClause = "";
        if (!conditions.isEmpty()) {
            whereClause = "WHERE " + String.join(" AND ", conditions);
        }

        String orderBy = "ORDER BY " + sort + " " + direction;
        String fullQuery = whereClause + " " + orderBy;

        // Consulta paginada
        PanacheQuery<Ingresso> panacheQuery = Ingresso.find(fullQuery, params);
        List<Ingresso> ingressos = panacheQuery.page(page, size).list();
        long totalCount = panacheQuery.count();

        List<IngressoRepresentation> representations = repList(ingressos);
        PageResponse<IngressoRepresentation> response =
                new PageResponse<>(representations, page, size, totalCount);

        return Response.ok(response).build();
    }

    @GET
    @Path("{id}")
    @Operation(summary = "Buscar ingresso por ID")
    public Response getById(@PathParam("id") long id) {
        Ingresso entity = Ingresso.findById(id);
        if (entity == null)
            return Response.status(404).build();
        return Response.ok(rep(entity)).build();
    }

    @POST
    @Operation(summary = "Criar novo ingresso")
    @Transactional
    public Response insert(@Valid CreateIngressoRequest request) {
        // Busque o evento pelo ID fornecido no request
        Evento evento = Evento.findById(request.getEventoId());
        if (evento == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Evento não encontrado").build();
        }

        // Verifique a capacidade do evento
        long ingressosVendidos = Ingresso.count("evento.id", evento.id);
        if (ingressosVendidos + request.getQuantidade() > evento.capacidadeMaxima) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Capacidade máxima do evento excedida").build();
        }

        // Crie uma nova entidade Ingresso a partir do request
        Ingresso ingresso = new Ingresso();
        ingresso.nomeComprador = request.getNomeComprador();
        ingresso.emailComprador = request.getEmailComprador();
        ingresso.quantidade = request.getQuantidade();
        ingresso.evento = evento;

        // Defina os valores controlados pelo servidor
        ingresso.dataCompra = LocalDateTime.now();
        ingresso.precoTotal = request.getQuantidade() * evento.precoIngresso;
        ingresso.status = Ingresso.StatusIngresso.RESERVADO;

        ingresso.persist();

        // Atualizar status do evento se necessário
        if (Ingresso.count("evento.id", evento.id) >= evento.capacidadeMaxima) {
            evento.status = Evento.StatusEvento.ESGOTADO;
        }

        return Response.created(URI.create("/api/v1/ingressos/" + ingresso.id)).entity(rep(ingresso)).build();
    }

    @PUT
    @Path("{id}")
    @Operation(summary = "Atualizar ingresso")
    @Transactional
    public Response update(@PathParam("id") long id, @Valid Ingresso newIngresso) {
        Ingresso entity = Ingresso.findById(id);
        if (entity == null)
            return Response.status(404).build();

        entity.nomeComprador = newIngresso.nomeComprador;
        entity.emailComprador = newIngresso.emailComprador;
        entity.quantidade = newIngresso.quantidade;
        entity.status = newIngresso.status;

        return Response.ok(rep(entity)).build();
    }

    @DELETE
    @Path("{id}")
    @Operation(summary = "Excluir ingresso")
    @Transactional
    public Response delete(@PathParam("id") long id) {
        Ingresso entity = Ingresso.findById(id);
        if (entity == null)
            return Response.status(404).build();

        Ingresso.deleteById(id);
        return Response.noContent().build();
    }

    @PUT
    @Path("{id}/status")
    @Operation(summary = "Atualizar status do ingresso")
    @Transactional
    public Response updateStatus(
            @PathParam("id") long id,
            @QueryParam("status") Ingresso.StatusIngresso novoStatus) {

        Ingresso ingresso = Ingresso.findById(id);
        if (ingresso == null) {
            return Response.status(404).build();
        }

        ingresso.status = novoStatus;
        return Response.ok(rep(ingresso)).build();
    }

    // Endpoints de busca simples (mantidos para compatibilidade)
    @GET
    @Path("/busca/email/{email}")
    @Operation(summary = "Buscar ingressos por email", description = "Busca ingressos pelo email do comprador")
    public Response buscarPorEmail(@PathParam("email") String email) {
        List<Ingresso> ingressos = Ingresso.list("emailComprador LIKE ?1", "%" + email + "%");
        return Response.ok(repList(ingressos)).build();
    }

    @GET
    @Path("/status/{status}")
    @Operation(summary = "Buscar ingressos por status", description = "Busca ingressos com o status especificado")
    public Response buscarPorStatus(@PathParam("status") Ingresso.StatusIngresso status) {
        List<Ingresso> ingressos = Ingresso.list("status", status);
        return Response.ok(repList(ingressos)).build();
    }
}