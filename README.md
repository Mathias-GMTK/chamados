# API de Sistema de Chamados

API REST em Java 21 + Spring Boot para gerenciar chamados de suporte técnico
(clientes, categorias, técnicos, chamados e comentários). Banco H2 em memória.

## Como rodar

```bash
./mvnw spring-boot:run
```

No Windows: `mvnw.cmd spring-boot:run`

## Interface (Swagger UI)

Com a aplicação rodando, abra **http://localhost:8080** — a raiz redireciona para o Swagger UI
(`/swagger-ui.html`). Por ali dá para ver e testar todos os endpoints: clique no endpoint,
em **Try it out**, ajuste o JSON de exemplo e clique em **Execute**.

Ordem sugerida para testar:

1. `POST /clientes` — cadastrar um cliente
2. `POST /categorias` — cadastrar uma categoria
3. `POST /tecnicos` — cadastrar um técnico
4. `POST /chamados` — abrir um chamado com os IDs criados
5. `PATCH /chamados/{id}/atribuir`, `PATCH /chamados/{id}/status` e `POST /chamados/{id}/comentarios`

Outros endereços úteis:

| Recurso | URL |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Especificação OpenAPI (JSON) | http://localhost:8080/v3/api-docs |
| Console do H2 | http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:chamadosdb`, usuário `sa`) |

## Regras de status

| Status atual | Pode ir para |
|---|---|
| ABERTO | EM_ATENDIMENTO, FECHADO |
| EM_ATENDIMENTO | AGUARDANDO_CLIENTE, FECHADO |
| AGUARDANDO_CLIENTE | EM_ATENDIMENTO, FECHADO |
| FECHADO | — |
