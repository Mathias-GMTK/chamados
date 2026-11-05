package br.com.univille.chamados.repository;

import br.com.univille.chamados.entity.Chamado;
import br.com.univille.chamados.entity.Prioridade;
import br.com.univille.chamados.entity.StatusChamado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ChamadoRepository
        extends JpaRepository<Chamado, Long>, JpaSpecificationExecutor<Chamado> {

    boolean existsByCategoriaId(Long categoriaId);
    boolean existsByClienteId(Long clienteId);
    boolean existsByTecnicoId(Long tecnicoId);

    long countByStatus(StatusChamado status);
    long countByPrioridade(Prioridade prioridade);
}