package com.centre.recherche.controllers;

import com.centre.recherche.models.Keyword;
import com.centre.recherche.services.KeywordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST de gestion des mots-clés (préfixe : /api/keywords).
 * Fournit le listing, la création et la suppression des mots-clés.
 */
@RestController
@RequestMapping("/api/keywords")
@Tag(name = "Mots-clés", description = "API de gestion des mots-clés")
public class ApiKeywordController {

    @Autowired
    private KeywordService keywordService;

    @GetMapping
    @Operation(summary = "Lister tous les mots-clés")
    public ResponseEntity<List<Keyword>> list() {
        return ResponseEntity.ok(keywordService.findAll());
    }

    @PostMapping
    @Operation(summary = "Créer un mot-clé")
    public ResponseEntity<Keyword> create(@RequestBody Keyword keyword) {
        return ResponseEntity.status(HttpStatus.CREATED).body(keywordService.save(keyword));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un mot-clé")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        keywordService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}