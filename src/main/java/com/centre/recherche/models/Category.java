package com.centre.recherche.models;

import jakarta.persistence.*;

/**
 * Entite JPA representant une categorie disciplinaire.
 *
 * <p>Les categories permettent de classer les publications par domaine
 * (ex : Informatique, Physique, Mathematiques).</p>
 */
@Entity
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    public Category() {}
    public Category(String name) { this.name = name; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
