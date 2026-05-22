package com.centre.recherche.repositories;

import com.centre.recherche.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Depot JPA pour l'entite {@link Category}.
 * Fournit les operations de base et une methode de recherche par nom.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Recherche une categorie par son nom.
     *
     * @param name le nom de la categorie
     * @return la categorie correspondante, ou null si aucune trouvee
     */
    Category findByName(String name);
}
