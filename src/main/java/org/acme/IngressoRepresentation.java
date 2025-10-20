package org.acme;

import jakarta.ws.rs.core.UriInfo;

public class IngressoRepresentation {
    public Long id;
    public String nomeComprador;
    public String emailComprador;
    public String dataCompra;
    public Integer quantidade;
    public Double precoTotal;
    public String status;
    public Long eventoId;

    public IngressoRepresentation() {
    }

    public static IngressoRepresentation from(Ingresso ingresso, UriInfo uriInfo) {
        IngressoRepresentation rep = new IngressoRepresentation();
        rep.id = ingresso.id;
        rep.nomeComprador = ingresso.nomeComprador;
        rep.emailComprador = ingresso.emailComprador;
        rep.dataCompra = ingresso.dataCompra.toString();
        rep.quantidade = ingresso.quantidade;
        rep.precoTotal = ingresso.precoTotal;
        rep.status = ingresso.status.toString();
        rep.eventoId = ingresso.evento.id;

        return rep;
    }
}