package com.centre.recherche.repositories;

import com.centre.recherche.models.Publication;
import com.centre.recherche.models.PublicationStatus;
import com.centre.recherche.models.PublicationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Depot JPA pour l'entite {@link Publication}.
 * Fournit les operations CRUD de base ainsi que des requetes personnalisees
 * pour la recherche, le filtrage et les statistiques des publications.
 */
@Repository
public interface PublicationRepository extends JpaRepository<Publication, Long> {

    List<Publication> findByStatus(PublicationStatus status);

    /**
     * Recherche les publications d'un chercheur, triees par annee de publication decroissante.
     *
     * @param researcherId l'identifiant du chercheur
     * @return la liste des publications du chercheur
     */
    @Query("SELECT DISTINCT p FROM Publication p JOIN p.authors a WHERE a.id = :researcherId ORDER BY p.publicationYear DESC")
    List<Publication> findByAuthorId(@Param("researcherId") Long researcherId);

    /**
     * Recherche les publications d'un chercheur filtrees par statut.
     *
     * @param researcherId l'identifiant du chercheur
     * @param status        le statut des publications a rechercher
     * @return la liste des publications correspondant aux criteres
     */
    @Query("SELECT DISTINCT p FROM Publication p JOIN p.authors a WHERE a.id = :researcherId AND p.status = :status")
    List<Publication> findByAuthorIdAndStatus(@Param("researcherId") Long researcherId, @Param("status") PublicationStatus status);

    Page<Publication> findByStatus(PublicationStatus status, Pageable pageable);

    long countByStatus(PublicationStatus status);

    /**
     * Recherche multi-criteres parmi les publications publiees.
     * Filtre par texte libre (titre, resume, mot-cle, auteur, identifiant, DOI),
     * par type de publication, par categorie et par annee.
     *
     * @param status     le statut des publications (generalement PUBLIEE)
     * @param query      texte de recherche libre, peut etre null ou vide
     * @param type       type de publication, peut etre null
     * @param categoryId identifiant de la categorie, peut etre null
     * @param year       annee de publication, peut etre null
     * @param pageable   informations de pagination
     * @return une page de publications correspondant aux criteres
     */
    @Query("SELECT DISTINCT p FROM Publication p " +
           "LEFT JOIN p.authors a " +
           "LEFT JOIN p.keywords k " +
           "WHERE p.status = :status " +
           "AND (:query IS NULL OR :query = '' " +
           "     OR LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "     OR LOWER(p.resume) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "     OR LOWER(k.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "     OR LOWER(a.firstName) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "     OR LOWER(a.lastName) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "     OR LOWER(p.uniqueIdentifier) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "     OR LOWER(p.doi) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND (:type IS NULL OR p.type = :type) " +
           "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
           "AND (:year IS NULL OR p.publicationYear = :year)")
    Page<Publication> searchPublished(@Param("status") PublicationStatus status,
                                       @Param("query") String query,
                                       @Param("type") PublicationType type,
                                       @Param("categoryId") Long categoryId,
                                       @Param("year") Integer year,
                                       Pageable pageable);

    /**
     * Recupere les annees de publication distinctes pour un statut donne, triees par ordre decroissant.
     *
     * @param status le statut des publications
     * @return la liste des annees distinctes
     */
    @Query("SELECT DISTINCT p.publicationYear FROM Publication p " +
           "WHERE p.status = :status AND p.publicationYear IS NOT NULL " +
           "ORDER BY p.publicationYear DESC")
    List<Integer> findDistinctPublishedYears(@Param("status") PublicationStatus status);

    /**
     * Incremente de 1 le compteur de telechargements d'une publication.
     *
     * @param id l'identifiant de la publication
     */
    @Modifying
    @Query("UPDATE Publication p SET p.downloadCount = p.downloadCount + 1 WHERE p.id = :id")
    void incrementDownloadCount(@Param("id") Long id);
}