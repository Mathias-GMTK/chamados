package br.com.univille.chamados.repository;

import br.com.univille.chamados.entity.Chamado;
import br.com.univille.chamados.entity.Prioridade;
import br.com.univille.chamados.entity.StatusChamado;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ChamadoSpecs {

    private ChamadoSpecs() {}

    public static Specification<Chamado> comFiltros(StatusChamado status,
                                                    Prioridade prioridade,
                                                    Long clienteId) {
        return (root, query, cb) -> {
            List<Predicate> filtros = new ArrayList<>();
            if (status != null) {
                filtros.add(cb.equal(root.get("status"), status));
            }
            if (prioridade != null) {
                filtros.add(cb.equal(root.get("prioridade"), prioridade));
            }
            if (clienteId != null) {
                filtros.add(cb.equal(root.get("cliente").get("id"), clienteId));
            }
            return cb.and(filtros.toArray(new Predicate[0]));
        };
    }
}