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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import io.quarkus.hibernate.orm.panache.PanacheQuery; // ← IMPORT ADICIONADO

@Path("/api/v1/eventos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Eventos", description = "Operações relacionadas a eventos")
public class EventoResource {

    @Context
    UriInfo uriInfo;

    private EventoRepresentation rep(Evento e) {
        return EventoRepresentation.from(e, uriInfo);
    }

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

        String orderBy = "ORDER BY " + sort + " " + direction;
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

        // Construir query dinâmica
        StringBuilder queryBuilder = new StringBuilder();
        Map<String, Object> params = new HashMap<>();

        List<String> conditions = new ArrayList<>();

        if (nome != null && !nome.trim().isEmpty()) {
            conditions.add("nome LIKE :nome");
            params.put("nome", "%" + nome + "%");
        }

        if (local != null && !local.trim().isEmpty()) {
            conditions.add("local LIKE :local");
            params.put("local", "%" + local + "%");
        }

        if (status != null) {
            conditions.add("status = :status");
            params.put("status", status);
        }

        String whereClause = "";
        if (!conditions.isEmpty()) {
            whereClause = "WHERE " + String.join(" AND ", conditions);
        }

        String orderBy = "ORDER BY " + sort + " " + direction;
        String fullQuery = whereClause + " " + orderBy;

        // Consulta paginada
        PanacheQuery<Evento> panacheQuery = Evento.find(fullQuery, params);
        List<Evento> eventos = panacheQuery.page(page, size).list();
        long totalCount = panacheQuery.count();

        List<EventoRepresentation> representations = repList(eventos);
        PageResponse<EventoRepresentation> response =
                new PageResponse<>(representations, page, size, totalCount);

