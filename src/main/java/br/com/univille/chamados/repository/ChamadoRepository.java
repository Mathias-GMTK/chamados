package br.com.univille.chamados.repository;

import br.com.univille.chamados.entity.Chamado;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChamadoRepository extends JpaRepository<Chamado, Long> {

    boolean existsByCategoriaId(Long categoriaId);

    boolean existsByTecnicoId(Long tecnicoId);
}