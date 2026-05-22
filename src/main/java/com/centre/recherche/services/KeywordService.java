package com.centre.recherche.services;

import com.centre.recherche.models.Keyword;
import com.centre.recherche.repositories.KeywordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service de gestion des mots-cles associes aux publications.
 * <p>
 * Fournit les operations CRUD de base pour l'entite {@link Keyword}
 * en deleguant au {@link KeywordRepository}.
 * </p>
 *
 * @see Keyword
 * @see KeywordRepository
 */
@Service
public class KeywordService {

    @Autowired
    private KeywordRepository keywordRepository;

    /** Retourne tous les mots-cles. */
    public List<Keyword> findAll() {
        return keywordRepository.findAll();
    }

    /**
     * Recherche un mot-cle par son identifiant.
     *
     * @param id l'identifiant du mot-cle
     * @return le mot-cle correspondant, ou null si introuvable
     */
    public Keyword findById(Long id) {
        return keywordRepository.findById(id).orElse(null);
    }

    /**
     * Sauvegarde un mot-cle (creation ou mise a jour).
     *
     * @param keyword le mot-cle a sauvegarder
     * @return le mot-cle sauvegarde
     */
    public Keyword save(Keyword keyword) {
        return keywordRepository.save(keyword);
    }

    /**
     * Supprime un mot-cle par son identifiant.
     *
     * @param id l'identifiant du mot-cle a supprimer
     */
    public void deleteById(Long id) {
        keywordRepository.deleteById(id);
    }
}
