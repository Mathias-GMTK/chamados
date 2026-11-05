package br.com.univille.chamados.repository;

import br.com.univille.chamados.entity.Cliente;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
class ClienteRepositoryTest {

    @Autowired ClienteRepository repository;

    @Test
    void encontraEmailExistente() {
        repository.save(new Cliente("Ana", "ana@teste.com", null));
        assertThat(repository.existsByEmail("ana@teste.com")).isTrue();
        assertThat(repository.existsByEmail("outro@teste.com")).isFalse();
    }

    @Test
    void naoPermiteEmailDuplicado() {
        repository.saveAndFlush(new Cliente("Ana", "ana@teste.com", null));
        assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(new Cliente("Outra Ana", "ana@teste.com", null)));
    }
}