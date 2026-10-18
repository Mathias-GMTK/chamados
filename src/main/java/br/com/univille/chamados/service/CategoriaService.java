package br.com.univille.chamados.service;

import br.com.univille.chamados.dto.CategoriaRequest;
import br.com.univille.chamados.dto.CategoriaResponse;
import br.com.univille.chamados.entity.Categoria;
import br.com.univille.chamados.exception.RecursoNaoEncontradoException;
import br.com.univille.chamados.exception.RegraNegocioException;
import br.com.univille.chamados.repository.CategoriaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository repository;

    public CategoriaService(CategoriaRepository repository) {
        this.repository = repository;
    }

    public List<CategoriaResponse> listar() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    public CategoriaResponse buscarPorId(Long id) {
        return toResponse(buscarEntity(id));
    }

    public CategoriaResponse criar(CategoriaRequest request) {
        if (repository.existsByNomeIgnoreCase(request.nome())) {
            throw new RegraNegocioException("Já existe uma categoria com o nome: " + request.nome());
        }
        Categoria categoria = new Categoria();
        categoria.setNome(request.nome());
        categoria.setDescricao(request.descricao());
        return toResponse(repository.save(categoria));
    }

    public CategoriaResponse atualizar(Long id, CategoriaRequest request) {
        Categoria categoria = buscarEntity(id);
        if (repository.existsByNomeIgnoreCaseAndIdNot(request.nome(), id)) {
            throw new RegraNegocioException("Já existe uma categoria com o nome: " + request.nome());
        }
        categoria.setNome(request.nome());
        categoria.setDescricao(request.descricao());
        return toResponse(repository.save(categoria));
    }

    public void deletar(Long id) {
        repository.delete(buscarEntity(id));
    }

    private Categoria buscarEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada: " + id));
    }

    private CategoriaResponse toResponse(Categoria c) {
        return new CategoriaResponse(c.getId(), c.getNome(), c.getDescricao());
    }
}