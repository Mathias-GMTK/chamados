package br.com.univille.chamados.Controller;

import br.com.univille.chamados.controller.ClienteController;
import br.com.univille.chamados.dto.ChamadoResponse;
import br.com.univille.chamados.dto.ClienteRequest;
import br.com.univille.chamados.dto.ClienteResponse;
import br.com.univille.chamados.entity.Prioridade;
import br.com.univille.chamados.entity.StatusChamado;
import br.com.univille.chamados.exception.RecursoNaoEncontradoException;
import br.com.univille.chamados.exception.RegraNegocioException;
import br.com.univille.chamados.service.ChamadoService;
import br.com.univille.chamados.service.ClienteService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClienteController.class)
class ClienteControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean ClienteService service;
    @MockitoBean ChamadoService chamadoService;

    private static final String CLIENTE_VALIDO = """
            {"nome":"Ana","email":"ana@teste.com","telefone":"47999990000"}
            """;

    @Test
    void criaCliente() throws Exception {
        when(service.criar(any(ClienteRequest.class)))
                .thenReturn(new ClienteResponse(1L, "Ana", "ana@teste.com", "47999990000"));

        mockMvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON).content(CLIENTE_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void rejeitaEmailInvalido() throws Exception {
        mockMvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Ana\",\"email\":\"isso-nao-e-email\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void emailDuplicadoRetorna409() throws Exception {
        when(service.criar(any(ClienteRequest.class)))
                .thenThrow(new RegraNegocioException("Já existe cliente com o e-mail ana@teste.com"));

        mockMvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON).content(CLIENTE_VALIDO))
                .andExpect(status().isConflict());
    }

    @Test
    void clienteInexistenteRetorna404() throws Exception {
        when(service.buscar(99L)).thenThrow(new RecursoNaoEncontradoException("Cliente 99 não encontrado"));

        mockMvc.perform(get("/clientes/99")).andExpect(status().isNotFound());
    }

    @Test
    void listaChamadosDoCliente() throws Exception {
        ChamadoResponse chamado = new ChamadoResponse(1L, "Sem internet", null, LocalDateTime.now(),
                null, Prioridade.ALTA, StatusChamado.ABERTO, 1L, 1L, null);
        when(chamadoService.listarPorCliente(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(chamado)));

        mockMvc.perform(get("/clientes/1/chamados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].titulo").value("Sem internet"));
    }

    @Test
    void chamadosDeClienteInexistenteRetorna404() throws Exception {
        when(chamadoService.listarPorCliente(eq(99L), any(Pageable.class)))
                .thenThrow(new RecursoNaoEncontradoException("Cliente não encontrado: 99"));

        mockMvc.perform(get("/clientes/99/chamados")).andExpect(status().isNotFound());
    }
}