        return Response.ok(response).build();
    }

    @GET
    @Path("{id}")
    @Operation(summary = "Buscar evento por ID")
    public Response getById(@PathParam("id") long id) {
        Evento entity = Evento.findById(id);
        if (entity == null)
            return Response.status(404).build();
        return Response.ok(rep(entity)).build();
    }

    @POST
    @Operation(summary = "Criar novo evento")
    @Transactional
    public Response insert(@Valid Evento evento) {
        // Se houver artistas enviados, carregar do banco
        if (evento.artistas != null && !evento.artistas.isEmpty()) {
            List<Artista> artistasPersistidos = new ArrayList<>();
            for (Artista artista : evento.artistas) {
                if (artista.id != null) {
                    Artista artistaPersistido = Artista.findById(artista.id);
                    if (artistaPersistido != null) {
                        artistasPersistidos.add(artistaPersistido);
                    }
                }
            }
            evento.artistas = artistasPersistidos;
        } else {
            evento.artistas = new ArrayList<>();
        }

        evento.persist();
        return Response.created(URI.create("/api/v1/eventos/" + evento.id)).entity(rep(evento)).build();
    }

    @PUT
    @Path("{id}")
    @Operation(summary = "Atualizar evento")
    @Transactional
    public Response update(@PathParam("id") long id, @Valid Evento newEvento) {
        Evento entity = Evento.findById(id);
        if (entity == null)
            return Response.status(404).build();

        entity.nome = newEvento.nome;
        entity.descricao = newEvento.descricao;
        entity.dataEvento = newEvento.dataEvento;
        entity.local = newEvento.local;
        entity.capacidadeMaxima = newEvento.capacidadeMaxima;
        entity.precoIngresso = newEvento.precoIngresso;
        entity.status = newEvento.status;

        // LÓGICA DE ATUALIZAÇÃO DE ARTISTAS CORRIGIDA
        if (newEvento.artistas != null) {
            // 1. Limpa a lista de artistas atuais do evento.
            for (Artista artista : new ArrayList<>(entity.artistas)) {
                artista.eventos.remove(entity);
            }
            entity.artistas.clear();

            // 2. Adiciona os novos artistas da requisição
            for (Artista artistaInfo : newEvento.artistas) {
                if (artistaInfo.id != null) {
                    Artista artistaPersistido = Artista.findById(artistaInfo.id);
                    if (artistaPersistido != null) {
                        // Adiciona o artista ao evento e o evento ao artista
                        entity.artistas.add(artistaPersistido);
                        artistaPersistido.eventos.add(entity);
                    }
                }
            }
        }

        return Response.ok(rep(entity)).build();
    }

    @DELETE
    @Path("{id}")
    @Operation(summary = "Excluir evento")
    @Transactional
    public Response delete(@PathParam("id") long id) {
        Evento entity = Evento.findById(id);
        if (entity == null)
            return Response.status(404).build();

        Evento.deleteById(id);
        return Response.noContent().build();
    }

    @PUT
    @Path("{id}/artistas")
    @Operation(summary = "Adicionar artistas ao evento")
    @Transactional
    public Response addArtistas(@PathParam("id") long id, List<Long> artistaIds) {
        Evento evento = Evento.findById(id);
        if (evento == null) {
            return Response.status(404).build();
        }

        for (Long artistaId : artistaIds) {
            Artista artista = Artista.findById(artistaId);
            if (artista != null && !evento.artistas.contains(artista)) {
                evento.artistas.add(artista);
                artista.eventos.add(evento);
            }
        }

        return Response.ok(rep(evento)).build();
    }

    @DELETE
    @Path("{id}/artistas/{artistaId}")
    @Operation(summary = "Remover artista do evento")
    @Transactional
    public Response removeArtista(@PathParam("id") long id, @PathParam("artistaId") long artistaId) {
        Evento evento = Evento.findById(id);
        Artista artista = Artista.findById(artistaId);

        if (evento == null || artista == null) {
            return Response.status(404).build();
        }

        evento.artistas.remove(artista);
        artista.eventos.remove(evento);

        return Response.ok(rep(evento)).build();
    }

    @GET
    @Path("{id}/artistas")
    @Operation(summary = "Listar artistas do evento")
    public Response getArtistasByEvento(@PathParam("id") long id) {
        Evento evento = Evento.findById(id);
        if (evento == null) {
            return Response.status(404).build();
        }

        List<ArtistaRepresentation> representations = evento.artistas.stream()
                .map(artista -> ArtistaRepresentation.from(artista, uriInfo))
                .collect(Collectors.toList());

        return Response.ok(representations).build();
    }

    @GET
    @Path("{id}/ingressos")
    @Operation(summary = "Listar ingressos do evento")
    public Response getIngressosByEvento(@PathParam("id") long id) {
        List<Ingresso> ingressos = Ingresso.find("evento.id", id).list();
        if (ingressos.isEmpty()) {
            return Response.status(204).build();
        }

        List<IngressoRepresentation> representations = ingressos.stream()
                .map(ingresso -> IngressoRepresentation.from(ingresso, uriInfo))
                .collect(Collectors.toList());

        return Response.ok(representations).build();
    }

    @PUT
    @Path("{id}/status")
    @Operation(summary = "Atualizar status do evento")
    @Transactional
    public Response updateStatus(
            @PathParam("id") long id,
            @QueryParam("status") Evento.StatusEvento novoStatus) {

        Evento evento = Evento.findById(id);
        if (evento == null) {
            return Response.status(404).build();
        }

        evento.status = novoStatus;
        return Response.ok(rep(evento)).build();
    }

    @POST
    @Path("{eventoId}/ingressos")
    @Operation(summary = "Comprar ingresso com idempotência", description = "Compra ingressos para um evento com suporte a idempotência")
    @Transactional
    public Response comprarIngressoComIdempotencia(
            @HeaderParam("Idempotency-Key") String idempotencyKey,
            @PathParam("eventoId") Long eventoId,
            @Valid CreateIngressoRequest request) {

        // Verificar chave de idempotência
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\": \"Idempotency-Key header é obrigatório\"}")
                    .build();
        }

        // Verificar se já existe resposta para esta chave
        Response cachedResponse = IdempotencyUtil.getResponseIfExists(idempotencyKey);
        if (cachedResponse != null) {
            return cachedResponse;
        }

        // Processar a compra
        Evento evento = Evento.findById(eventoId);
        if (evento == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"Evento não encontrado\"}")
                    .build();
        }

        // Verificar capacidade
        long ingressosVendidos = Ingresso.count("evento.id", evento.id);
        if (ingressosVendidos + request.getQuantidade() > evento.capacidadeMaxima) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\": \"Capacidade máxima do evento excedida\"}")
                    .build();
        }

        // Criar ingresso
        Ingresso ingresso = new Ingresso();
        ingresso.nomeComprador = request.getNomeComprador();
        ingresso.emailComprador = request.getEmailComprador();
        ingresso.quantidade = request.getQuantidade();
        ingresso.evento = evento;
        ingresso.dataCompra = LocalDateTime.now();
        ingresso.precoTotal = request.getQuantidade() * evento.precoIngresso;
        ingresso.status = Ingresso.StatusIngresso.RESERVADO;

        ingresso.persist();

        // Atualizar status do evento se necessário
        if (Ingresso.count("evento.id", evento.id) >= evento.capacidadeMaxima) {
            evento.status = Evento.StatusEvento.ESGOTADO;
        }

        IngressoRepresentation response = IngressoRepresentation.from(ingresso, uriInfo);
        Response finalResponse = Response.status(Response.Status.CREATED)
                .entity(response)
                .build();

        // Armazenar resposta para idempotência
        IdempotencyUtil.storeResponse(idempotencyKey, finalResponse);

        return finalResponse;
    }

    // Endpoints de busca simples (mantidos para compatibilidade)
    @GET
    @Path("/busca/nome/{nome}")
    @Operation(summary = "Buscar eventos por nome", description = "Busca eventos que contenham o nome especificado")
    public Response buscarPorNome(@PathParam("nome") String nome) {
        List<Evento> eventos = Evento.list("nome LIKE ?1", "%" + nome + "%");
        return Response.ok(repList(eventos)).build();
    }

    @GET
    @Path("/status/{status}")
    @Operation(summary = "Buscar eventos por status", description = "Busca eventos com o status especificado")
    public Response buscarPorStatus(@PathParam("status") Evento.StatusEvento status) {
        List<Evento> eventos = Evento.list("status", status);
        return Response.ok(repList(eventos)).build();
    }

    @GET
    @Path("/local/{local}")
    @Operation(summary = "Buscar eventos por local", description = "Busca eventos no local especificado")
    public Response buscarPorLocal(@PathParam("local") String local) {
        List<Evento> eventos = Evento.list("local LIKE ?1", "%" + local + "%");
        return Response.ok(repList(eventos)).build();
    }
}