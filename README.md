API de Gerenciamento de Eventos de Festivais
API RESTful para gerenciamento de eventos, artistas e ingressos de festivais, desenvolvida com Quarkus.

🚀 Tecnologias Utilizadas
Quarkus 3.25.2

Java 21

Hibernate ORM with Panache

H2 Database (em memória)

OpenAPI 3.0 / Swagger UI

Bean Validation

JWT (para autenticação)

JBCrypt (para hash de senhas)

🏗️ Estrutura do Projeto
text
src/main/java/org/acme/
├── entities/
│   ├── Artista.java          # Entidade Artista
│   ├── Evento.java           # Entidade Evento  
│   ├── Ingresso.java         # Entidade Ingresso
│   ├── Usuario.java          # Entidade Usuario
│   └── ApiKey.java           # Entidade ApiKey
├── resources/
│   ├── ArtistaResource.java      # Endpoints Artistas
│   ├── EventoResource.java       # Endpoints Eventos
│   ├── IngressoResource.java     # Endpoints Ingressos
│   ├── UsuarioResource.java      # Endpoints Usuários
│   ├── AuthResource.java         # Autenticação
│   └── ApiKeyResource.java       # Gerenciamento de API Keys
├── utils/
│   ├── IdempotencyUtil.java      # Utilitário de Idempotência
│   ├── ApiKeyAuthFilter.java     # Filtro de Autenticação
│   ├── RateLimitingFilter.java   # Filtro de Rate Limiting
│   └── GlobalExceptionHandler.java # Tratamento de Exceções
└── representations/
├── ArtistaRepresentation.java    # DTO Artista
├── EventoRepresentation.java     # DTO Evento
├── IngressoRepresentation.java   # DTO Ingresso
└── CreateIngressoRequest.java    # Request DTO
🚀 Como Executar
Pré-requisitos
Java 21+

Maven 3.9+

Executar em Desenvolvimento
bash
# Clone o projeto
git clone <url-do-repositorio>
cd Api-Eventos

# Execute em modo desenvolvimento
mvn quarkus:dev
Acessos
API Base: http://localhost:8080

Documentação Swagger: http://localhost:8080/q/swagger-ui

Health Check: http://localhost:8080/q/health

📚 Endpoints da API
🔐 Autenticação & Usuários
Criar Usuário
bash
POST /api/v1/usuarios
Content-Type: application/json

{
"nome": "João Silva",
"email": "joao@email.com",
"senha": "123456",
"tipo": "CLIENTE"
}
Login
bash
POST /api/v1/auth/login
Content-Type: application/json

{
"email": "joao@email.com",
"senha": "123456"
}
Gerar API Key
bash
POST /api/v1/apikeys/generate/{usuarioId}
X-API-Key: (opcional para primeira geração)
🎤 Artistas
Listar Artistas
bash
GET /api/v1/artistas
X-API-Key: sua_api_key
Buscar Artista por ID
bash
GET /api/v1/artistas/{id}
X-API-Key: sua_api_key
Criar Artista
bash
POST /api/v1/artistas
X-API-Key: sua_api_key
Content-Type: application/json

{
"nome": "Coldplay",
"generoMusical": "Rock",
"biografia": "Banda britânica de rock"
}
Atualizar Artista
bash
PUT /api/v1/artistas/{id}
X-API-Key: sua_api_key
Content-Type: application/json

{
"nome": "Coldplay",
"generoMusical": "Rock Alternativo",
"biografia": "Banda britânica de rock formada em 1996"
}
Buscar Artistas por Nome
bash
GET /api/v1/artistas/busca/nome/{nome}
X-API-Key: sua_api_key
Buscar Artistas por Gênero
bash
GET /api/v1/artistas/genero/{genero}
X-API-Key: sua_api_key
Listar Eventos do Artista
bash
GET /api/v1/artistas/{id}/eventos
X-API-Key: sua_api_key
🎪 Eventos
Listar Eventos
bash
GET /api/v1/eventos
X-API-Key: sua_api_key
Buscar Evento por ID
bash
GET /api/v1/eventos/{id}
X-API-Key: sua_api_key
Criar Evento
bash
POST /api/v1/eventos
X-API-Key: sua_api_key
Content-Type: application/json

{
"nome": "Festival de Verão",
"descricao": "Maior festival de verão",
"dataEvento": "2024-12-15",
"local": "Praia de Copacabana",
"capacidadeMaxima": 50000,
"precoIngresso": 250.0,
"artistas": [{"id": 1}, {"id": 2}]
}
Atualizar Evento
bash
PUT /api/v1/eventos/{id}
X-API-Key: sua_api_key
Content-Type: application/json

{
"nome": "Festival de Verão 2024",
"descricao": "Maior festival de verão atualizado",
"dataEvento": "2024-12-20",
"local": "Praia de Copacabana",
"capacidadeMaxima": 60000,
"precoIngresso": 300.0
}
Adicionar Artistas ao Evento
bash
PUT /api/v1/eventos/{id}/artistas
X-API-Key: sua_api_key
Content-Type: application/json

[1, 2, 3]
Remover Artista do Evento
bash
DELETE /api/v1/eventos/{id}/artistas/{artistaId}
X-API-Key: sua_api_key
Listar Artistas do Evento
bash
GET /api/v1/eventos/{id}/artistas
X-API-Key: sua_api_key
Listar Ingressos do Evento
bash
GET /api/v1/eventos/{id}/ingressos
X-API-Key: sua_api_key
Atualizar Status do Evento
bash
PUT /api/v1/eventos/{id}/status?status=ESGOTADO
X-API-Key: sua_api_key
Buscar Eventos por Nome
bash
GET /api/v1/eventos/busca/nome/{nome}
X-API-Key: sua_api_key
Buscar Eventos por Status
bash
GET /api/v1/eventos/status/{status}
X-API-Key: sua_api_key
Buscar Eventos por Local
bash
GET /api/v1/eventos/local/{local}
X-API-Key: sua_api_key
🎫 Ingressos
Listar Ingressos
bash
GET /api/v1/ingressos
X-API-Key: sua_api_key
Buscar Ingresso por ID
bash
GET /api/v1/ingressos/{id}
X-API-Key: sua_api_key
Comprar Ingresso (Sem Idempotência)
bash
POST /api/v1/ingressos
X-API-Key: sua_api_key
Content-Type: application/json

