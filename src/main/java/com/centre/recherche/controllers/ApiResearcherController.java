package com.centre.recherche.controllers;

import com.centre.recherche.models.Researcher;
import com.centre.recherche.services.ResearcherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST de consultation des chercheurs (préfixe : /api/researchers).
 * Fournit le listing et le détail des chercheurs avec leurs publications.
 */
@RestController
@RequestMapping("/api/researchers")
@Tag(name = "Chercheurs", description = "API de consultation des chercheurs")
public class ApiResearcherController {

    @Autowired
    private ResearcherService researcherService;

    @GetMapping
    @Operation(summary = "Lister les chercheurs avec publications publiées")
    public ResponseEntity<List<Researcher>> list() {
        return ResponseEntity.ok(researcherService.findAllWithPublished());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détail d'un chercheur")
    public ResponseEntity<Researcher> getById(@PathVariable Long id) {
        Researcher r = researcherService.findById(id);
        if (r == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(r);
    }
}