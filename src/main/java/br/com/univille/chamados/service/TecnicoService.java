package br.com.univille.chamados.service;

import br.com.univille.chamados.dto.TecnicoRequest;
import br.com.univille.chamados.dto.TecnicoResponse;
import br.com.univille.chamados.entity.Tecnico;
import br.com.univille.chamados.exception.RecursoNaoEncontradoException;
import br.com.univille.chamados.exception.RegraNegocioException;
import br.com.univille.chamados.repository.TecnicoRepository;
import br.com.univille.chamados.repository.ChamadoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TecnicoService {

    private final TecnicoRepository repository;
    private final ChamadoRepository chamadoRepository;

    public TecnicoService(TecnicoRepository repository, ChamadoRepository chamadoRepository) {
        this.repository = repository;
        this.chamadoRepository = chamadoRepository;
    }

    public List<TecnicoResponse> listar() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    public TecnicoResponse buscarPorId(Long id) {
        return toResponse(buscarEntity(id));
    }

    public TecnicoResponse criar(TecnicoRequest request) {
        if (repository.existsByEmailIgnoreCase(request.email())) {
            throw new RegraNegocioException("Já existe um técnico com o e-mail: " + request.email());
        }
        Tecnico tecnico = new Tecnico();
        preencher(tecnico, request);
        return toResponse(repository.save(tecnico));
    }

    public TecnicoResponse atualizar(Long id, TecnicoRequest request) {
        Tecnico tecnico = buscarEntity(id);
        if (repository.existsByEmailIgnoreCaseAndIdNot(request.email(), id)) {
            throw new RegraNegocioException("Já existe um técnico com o e-mail: " + request.email());
        }
        preencher(tecnico, request);
        return toResponse(repository.save(tecnico));
    }

   public void deletar(Long id) {
    Tecnico tecnico = buscarEntity(id);
    if (chamadoRepository.existsByTecnicoId(id)) {
        throw new RegraNegocioException(
            "Não é possível excluir o técnico, pois há chamados vinculados a ele");
    }
    repository.delete(tecnico);
}

    private void preencher(Tecnico tecnico, TecnicoRequest request) {
        tecnico.setNome(request.nome());
        tecnico.setEmail(request.email());
        tecnico.setPerfil(request.perfil());
    }

    private Tecnico buscarEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Técnico não encontrado: " + id));
    }

    private TecnicoResponse toResponse(Tecnico t) {
        return new TecnicoResponse(t.getId(), t.getNome(), t.getEmail(), t.getPerfil());
    }
}