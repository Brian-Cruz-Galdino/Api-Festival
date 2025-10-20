# Busca, Filtro, Paginação e Ordenação

Este módulo mostra como expor endpoints com query dinâmica para a entidade `Book` utilizando Panache.

## Endpoint de Busca

`GET /books/search`

Parâmetros suportados:
- `q`: termo textual (titulo, autor, editora)
- `sort`: campo para ordenar (id, titulo, autor, editora, anoLancamento, estaDisponivel)
- `direction`: asc | desc
- `page`: página (1-based na API, convertida internamente para 0-based)
- `size`: tamanho da página

Exemplo:
```
GET /books/search?q=machado&sort=anoLancamento&direction=desc&page=1&size=5
```

## Implementação (trecho)
```java
    Set<String> allowed = Set.of("id","titulo","autor","editora","anoLancamento","estaDisponivel");
    if(!allowed.contains(sort)) sort = "id";
            Sort sortObj = Sort.by(sort, "desc".equalsIgnoreCase(direction) ? Sort.Direction.Descending : Sort.Direction.Ascending);
    int effectivePage = page <= 1 ? 0 : page - 1;
    var query = (q == null || q.isBlank())
        ? Book.findAll(sortObj)
        : Book.find("lower(titulo) like ?1 or lower(autor) like ?1 or lower(editora) like ?1", sortObj, "%"+q.toLowerCase()+"%");
    var books = query.page(effectivePage, size).list();
```

## Paginação
- `query.page(pageIndex, size)` retorna uma sublista.
- Pode-se expor metadados (total, página atual) com `query.count()` e `query.page().index` para enriquecer a resposta (melhoria futura).

## Sanitização do Sort
Evita injeção ou erro de campo inválido validando a lista `allowed`.

## Estratégia de Busca
Uso de `lower(campo) like ?1` para busca case-insensitive simples. Para grandes volumes e desempenho: considerar índices ou full-text search (PostgreSQL, Elastic, etc.).

## Integração com HATEOAS
Resposta atual devolve lista de `BookRepresentation` cada uma com links:
```json
{
  "id": 2,
  "titulo": "O Alquimista",
  "_links": {
    "self": "http://localhost:8080/books/2",
    "all": "http://localhost:8080/books",
    "delete": "http://localhost:8080/books/2",
    "update": "http://localhost:8080/books/2",
    "search": "http://localhost:8080/books/search"
  }
}
```

Melhoria futura: adicionar links de paginação (`first`, `prev`, `next`, `last`) construindo URLs com os mesmos parâmetros.

## Testes (Sugestão)
- Sem parâmetros: `GET /books/search` -> lista padrão ordenada por id asc.
- Com `q` que não encontra: resposta lista vazia.
- Ordenação desc: verificar primeiro elemento é o maior valor do campo.
- Páginas consecutivas não devem repetir itens.

## Próximos Passos
- Adicionar metadados de paginação (total, page, size, totalPages).
- Implementar links de paginação HATEOAS.
- Cache / ETag para respostas estáticas.
# Introdução ao Quarkus

Quarkus é um framework Java nativo para a nuvem e otimizado para GraalVM e HotSpot. Objetivos principais:

- Tempo de inicialização muito rápido
- Baixo consumo de memória
- Produtividade (live reload / dev mode)
- Integração simples com especificações Jakarta EE / MicroProfile

## Conceitos Centrais

- Extensões: adicionam capacidades (RESTEasy / REST, Hibernate ORM, Panache, Jackson etc.)
- Dev Mode: `./mvnw quarkus:dev` recompila e recarrega automaticamente
- Configuração: `application.properties`
- Build nativa: `./mvnw -Pnative package`

## Extensões Utilizadas neste projeto

| Extensão | Função |
|----------|--------|
| quarkus-rest | Implementação REST (Jakarta REST) |
| quarkus-rest-jackson | Serialização JSON |
| quarkus-hibernate-orm-panache | ORM + API Panache (simplifica CRUD) |
| quarkus-hibernate-orm | Base do Hibernate ORM |
| quarkus-jdbc-h2 | Banco em memória para dev/test |

## Entidade Exemplo

```java
@Entity
public class Book extends PanacheEntity {
    public String titulo;
    public String autor;
    public String editora;
    public int anoLancamento;
    public boolean estaDisponivel;
}
```

PanacheEntity já fornece o campo `id` e métodos utilitários (`listAll()`, `find`, `persist`, `deleteById`, etc.).

## Recurso REST Básico (antes do HATEOAS)

```java
@Path("/books")
public class BookResource {
    @GET
    public Response getAll(){ return Response.ok(Book.listAll()).build(); }
}
```

## Dev Mode

Execute:
```
./mvnw quarkus:dev
```
A aplicação sobe em modo interativo com live reload e console de Dev UI.

---
Esta base permite evoluir para CRUD completo, busca avançada e HATEOAS mostrados nos próximos arquivos.