{
"nomeComprador": "João Silva",
"emailComprador": "joao@email.com",
"quantidade": 2,
"eventoId": 1
}
Comprar Ingresso (Com Idempotência)
bash
POST /api/v1/eventos/{eventoId}/ingressos
X-API-Key: sua_api_key
Idempotency-Key: unique-key-123
Content-Type: application/json

{
"nomeComprador": "João Silva",
"emailComprador": "joao@email.com",
"quantidade": 2
}
Atualizar Ingresso
bash
PUT /api/v1/ingressos/{id}
X-API-Key: sua_api_key
Content-Type: application/json

{
"nomeComprador": "João Silva Santos",
"emailComprador": "joao.silva@email.com",
"quantidade": 3,
"status": "PAGO"
}
Atualizar Status do Ingresso
bash
PUT /api/v1/ingressos/{id}/status?status=PAGO
X-API-Key: sua_api_key
Buscar Ingressos por Email
bash
GET /api/v1/ingressos/busca/email/{email}
X-API-Key: sua_api_key
Buscar Ingressos por Status
bash
GET /api/v1/ingressos/status/{status}
X-API-Key: sua_api_key
🔐 Autenticação e Segurança
API Keys
Todas as rotas (exceto as públicas) requerem API Key

Header: X-API-Key: sua_chave_aqui

Rotas públicas: login, criar usuário, gerar primeira API Key

Rate Limiting
Limite padrão: 100 requisições/minuto

Endpoints específicos têm limites menores

Headers de resposta:

X-RateLimit-Limit: Limite total

X-RateLimit-Remaining: Requisições restantes

Idempotência
Compras de ingressos suportam idempotência

Header: Idempotency-Key: chave_unica

Garante que a mesma operação não seja processada duas vezes

🗄️ Modelo de Dados
Artista
java
- id: Long
- nome: String (2-50 caracteres)
- generoMusical: String
- biografia: String (max 500)
- eventos: List<Evento> (Many-to-Many)
  Evento
  java
- id: Long
- nome: String (2-100 caracteres)
- descricao: String (max 500)
- dataEvento: LocalDate (futura)
- local: String
- capacidadeMaxima: Integer (min 1)
- precoIngresso: Double (min 0.0)
- status: Enum (DISPONIVEL, ESGOTADO, CANCELADO, ADIADO)
- artistas: List<Artista> (Many-to-Many)
- ingressos: List<Ingresso> (One-to-Many)
  Ingresso
  java
- id: Long
- nomeComprador: String (2-100 caracteres)
- emailComprador: String (email válido)
- dataCompra: LocalDateTime
- quantidade: Integer (min 1)
- precoTotal: Double
- status: Enum (RESERVADO, PAGO, CANCELADO, UTILIZADO)
- evento: Evento (Many-to-One)
  Usuario
  java
- id: Long
- nome: String (2-100 caracteres)
- email: String (único)
- senha: String (hash bcrypt)
- tipo: Enum (CLIENTE, ADMIN, ORGANIZADOR)
  ApiKey
  java
- id: Long
- chave: String (única)
- usuario: Usuario (Many-to-One)
- dataCriacao: LocalDateTime
- dataExpiracao: LocalDateTime
- status: Enum (ATIVA, INATIVA, EXPIRADA, REVOGADA)
  🧪 Dados de Exemplo
  O sistema vem com dados pré-carregados:

Usuários
Admin: admin@festivais.com / senha: $2a$10$B/gHc3/pYlTZ8fMTlhnY9./LCG/VAw4FoogTZe0bjYhnoyZiAkaXm

Cliente: joao@email.com

Organizador: maria@eventos.com

Artistas
Coldplay, Beyonce, Drake, Lady Gaga

Eventos
Festival de Verão, Rock in Rio, Lollapalooza, Tomorrowland

API Key de Teste
Chave: demo_key_123456

🛠️ Configurações
application.properties
properties
# Banco de Dados
quarkus.datasource.db-kind=h2
quarkus.datasource.jdbc.url=jdbc:h2:mem:festivaisdb

# Documentação
quarkus.swagger-ui.always-include=true
quarkus.swagger-ui.path=/q/swagger-ui

# Segurança
quarkus.http.cors=true
quarkus.http.cors.origins=*
mp.fault.tolerance.enabled=true
📊 Códigos de Status
200 - Sucesso

201 - Criado

400 - Dados inválidos

401 - Não autenticado

403 - Não autorizado

404 - Não encontrado

409 - Conflito (email duplicado)

429 - Rate limit excedido

500 - Erro interno

🐛 Solução de Problemas
Erro de Compilação
bash
mvn clean compile
Porta Ocupada
bash
# Mudar porta
mvn quarkus:dev -Dquarkus.http.port=8081
Problemas com Java 21+
bash
# Adicionar no IntelliJ VM Options:
--add-opens java.base/java.lang=ALL-UNNAMED
📞 Suporte
Em caso de problemas, verifique:

Logs da aplicação no terminal

Documentação Swagger em /q/swagger-ui

Health check em /q/health

Desenvolvido com ❤️ usando Quarkus

