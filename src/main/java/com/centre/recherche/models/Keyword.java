package com.centre.recherche.models;

import jakarta.persistence.*;

/**
 * Entite JPA representant un mot-cle associe aux publications.
 *
 * <p>Les mots-cles sont partages entre publications et indexes
 * pour la recherche TF-IDF.</p>
 */
@Entity
public class Keyword {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    public Keyword() {}
    public Keyword(String name) { this.name = name; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
