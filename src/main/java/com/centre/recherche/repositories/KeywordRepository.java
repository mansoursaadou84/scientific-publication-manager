package com.centre.recherche.repositories;

import com.centre.recherche.models.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Depot JPA pour l'entite {@link Keyword}.
 * Fournit les operations de base et une methode de recherche par nom.
 */
@Repository
public interface KeywordRepository extends JpaRepository<Keyword, Long> {

    /**
     * Recherche un mot-cle par son nom.
     *
     * @param name le nom du mot-cle
     * @return le mot-cle correspondant, ou null si aucun trouve
     */
    Keyword findByName(String name);
}
