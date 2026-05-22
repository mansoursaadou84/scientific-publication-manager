package com.centre.recherche.repositories;

import com.centre.recherche.models.Researcher;
import com.centre.recherche.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Depot JPA pour l'entite {@link Researcher}.
 * Fournit les operations de base ainsi que des methodes de recherche
 * par utilisateur associe et par publications publiees.
 */
@Repository
public interface ResearcherRepository extends JpaRepository<Researcher, Long> {

    /**
     * Recherche un chercheur a partir de l'utilisateur associe.
     *
     * @param user l'utilisateur lie au chercheur
     * @return le chercheur correspondant, ou null si aucun trouve
     */
    Researcher findByUser(User user);

    /**
     * Recherche les chercheurs ayant au moins une publication publiee,
     * tries par nom puis prenom.
     *
     * @return la liste des chercheurs ayant des publications publiees
     */
    @Query("SELECT DISTINCT r FROM Researcher r JOIN r.publications p WHERE p.status = 'PUBLIEE' ORDER BY r.lastName, r.firstName")
    List<Researcher> findWithPublishedPublications();
}
