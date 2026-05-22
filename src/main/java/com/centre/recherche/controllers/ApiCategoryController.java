package com.centre.recherche.controllers;

import com.centre.recherche.models.Category;
import com.centre.recherche.services.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST de gestion des catégories (préfixe : /api/categories).
 * Fournit les opérations de listing, consultation, création et suppression des catégories.
 */
@RestController
@RequestMapping("/api/categories")
@Tag(name = "Catégories", description = "API de gestion des catégories de publications")
public class ApiCategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Lister toutes les catégories")
    public ResponseEntity<List<Category>> list() {
        return ResponseEntity.ok(categoryService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir une catégorie par ID")
    public ResponseEntity<Category> getById(@PathVariable Long id) {
        Category cat = categoryService.findById(id);
        if (cat == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(cat);
    }

    @PostMapping
    @Operation(summary = "Créer une catégorie")
    public ResponseEntity<Category> create(@RequestBody Category category) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.save(category));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une catégorie")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}