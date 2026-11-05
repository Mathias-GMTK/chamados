package br.com.univille.chamados.repository;

import br.com.univille.chamados.entity.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComentarioRepository extends JpaRepository<Comentario, Long> {

    List<Comentario> findByChamadoIdOrderByDataAsc(Long chamadoId);

    void deleteByChamadoId(Long chamadoId);
}