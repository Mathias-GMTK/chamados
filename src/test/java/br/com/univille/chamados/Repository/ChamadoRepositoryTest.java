package br.com.univille.chamados.Repository;

import br.com.univille.chamados.entity.*;
import br.com.univille.chamados.repository.CategoriaRepository;
import br.com.univille.chamados.repository.ChamadoRepository;
import br.com.univille.chamados.repository.ChamadoSpecs;
import br.com.univille.chamados.repository.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ChamadoRepositoryTest {

    @Autowired
    ChamadoRepository chamadoRepository;
    @Autowired
    ClienteRepository clienteRepository;
    @Autowired
    CategoriaRepository categoriaRepository;

    private Cliente cliente;
    private Categoria categoria;

    @BeforeEach
    void preparar() {
        cliente = clienteRepository.save(new Cliente("Ana", "ana@teste.com", "47999990000"));
        Categoria nova = new Categoria();
        nova.setNome("Rede");
        categoria = categoriaRepository.save(nova);

        salvar("Sem internet", StatusChamado.ABERTO, Prioridade.ALTA);
        salvar("Mouse quebrado", StatusChamado.FECHADO, Prioridade.BAIXA);
    }

    private void salvar(String titulo, StatusChamado status, Prioridade prioridade) {
        Chamado c = new Chamado();
        c.setTitulo(titulo);
        c.setStatus(status);
        c.setPrioridade(prioridade);
        c.setDataAbertura(LocalDateTime.now());
        c.setCliente(cliente);
        c.setCategoria(categoria);
        chamadoRepository.save(c);
    }

    @Test
    void semFiltrosRetornaTodos() {
        Page<Chamado> pagina = chamadoRepository.findAll(
                ChamadoSpecs.comFiltros(null, null, null), PageRequest.of(0, 10));
        assertThat(pagina.getTotalElements()).isEqualTo(2);
    }

    @Test
    void filtraPorStatus() {
        Page<Chamado> pagina = chamadoRepository.findAll(
                ChamadoSpecs.comFiltros(StatusChamado.ABERTO, null, null), PageRequest.of(0, 10));
        assertThat(pagina.getTotalElements()).isEqualTo(1);
        assertThat(pagina.getContent().get(0).getTitulo()).isEqualTo("Sem internet");
    }

    @Test
    void combinaPrioridadeECliente() {
        Page<Chamado> pagina = chamadoRepository.findAll(
                ChamadoSpecs.comFiltros(null, Prioridade.BAIXA, cliente.getId()), PageRequest.of(0, 10));
        assertThat(pagina.getTotalElements()).isEqualTo(1);
        assertThat(pagina.getContent().get(0).getTitulo()).isEqualTo("Mouse quebrado");
    }

    @Test
    void filtroSemResultadoRetornaVazio() {
        Page<Chamado> pagina = chamadoRepository.findAll(
                ChamadoSpecs.comFiltros(StatusChamado.ABERTO, Prioridade.BAIXA, null), PageRequest.of(0, 10));
        assertThat(pagina.getTotalElements()).isZero();
    }

    @Test
    void paginaRespeitaTamanho() {
        Page<Chamado> pagina = chamadoRepository.findAll(
                ChamadoSpecs.comFiltros(null, null, null), PageRequest.of(0, 1));
        assertThat(pagina.getContent()).hasSize(1);
        assertThat(pagina.getTotalPages()).isEqualTo(2);
    }

    @Test
    void existePorCliente() {
        assertThat(chamadoRepository.existsByClienteId(cliente.getId())).isTrue();
        assertThat(chamadoRepository.existsByClienteId(999L)).isFalse();
    }
}