package br.com.univille.chamados.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuração geral da documentação OpenAPI exibida no Swagger UI.
 * Acesse em http://localhost:8080/swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI chamadosOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Sistema de Chamados")
                        .version("v1")
                        .description("""
                                API REST para gerenciamento de chamados de suporte técnico.

                                **Fluxo sugerido para testar:**
                                1. Cadastre um cliente em **Clientes**
                                2. Cadastre uma categoria em **Categorias**
                                3. Cadastre um técnico em **Técnicos**
                                4. Abra um chamado em **Chamados** usando os IDs criados
                                5. Atribua o técnico, altere o status e adicione comentários

                                **Transições de status permitidas:**
                                - ABERTO → EM_ATENDIMENTO ou FECHADO
                                - EM_ATENDIMENTO → AGUARDANDO_CLIENTE ou FECHADO
                                - AGUARDANDO_CLIENTE → EM_ATENDIMENTO ou FECHADO
                                - FECHADO → (nenhuma)
                                """)
                        .contact(new Contact()
                                .name("Mathias")
                                .url("https://github.com/Mathias-GMTK/chamados")))
                .servers(List.of(new Server()
                        .url("http://localhost:8080")
                        .description("Ambiente local")))
                .tags(List.of(
                        new Tag().name("Clientes").description("Quem abre os chamados"),
                        new Tag().name("Categorias").description("Classificação dos chamados"),
                        new Tag().name("Técnicos").description("Quem atende os chamados"),
                        new Tag().name("Chamados").description("Abertura, atendimento e acompanhamento")));
    }
}
