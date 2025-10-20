package org.acme;

import jakarta.ws.rs.core.UriInfo;
import java.util.stream.Collectors;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Representação de um evento para API REST")
public class EventoRepresentation {

    @Schema(description = "ID único do evento")
    public Long id;

    @Schema(description = "Nome do evento")
    public String nome;

    @Schema(description = "Descrição do evento")
    public String descricao;

    @Schema(description = "Data do evento")
    public String dataEvento;

    @Schema(description = "Local do evento")
    public String local;

    @Schema(description = "Capacidade máxima do evento")
    public Integer capacidadeMaxima;

    @Schema(description = "Preço do ingresso")
    public Double precoIngresso;

    @Schema(description = "Status do evento")
    public String status;

    @Schema(description = "IDs dos artistas participantes")
    public java.util.List<Long> artistaIds;

    public EventoRepresentation() {
    }

    public static EventoRepresentation from(Evento evento, UriInfo uriInfo) {
        EventoRepresentation rep = new EventoRepresentation();
        rep.id = evento.id;
        rep.nome = evento.nome;
        rep.descricao = evento.descricao;
        rep.dataEvento = evento.dataEvento.toString();
        rep.local = evento.local;
        rep.capacidadeMaxima = evento.capacidadeMaxima;
        rep.precoIngresso = evento.precoIngresso;
        rep.status = evento.status.toString();
        rep.artistaIds = evento.artistas.stream().map(artista -> artista.id).collect(Collectors.toList());

        return rep;
    }
}