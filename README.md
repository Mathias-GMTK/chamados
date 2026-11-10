# API de Sistema de Chamados

API REST em Java 21 + Spring Boot para gerenciar chamados de suporte técnico
(clientes, categorias, técnicos, chamados e comentários). Banco H2 em memória.

## Como rodar

```bash
./mvnw spring-boot:run
```

No Windows: `mvnw.cmd spring-boot:run`

## Front-end

Com a aplicação rodando, abra **http://localhost:8080**. O front-end é servido pelo próprio
Spring Boot (arquivos em `src/main/resources/static`) e consome a API REST com `fetch`,
seguindo os contratos documentados no Swagger.

Telas disponíveis:

- **Chamados**: lista paginada com total por status (clique para filtrar), filtros por
  prioridade e cliente, abertura de chamado e tela de detalhe com mudança de status,
  atribuição de técnico, comentários, edição e exclusão
- **Clientes**, **Categorias** e **Técnicos**: cadastro, edição e exclusão

Ordem para começar a usar: cadastre um cliente, uma categoria e um técnico, depois abra um chamado.

Estrutura do front-end:

| Arquivo | Responsabilidade |
|---|---|
| `static/index.html` | Estrutura da página e menu |
| `static/css/app.css` | Estilos |
| `static/js/api.js` | Todas as chamadas à API REST |
| `static/js/ui.js` | Utilitários de interface (modal, avisos, rótulos) |
| `static/js/cadastros.js` | Telas de clientes, categorias e técnicos |
| `static/js/chamados.js` | Lista, detalhe e atendimento de chamados |
| `static/js/app.js` | Navegação entre as telas |

## Documentação da API (Swagger)

| Recurso | URL |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html (também no link do menu lateral) |
| Especificação OpenAPI (JSON) | http://localhost:8080/v3/api-docs |
| Console do H2 | http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:chamadosdb`, usuário `sa`) |

## Regras de status

| Status atual | Pode ir para |
|---|---|
| ABERTO | EM_ATENDIMENTO, FECHADO |
| EM_ATENDIMENTO | AGUARDANDO_CLIENTE, FECHADO |
| AGUARDANDO_CLIENTE | EM_ATENDIMENTO, FECHADO |
| FECHADO | — |
