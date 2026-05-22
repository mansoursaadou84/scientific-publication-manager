package com.centre.recherche.services;

import com.centre.recherche.models.Category;
import com.centre.recherche.repositories.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service de gestion des categories de publications.
 * <p>
 * Fournit les operations CRUD de base pour l'entite {@link Category}
 * en deleguant au {@link CategoryRepository}.
 * </p>
 *
 * @see Category
 * @see CategoryRepository
 */
@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    /** Retourne toutes les categories. */
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    /**
     * Recherche une categorie par son identifiant.
     *
     * @param id l'identifiant de la categorie
     * @return la categorie correspondante, ou null si introuvable
     */
    public Category findById(Long id) {
        return categoryRepository.findById(id).orElse(null);
    }

    /**
     * Sauvegarde une categorie (creation ou mise a jour).
     *
     * @param category la categorie a sauvegarder
     * @return la categorie sauvegardee
     */
    public Category save(Category category) {
        return categoryRepository.save(category);
    }

    /**
     * Supprime une categorie par son identifiant.
     *
     * @param id l'identifiant de la categorie a supprimer
     */
    public void deleteById(Long id) {
        categoryRepository.deleteById(id);
    }
}
