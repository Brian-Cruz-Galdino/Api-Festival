# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Quarkus-based Java project implementing a RESTful API for book management. The project demonstrates:
- CRUD operations with HTTP semantics
- Search functionality with pagination, filtering, and sorting
- HATEOAS-style representations
- Hibernate ORM with Panache for persistence

## Development Commands

### Running the Application
```bash
./mvnw quarkus:dev
```
Runs in development mode with live reload. Dev UI available at http://localhost:8080/q/dev/

### Testing
```bash
./mvnw test
```
Runs unit tests using JUnit 5 and RestAssured for integration testing.

### Building
```bash
./mvnw package
```
Creates `quarkus-run.jar` in `target/quarkus-app/` directory.

For uber-jar:
```bash
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

### Native Build
```bash
./mvnw package -Dnative
```
Or with container build:
```bash
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

## Architecture

### Core Components
- **Book Entity** (`src/main/java/org/acme/Book.java`): JPA entity extending PanacheEntity for simplified persistence
- **BookResource** (`src/main/java/org/acme/BookResource.java`): REST endpoints for book operations
- **BookRepresentation** (`src/main/java/org/acme/BookRepresentation.java`): HATEOAS representation layer

### Database
- Uses H2 in-memory database for development/testing
- Configured via `application.properties`
- Hibernate ORM with Panache provides Active Record pattern

### REST API Design
- Base path: `/books`
- Implements full CRUD with proper HTTP semantics
- Search endpoint: `/books/search` with query parameters:
  - `q`: text search across titulo, autor, editora
  - `sort`: field to sort by (id, titulo, autor, editora, anoLancamento, estaDisponivel)
  - `direction`: asc/desc
  - `page`: 1-based page number
  - `size`: page size

### Key Patterns
- Uses `@Transactional` for write operations
- Proper HTTP status codes (200, 201, 204, 404)
- HATEOAS links in responses via BookRepresentation
- Input validation for sort parameters to prevent injection
- Case-insensitive search with LIKE queries

## Technology Stack
- Quarkus 3.25.2
- Java 21/23
- Jakarta EE (REST, Persistence)
- Hibernate ORM with Panache
- H2 Database
- Jackson for JSON serialization
- JUnit 5 + RestAssured for testing