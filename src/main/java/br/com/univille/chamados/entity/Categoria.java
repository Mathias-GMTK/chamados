package br.com.univille.chamados.entity;


import jakarta.persistence.*;

@Entity
@Table(name = "categorias")
public class Categoria {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String nome;

    @Column(length = 100)
    private String descricao;

}
