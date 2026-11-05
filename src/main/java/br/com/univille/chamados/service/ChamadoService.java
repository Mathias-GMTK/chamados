package br.com.univille.chamados.service;

import br.com.univille.chamados.dto.ChamadoRequest;
import br.com.univille.chamados.dto.ChamadoResponse;
import br.com.univille.chamados.entity.Chamado;
import br.com.univille.chamados.entity.StatusChamado;
import br.com.univille.chamados.entity.Tecnico;
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
        chamado.setTecnico(buscarTecnico(request.tecnicoId())); // null é permitido na criação
        chamado.setStatus(StatusChamado.ABERTO);
        chamado.setDataAbertura(LocalDateTime.now());
        return toResponse(chamadoRepository.save(chamado));
    }

    public ChamadoResponse atualizar(Long id, ChamadoRequest request) {
        Chamado chamado = buscarEntity(id);
        garantirNaoFechado(chamado);
        preencher(chamado, request); // não altera status nem técnico
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

    public void deletar(Long id) {
        chamadoRepository.delete(buscarEntity(id));
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