package br.com.univille.chamados.service;

import br.com.univille.chamados.dto.ChamadoRequest;
import br.com.univille.chamados.dto.ChamadoResponse;
import br.com.univille.chamados.entity.*;
import br.com.univille.chamados.exception.RecursoNaoEncontradoException;
import br.com.univille.chamados.exception.RegraNegocioException;
import br.com.univille.chamados.repository.CategoriaRepository;
import br.com.univille.chamados.repository.ChamadoRepository;
import br.com.univille.chamados.repository.ClienteRepository;
import br.com.univille.chamados.repository.TecnicoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChamadoService {

    private final ChamadoRepository chamadoRepository;
    private final ClienteRepository clienteRepository;
    private final CategoriaRepository categoriaRepository;
    private final TecnicoRepository tecnicoRepository;

    public ChamadoService(ChamadoRepository chamadoRepository,
                          ClienteRepository clienteRepository,
                          CategoriaRepository categoriaRepository,
                          TecnicoRepository tecnicoRepository) {
        this.chamadoRepository = chamadoRepository;
        this.clienteRepository = clienteRepository;
        this.categoriaRepository = categoriaRepository;
        this.tecnicoRepository = tecnicoRepository;
    }

    public List<ChamadoResponse> listar() {
        return chamadoRepository.findAll().stream().map(this::toResponse).toList();
    }

    public ChamadoResponse buscarPorId(Long id) {
        return toResponse(buscarEntity(id));
    }

    public ChamadoResponse criar(ChamadoRequest request) {
        Chamado chamado = new Chamado();
        preencher(chamado, request);
        chamado.setStatus(StatusChamado.ABERTO);
        chamado.setDataAbertura(LocalDateTime.now());
        return toResponse(chamadoRepository.save(chamado));
    }

   public ChamadoResponse atualizar(Long id, ChamadoRequest request) {
    Chamado chamado = buscarEntity(id);

    if (request.status() != null && !chamado.getStatus().podeIrPara(request.status())) {
        throw new RegraNegocioException(
            "Mudança de status inválida: de " + chamado.getStatus()
            + " para " + request.status());
    }

    preencher(chamado, request);

    if (request.status() != null && request.status() != chamado.getStatus()) {
        chamado.setStatus(request.status());
        if (request.status() == StatusChamado.FECHADO) {
            chamado.setDataFechamento(LocalDateTime.now());
        }
    }
    return toResponse(chamadoRepository.save(chamado));
}

    public void deletar(Long id) {
        chamadoRepository.delete(buscarEntity(id));
    }

    private void preencher(Chamado chamado, ChamadoRequest request) {
        chamado.setTitulo(request.titulo());
        chamado.setDescricao(request.descricao());
        chamado.setPrioridade(request.prioridade());

        chamado.setCliente(clienteRepository.findById(request.clienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado: " + request.clienteId())));

        chamado.setCategoria(categoriaRepository.findById(request.categoriaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada: " + request.categoriaId())));

        if (request.tecnicoId() != null) {
            chamado.setTecnico(tecnicoRepository.findById(request.tecnicoId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Técnico não encontrado: " + request.tecnicoId())));
        } else {
            chamado.setTecnico(null);
        }
    }

    private Chamado buscarEntity(Long id) {
        return chamadoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Chamado não encontrado: " + id));
    }

    private ChamadoResponse toResponse(Chamado c) {
        return new ChamadoResponse(
                c.getId(),
                c.getTitulo(),
                c.getDescricao(),
                c.getDataAbertura(),
                c.getDataFechamento(),
                c.getPrioridade(),
                c.getStatus(),
                c.getCliente().getId(),
                c.getCategoria().getId(),
                c.getTecnico() != null ? c.getTecnico().getId() : null
        );
    }
}