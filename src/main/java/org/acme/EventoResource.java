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
                        // Opcional: retornar erro se um ID de artista não for encontrado
                         // return Response.status(Response.Status.BAD_REQUEST)
                         //               .entity("{\"message\": \"Artista com ID " + artistaInput.id + " não encontrado.\"}")
                         //               .build();
                    }
                }
            }
            evento.artistas = artistasPersistidos;
        } else {
            evento.artistas = new ArrayList<>(); // Garante que a lista não seja nula
        }

        // Garante status default se não vier no JSON
        if (evento.status == null) {
            evento.status = Evento.StatusEvento.DISPONIVEL;
        }

        evento.persist();
        return Response.created(URI.create("/api/v1/eventos/" + evento.id)).entity(rep(evento)).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Atualizar evento")
    @Transactional
    public Response update(@PathParam("id") long id, @Valid Evento newEvento) {
        Evento entity = Evento.findById(id);
        if (entity == null) {
             return Response.status(Response.Status.NOT_FOUND)
                            .entity("{\"message\": \"Evento não encontrado\"}")
                            .build();
        }

        // Atualiza os campos simples
        entity.nome = newEvento.nome;
        entity.descricao = newEvento.descricao;
        entity.dataEvento = newEvento.dataEvento;
        entity.local = newEvento.local;
        entity.capacidadeMaxima = newEvento.capacidadeMaxima;
        entity.precoIngresso = newEvento.precoIngresso;
        if (newEvento.status != null) { // Só atualiza status se for enviado
            entity.status = newEvento.status;
        }

        // Atualiza a lista de artistas (se enviada)
        if (newEvento.artistas != null) {
            // Remove o evento das listas dos artistas antigos
            for (Artista artistaAntigo : new ArrayList<>(entity.artistas)) {
                artistaAntigo.eventos.remove(entity);
            }
            entity.artistas.clear(); // Limpa a lista do evento

            // Adiciona os novos artistas
            for (Artista artistaInfo : newEvento.artistas) {
                if (artistaInfo != null && artistaInfo.id != null) {
                    Artista artistaPersistido = Artista.findById(artistaInfo.id);
                    if (artistaPersistido != null) {
                        entity.artistas.add(artistaPersistido);
                        artistaPersistido.eventos.add(entity); // Mantém a bidirecionalidade
                    }
                }
            }
        }
        // Se newEvento.artistas for null, a lista de artistas atual não é modificada

        entity.persist(); // Garante que as mudanças sejam salvas
        return Response.ok(rep(entity)).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Excluir evento")
    @Transactional
    public Response delete(@PathParam("id") long id) {
        Evento entity = Evento.findById(id);
        if (entity == null) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("{\"message\": \"Evento não encontrado\"}")
                           .build();
        }

        // Remove a associação bidirecional com artistas antes de deletar
        for (Artista artista : new ArrayList<>(entity.artistas)) {
            artista.eventos.remove(entity);
        }
        entity.artistas.clear();

        // O cascade=CascadeType.ALL no Evento.java deve lidar com a exclusão de ingressos
        boolean deleted = Evento.deleteById(id);
        if (!deleted) {
             return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("{\"message\": \"Falha ao deletar evento.\"}")
                            .build();
        }

        return Response.noContent().build();
    }

    // --- Métodos para Gerenciar Artistas no Evento ---

    @PUT
    @Path("/{id}/artistas")
    @Operation(summary = "Adicionar/Definir artistas do evento", description = "Substitui a lista de artistas do evento pelos IDs fornecidos")
    @Transactional
    public Response setArtistas(@PathParam("id") long id, List<Long> artistaIds) {
        Evento evento = Evento.findById(id);
        if (evento == null) {
             return Response.status(Response.Status.NOT_FOUND)
                            .entity("{\"message\": \"Evento não encontrado\"}")
                            .build();
        }

        // Remove associações antigas
        for (Artista artistaAntigo : new ArrayList<>(evento.artistas)) {
            artistaAntigo.eventos.remove(evento);
        }
        evento.artistas.clear();

        // Adiciona novas associações
        if (artistaIds != null) {
            for (Long artistaId : artistaIds) {
                Artista artista = Artista.findById(artistaId);
                if (artista != null) {
                    evento.artistas.add(artista);
                    artista.eventos.add(evento);
                } else {
                     // Opcional: Retornar erro se um ID não for encontrado
                    // return Response.status(Response.Status.BAD_REQUEST)
                    //                .entity("{\"message\": \"Artista com ID " + artistaId + " não encontrado.\"}")
                    //                .build();
                }
            }
        }
        
        evento.persist();
        return Response.ok(rep(evento)).build();
    }

    @DELETE
    @Path("/{id}/artistas/{artistaId}")
    @Operation(summary = "Remover artista do evento")
    @Transactional
    public Response removeArtista(@PathParam("id") long id, @PathParam("artistaId") long artistaId) {
        Evento evento = Evento.findById(id);
        Artista artista = Artista.findById(artistaId);

        if (evento == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("{\"message\": \"Evento não encontrado\"}").build();
        }
         if (artista == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("{\"message\": \"Artista não encontrado\"}").build();
        }

        if (evento.artistas.contains(artista)) {
            evento.artistas.remove(artista);
            artista.eventos.remove(evento);
            evento.persist();
            return Response.ok(rep(evento)).build();
        } else {
            // Retorna OK mesmo se o artista já não estava no evento (idempotente)
            return Response.ok(rep(evento)).build();
        }
    }

    @GET
    @Path("/{id}/artistas")
    @Operation(summary = "Listar artistas do evento")
    public Response getArtistasByEvento(@PathParam("id") long id) {
        Evento evento = Evento.findById(id);
        if (evento == null) {
             return Response.status(Response.Status.NOT_FOUND)
                            .entity("{\"message\": \"Evento não encontrado\"}")
                            .build();
        }

        // Força o carregamento LAZY dos artistas, se necessário (geralmente não é preciso com Panache)
        // Hibernate.initialize(evento.artistas); 

        List<ArtistaRepresentation> representations = evento.artistas.stream()
                .map(artista -> ArtistaRepresentation.from(artista, uriInfo))
                .collect(Collectors.toList());

        return Response.ok(representations).build();
    }

    // --- Métodos para Gerenciar Ingressos no Evento ---

    @GET
    @Path("/{id}/ingressos")
    @Operation(summary = "Listar ingressos do evento")
    public Response getIngressosByEvento(@PathParam("id") long id) {
        // Verifica se o evento existe primeiro
        Evento evento = Evento.findById(id);
         if (evento == null) {
             return Response.status(Response.Status.NOT_FOUND)
                            .entity("{\"message\": \"Evento não encontrado\"}")
                            .build();
         }
        
        List<Ingresso> ingressos = Ingresso.find("evento.id", id).list();
        // Não retorna 404 se a lista estiver vazia, retorna 200 OK com lista vazia ou 204 No Content
        if (ingressos.isEmpty()) {
            return Response.noContent().build(); // 204 é mais apropriado para lista vazia
        }

        List<IngressoRepresentation> representations = ingressos.stream()
                .map(ingresso -> IngressoRepresentation.from(ingresso, uriInfo))
                .collect(Collectors.toList());

        return Response.ok(representations).build();
    }

    @PUT
    @Path("/{id}/status")
    @Operation(summary = "Atualizar status do evento")
    @Transactional
    public Response updateStatus(
            @PathParam("id") long id,
            @QueryParam("status") Evento.StatusEvento novoStatus) {

        Evento evento = Evento.findById(id);
        if (evento == null) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("{\"message\": \"Evento não encontrado\"}")
                           .build();
        }
        
        // Valida se o status enviado é válido
        if (novoStatus == null) {
             return Response.status(Response.Status.BAD_REQUEST)
                            .entity("{\"message\": \"Parâmetro 'status' é obrigatório e deve ser um valor válido (DISPONIVEL, ESGOTADO, CANCELADO, ADIADO).\"}")
                            .build();
        }

        evento.status = novoStatus;
        evento.persist();
        return Response.ok(rep(evento)).build();
    }

    @POST
    @Path("/{eventoId}/ingressos")
    @Operation(summary = "Comprar ingresso com idempotência", description = "Compra ingressos para um evento com suporte a idempotência")
    @Transactional
    public Response comprarIngressoComIdempotencia(
            // CORREÇÃO APLICADA AQUI
            @HeaderParam("x-idempotency-key") String idempotencyKey,
            @PathParam("eventoId") Long eventoId,
            @Valid CreateIngressoRequest request) {

        // Validar se o eventoId no Path é o mesmo do corpo (se existir no corpo)
        // No seu CreateIngressoRequest, o eventoId VEM no corpo
        if (!eventoId.equals(request.getEventoId())) {
             return Response.status(Response.Status.BAD_REQUEST)
                            .entity("{\"message\": \"O ID do evento na URL (" + eventoId + ") não corresponde ao ID no corpo da requisição (" + request.getEventoId() + ").\"}")
                            .build();
        }


        // Verificar chave de idempotência
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\": \"Header 'x-idempotency-key' é obrigatório\"}")
                    .build();
        }

        // Verificar se já existe resposta para esta chave
        Response cachedResponse = IdempotencyUtil.getResponseIfExists(idempotencyKey);
        if (cachedResponse != null) {
            // Retorna a resposta armazenada (geralmente 201 Created ou um erro anterior)
            return cachedResponse;
        }

        // Processar a compra
        Evento evento = Evento.findById(eventoId);
        if (evento == null) {
            // Armazena o erro 404 para futuras requisições com a mesma chave
            Response errorResponse = Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"Evento não encontrado\"}")
                    .build();
            IdempotencyUtil.storeResponse(idempotencyKey, errorResponse);
            return errorResponse;
        }
        
        // Verifica se o evento está em um status que permite compra
        if (evento.status == Evento.StatusEvento.ESGOTADO || evento.status == Evento.StatusEvento.CANCELADO) {
             Response errorResponse = Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\": \"Não é possível comprar ingressos para um evento " + evento.status.toString() + ".\"}")
                    .build();
             IdempotencyUtil.storeResponse(idempotencyKey, errorResponse);
             return errorResponse;
        }

        // Verificar capacidade
        long ingressosVendidos = Ingresso.count("evento.id = ?1 and status != ?2", evento.id, Ingresso.StatusIngresso.CANCELADO); // Não conta cancelados
        if (ingressosVendidos + request.getQuantidade() > evento.capacidadeMaxima) {
             Response errorResponse = Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\": \"Capacidade máxima do evento excedida. Disponíveis: " + (evento.capacidadeMaxima - ingressosVendidos) + "\"}")
                    .build();
             IdempotencyUtil.storeResponse(idempotencyKey, errorResponse);
             return errorResponse;
        }

        // Criar ingresso
        Ingresso ingresso = new Ingresso();
        ingresso.nomeComprador = request.getNomeComprador();
        ingresso.emailComprador = request.getEmailComprador();
        ingresso.quantidade = request.getQuantidade();
        ingresso.evento = evento; // Associação com o evento
        ingresso.dataCompra = LocalDateTime.now();
        ingresso.precoTotal = request.getQuantidade() * evento.precoIngresso;
        ingresso.status = Ingresso.StatusIngresso.RESERVADO; // Status inicial

        ingresso.persist();

        // Atualizar status do evento se atingiu a capacidade
        long ingressosAtuais = Ingresso.count("evento.id = ?1 and status != ?2", evento.id, Ingresso.StatusIngresso.CANCELADO);
        if (ingressosAtuais >= evento.capacidadeMaxima) {
            evento.status = Evento.StatusEvento.ESGOTADO;
            evento.persist();
        }

        // Cria a resposta de sucesso
        IngressoRepresentation responseRepresentation = IngressoRepresentation.from(ingresso, uriInfo);
        Response finalResponse = Response.status(Response.Status.CREATED)
                .entity(responseRepresentation)
                .build();

        // Armazena a resposta de sucesso para idempotência
        IdempotencyUtil.storeResponse(idempotencyKey, finalResponse);

        return finalResponse;
    }

    // --- Endpoints de busca simples (mantidos) ---
    @GET
    @Path("/busca/nome/{nome}")
    @Operation(summary = "Buscar eventos por nome", description = "Busca eventos que contenham o nome especificado (case-insensitive)")
    public Response buscarPorNome(@PathParam("nome") String nome) {
        List<Evento> eventos = Evento.list("lower(nome) LIKE lower(?1)", "%" + nome + "%");
        if (eventos.isEmpty()) return Response.noContent().build();
        return Response.ok(repList(eventos)).build();
    }

    @GET
    @Path("/status/{status}")
    @Operation(summary = "Buscar eventos por status", description = "Busca eventos com o status especificado")
    public Response buscarPorStatus(@PathParam("status") Evento.StatusEvento status) {
         if (status == null) {
              return Response.status(Response.Status.BAD_REQUEST)
                             .entity("{\"message\": \"Status inválido.\"}")
                             .build();
         }
        List<Evento> eventos = Evento.list("status", status);
         if (eventos.isEmpty()) return Response.noContent().build();
        return Response.ok(repList(eventos)).build();
    }

    @GET
    @Path("/local/{local}")
    @Operation(summary = "Buscar eventos por local", description = "Busca eventos no local especificado (case-insensitive)")
    public Response buscarPorLocal(@PathParam("local") String local) {
        List<Evento> eventos = Evento.list("lower(local) LIKE lower(?1)", "%" + local + "%");
        if (eventos.isEmpty()) return Response.noContent().build();
        return Response.ok(repList(eventos)).build();
    }
}
