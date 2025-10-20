# CRUD com Métodos HTTP

Este documento descreve como implementar operações CRUD para a entidade `Book` usando Quarkus REST + Panache.

## Métodos HTTP e Semântica

| Operação | HTTP | Caminho | Status de Sucesso |
|----------|------|--------|-------------------|
| Listar todos | GET | /books | 200 |
| Obter por id | GET | /books/{id} | 200 / 404 |
| Criar | POST | /books | 201 |
| Atualizar | PUT | /books/{id} | 200 / 404 |
| Excluir | DELETE | /books/{id} | 204 / 404 |

## Recurso Atual (com HATEOAS simplificado)

Trechos essenciais:

```java
@Path("/books")
public class BookResource {
    @GET
    public Response getAll(){
        return Response.ok(repList(Book.listAll())).build();
    }

    @GET @Path("{id}")
    public Response getById(@PathParam("id") long id){
        Book b = Book.findById(id);
        if(b == null) return Response.status(404).build();
        return Response.ok(rep(b)).build();
    }

    @POST @Transactional
    public Response insert(Book book){
        Book.persist(book);
        return Response.status(201).entity(rep(book)).build();
    }

    @PUT @Path("{id}") @Transactional
    public Response update(@PathParam("id") long id, Book novo){
        Book entidade = Book.findById(id);
        if(entidade == null) return Response.status(404).build();
        entidade.titulo = novo.titulo; // etc.
        return Response.ok(rep(entidade)).build();
    }

    @DELETE @Path("{id}") @Transactional
    public Response delete(@PathParam("id") long id){
        Book entidade = Book.findById(id);
        if(entidade == null) return Response.status(404).build();
        Book.deleteById(id);
        return Response.noContent().build();
    }
}
```

## Persistência com Panache

- `Book.persist(entidade)` salva a entidade.
- `Book.findById(id)` retorna ou `null`.
- `Book.listAll()` lista tudo.
- `Book.deleteById(id)` remove por id (retorna boolean internamente se precisar checar).

## Boas Práticas de Status HTTP

- 201 Created no POST com corpo retornando o recurso (e idealmente header `Location`).
- 200 OK em GET/PUT com representação atual.
- 204 No Content em DELETE sem corpo.
- 404 Not Found quando id não existe.

## HATEOAS (Simples)

A classe `BookRepresentation` adiciona `_links` para guiar o cliente:

```json
{
  "id": 1,
  "titulo": "Dom Casmurro",
  "_links": {
    "self": "http://localhost:8080/books/1",
    "all": "http://localhost:8080/books",
    "delete": "http://localhost:8080/books/1",
    "update": "http://localhost:8080/books/1",
    "search": "http://localhost:8080/books/search"
  }
}
```

Isso facilita navegação RESTful sem o cliente construir URLs manualmente.

## Testes (Sugestão)

Use RestAssured:
```
GET /books -> 200 e JSON array
POST /books -> 201 e objeto com _links
DELETE /books/{id} -> 204
```

## Próximo Passo

Adicionar busca dinâmica, filtros e ordenação (ver próximo documento).
