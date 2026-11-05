package br.com.univille.chamados.service;

import br.com.univille.chamados.dto.*;
import br.com.univille.chamados.entity.*;
import br.com.univille.chamados.exception.RecursoNaoEncontradoException;
import br.com.univille.chamados.exception.RegraNegocioException;
import br.com.univille.chamados.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChamadoService {

    private final ChamadoRepository chamadoRepository;
    private final ClienteRepository clienteRepository;
    private final CategoriaRepository categoriaRepository;
    private final TecnicoRepository tecnicoRepository;
    private final ComentarioRepository comentarioRepository;

    public ChamadoService(ChamadoRepository chamadoRepository,
                          ClienteRepository clienteRepository,
                          CategoriaRepository categoriaRepository,
                          TecnicoRepository tecnicoRepository,
                          ComentarioRepository comentarioRepository) {
        this.chamadoRepository = chamadoRepository;
        this.clienteRepository = clienteRepository;
        this.categoriaRepository = categoriaRepository;
        this.tecnicoRepository = tecnicoRepository;
        this.comentarioRepository = comentarioRepository;
    }

    public Page<ChamadoResponse> listar(StatusChamado status, Prioridade prioridade,
                                        Long clienteId, Pageable pageable) {
        return chamadoRepository
                .findAll(ChamadoSpecs.comFiltros(status, prioridade, clienteId), pageable)
                .map(this::toResponse);
    }

    public Page<ChamadoResponse> listarPorCliente(Long clienteId, Pageable pageable) {
        if (!clienteRepository.existsById(clienteId)) {
            throw new RecursoNaoEncontradoException("Cliente não encontrado: " + clienteId);
        }
        return listar(null, null, clienteId, pageable);
    }

    public ChamadoDetalheResponse buscarDetalhe(Long id) {
        Chamado c = buscarEntity(id);
        List<ComentarioResponse> comentarios = comentarioRepository
                .findByChamadoIdOrderByDataAsc(id).stream()
                .map(this::toComentarioResponse).toList();
        return new ChamadoDetalheResponse(
                c.getId(), c.getTitulo(), c.getDescricao(),
                c.getDataAbertura(), c.getDataFechamento(),
                c.getPrioridade(), c.getStatus(),
                c.getCliente().getId(), c.getCategoria().getId(),
                c.getTecnico() != null ? c.getTecnico().getId() : null,
                comentarios);
    }

    public ChamadoResponse criar(ChamadoRequest request) {
        Chamado chamado = new Chamado();
        preencher(chamado, request);
        chamado.setTecnico(buscarTecnico(request.tecnicoId()));
        chamado.setStatus(StatusChamado.ABERTO);
        chamado.setDataAbertura(LocalDateTime.now());
        return toResponse(chamadoRepository.save(chamado));
    }

    public ChamadoResponse atualizar(Long id, ChamadoRequest request) {
        Chamado chamado = buscarEntity(id);
        garantirNaoFechado(chamado);
        preencher(chamado, request);
        return toResponse(chamadoRepository.save(chamado));
    }

    public ChamadoResponse atribuir(Long id, Long tecnicoId) {
        Chamado chamado = buscarEntity(id);
        if (chamado.getStatus() == StatusChamado.FECHADO) {
            throw new RegraNegocioException("Não é possível atribuir técnico a um chamado fechado");
        }
        chamado.setTecnico(buscarTecnico(tecnicoId));
        return toResponse(chamadoRepository.save(chamado));
    }

    public ChamadoResponse alterarStatus(Long id, StatusChamado novo) {
        Chamado chamado = buscarEntity(id);
        garantirNaoFechado(chamado);

        StatusChamado atual = chamado.getStatus();
        if (!atual.podeIrPara(novo)) {
            throw new RegraNegocioException(
                    "Mudança de status inválida: de " + atual + " para " + novo);
        }
        if (novo == StatusChamado.FECHADO) {
            chamado.setDataFechamento(LocalDateTime.now());
        }
        chamado.setStatus(novo);
        return toResponse(chamadoRepository.save(chamado));
    }

    public ComentarioResponse adicionarComentario(Long chamadoId, ComentarioRequest request) {
        Chamado chamado = buscarEntity(chamadoId);
        Comentario comentario = new Comentario();
        comentario.setTexto(request.texto());
        comentario.setAutor(request.autor());
        comentario.setData(LocalDateTime.now());
        comentario.setChamado(chamado);
        return toComentarioResponse(comentarioRepository.save(comentario));
    }

    @Transactional
    public void deletar(Long id) {
        Chamado chamado = buscarEntity(id);
        comentarioRepository.deleteByChamadoId(id);
        chamadoRepository.delete(chamado);
    }

    private void garantirNaoFechado(Chamado chamado) {
        if (chamado.getStatus() == StatusChamado.FECHADO) {
            throw new RegraNegocioException("Chamado fechado não pode ser alterado");
        }
    }

    private Tecnico buscarTecnico(Long tecnicoId) {
        if (tecnicoId == null) {
            return null;
        }
        return tecnicoRepository.findById(tecnicoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Técnico não encontrado: " + tecnicoId));
    }

    private void preencher(Chamado chamado, ChamadoRequest request) {
        chamado.setTitulo(request.titulo());
        chamado.setDescricao(request.descricao());
        chamado.setPrioridade(request.prioridade());

        chamado.setCliente(clienteRepository.findById(request.clienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado: " + request.clienteId())));

        chamado.setCategoria(categoriaRepository.findById(request.categoriaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada: " + request.categoriaId())));
    }

    private Chamado buscarEntity(Long id) {
        return chamadoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Chamado não encontrado: " + id));
    }

    private ChamadoResponse toResponse(Chamado c) {
        return new ChamadoResponse(
                c.getId(), c.getTitulo(), c.getDescricao(),
                c.getDataAbertura(), c.getDataFechamento(),
                c.getPrioridade(), c.getStatus(),
                c.getCliente().getId(), c.getCategoria().getId(),
                c.getTecnico() != null ? c.getTecnico().getId() : null);
    }

    private ComentarioResponse toComentarioResponse(Comentario c) {
        return new ComentarioResponse(c.getId(), c.getTexto(), c.getAutor(), c.getData());
    }
}