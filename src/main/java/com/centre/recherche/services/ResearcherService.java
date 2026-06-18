package com.centre.recherche.services;

import com.centre.recherche.models.Researcher;
import com.centre.recherche.models.User;
import com.centre.recherche.repositories.ResearcherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service de gestion des profils chercheurs.
 * <p>
 * Fournit les operations de base : recherche par utilisateur associe,
 * consultation, sauvegarde et liste des chercheurs ayant des publications publiees.
 * </p>
 *
 * @see Researcher
 * @see ResearcherRepository
 */
@Service
public class ResearcherService {

    @Autowired
    private ResearcherRepository researcherRepository;

    /** Retourne le profil chercheur associe a un utilisateur donne. */
    public Researcher findByUser(User user) {
        return researcherRepository.findByUser(user);
    }

    /** Sauvegarde un profil chercheur. */
    public Researcher save(Researcher researcher) {
        return researcherRepository.save(researcher);
    }

    /** Retourne un chercheur par son identifiant, ou null si introuvable. */
    public Researcher findById(Long id) {
        return researcherRepository.findById(id).orElse(null);
    }

    /** Retourne la liste des chercheurs ayant au moins une publication publiee. */
    public List<Researcher> findAllWithPublished() {
        return researcherRepository.findWithPublishedPublications();
    }

    /** Retourne la liste de tous les chercheurs. */
    public List<Researcher> findAll() {
        return researcherRepository.findAll();
    }
}