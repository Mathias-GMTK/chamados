package br.com.univille.chamados.Controller;

import br.com.univille.chamados.controller.ChamadoController;
import br.com.univille.chamados.dto.*;
import br.com.univille.chamados.entity.Prioridade;
import br.com.univille.chamados.entity.StatusChamado;
import br.com.univille.chamados.exception.RecursoNaoEncontradoException;
import br.com.univille.chamados.exception.RegraNegocioException;
import br.com.univille.chamados.service.ChamadoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChamadoController.class)
class ChamadoControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean ChamadoService service;

    private ChamadoResponse resposta(StatusChamado status) {
        return new ChamadoResponse(1L, "Sem internet", "Rede caiu", LocalDateTime.now(),
                null, Prioridade.ALTA, status, 1L, 1L, null);
    }

    private static final String CHAMADO_VALIDO = """
            {"titulo":"Sem internet","descricao":"Rede caiu","prioridade":"ALTA",
             "clienteId":1,"categoriaId":1}
            """;

    @Test
    void criaChamadoComSucesso() throws Exception {
        when(service.criar(any(ChamadoRequest.class))).thenReturn(resposta(StatusChamado.ABERTO));

        mockMvc.perform(post("/chamados").contentType(MediaType.APPLICATION_JSON).content(CHAMADO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ABERTO"));
    }

    @Test
    void rejeitaChamadoSemTitulo() throws Exception {
        mockMvc.perform(post("/chamados").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"","prioridade":"ALTA","clienteId":1,"categoriaId":1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Dados inválidos"));
    }

    @Test
    void retorna404QuandoClienteNaoExiste() throws Exception {
        when(service.criar(any(ChamadoRequest.class)))
                .thenThrow(new RecursoNaoEncontradoException("Cliente não encontrado: 1"));

        mockMvc.perform(post("/chamados").contentType(MediaType.APPLICATION_JSON).content(CHAMADO_VALIDO))
                .andExpect(status().isNotFound());
    }

    @Test
    void buscaDetalheComComentarios() throws Exception {
        ChamadoDetalheResponse detalhe = new ChamadoDetalheResponse(1L, "Sem internet", "Rede caiu",
                LocalDateTime.now(), null, Prioridade.ALTA, StatusChamado.ABERTO, 1L, 1L, null,
                List.of(new ComentarioResponse(1L, "Reiniciei o roteador", "Carlos", LocalDateTime.now())));
        when(service.buscarDetalhe(1L)).thenReturn(detalhe);

        mockMvc.perform(get("/chamados/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comentarios[0].autor").value("Carlos"));
    }

    @Test
    void buscaChamadoInexistenteRetorna404() throws Exception {
        when(service.buscarDetalhe(99L))
                .thenThrow(new RecursoNaoEncontradoException("Chamado não encontrado: 99"));

        mockMvc.perform(get("/chamados/99")).andExpect(status().isNotFound());
    }

    @Test
    void listaComFiltros() throws Exception {
        when(service.listar(eq(StatusChamado.ABERTO), eq(Prioridade.ALTA), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(resposta(StatusChamado.ABERTO))));

        mockMvc.perform(get("/chamados").param("status", "ABERTO").param("prioridade", "ALTA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].titulo").value("Sem internet"));
    }

    @Test
    void filtroComStatusInvalidoRetorna400() throws Exception {
        mockMvc.perform(get("/chamados").param("status", "XYZ"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void alteraStatusComSucesso() throws Exception {
        when(service.alterarStatus(1L, StatusChamado.EM_ATENDIMENTO))
                .thenReturn(resposta(StatusChamado.EM_ATENDIMENTO));

        mockMvc.perform(patch("/chamados/1/status").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"EM_ATENDIMENTO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_ATENDIMENTO"));
    }

    @Test
    void transicaoInvalidaRetorna409() throws Exception {
        when(service.alterarStatus(1L, StatusChamado.AGUARDANDO_CLIENTE))
                .thenThrow(new RegraNegocioException("Mudança de status inválida"));

        mockMvc.perform(patch("/chamados/1/status").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"AGUARDANDO_CLIENTE\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void statusInexistenteNoJsonRetorna400() throws Exception {
        mockMvc.perform(patch("/chamados/1/status").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"XYZ\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void atribuirEmChamadoFechadoRetorna409() throws Exception {
        when(service.atribuir(1L, 2L))
                .thenThrow(new RegraNegocioException("Não é possível atribuir técnico a um chamado fechado"));

        mockMvc.perform(patch("/chamados/1/atribuir").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tecnicoId\":2}"))
                .andExpect(status().isConflict());
    }

    @Test
    void criaComentario() throws Exception {
        when(service.adicionarComentario(eq(1L), any(ComentarioRequest.class)))
                .thenReturn(new ComentarioResponse(1L, "Reiniciei o roteador", "Carlos", LocalDateTime.now()));

        mockMvc.perform(post("/chamados/1/comentarios").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"texto\":\"Reiniciei o roteador\",\"autor\":\"Carlos\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.autor").value("Carlos"));
    }

    @Test
    void rejeitaComentarioSemTexto() throws Exception {
        mockMvc.perform(post("/chamados/1/comentarios").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"texto\":\"\",\"autor\":\"Carlos\"}"))
                .andExpect(status().isBadRequest());
    }
}