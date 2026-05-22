package com.centre.recherche.controllers;

import com.centre.recherche.models.Category;
import com.centre.recherche.models.Keyword;
import com.centre.recherche.models.Publication;
import com.centre.recherche.models.PublicationStatus;
import com.centre.recherche.models.PublicationType;
import com.centre.recherche.services.CategoryService;
import com.centre.recherche.services.KeywordService;
import com.centre.recherche.services.PublicationService;
import com.centre.recherche.services.ResearcherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Contrôleur REST de gestion des publications (préfixe : /api/publications).
 * Fournit les opérations CRUD, la recherche paginée et le compteur de téléchargements.
 */
@RestController
@RequestMapping("/api/publications")
@Tag(name = "Publications", description = "API de gestion des publications scientifiques")
public class ApiPublicationController {

    @Autowired
    private PublicationService publicationService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private KeywordService keywordService;

    @Autowired
    private ResearcherService researcherService;

    @GetMapping
    @Operation(summary = "Lister les publications publiées", description = "Retourne les publications publiées avec pagination et filtres")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste des publications")
    })
    public ResponseEntity<Page<Publication>> listPublished(
            @Parameter(description = "Terme de recherche") @RequestParam(required = false) String query,
            @Parameter(description = "Type de publication") @RequestParam(required = false) PublicationType type,
            @Parameter(description = "ID de la catégorie") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Année de publication") @RequestParam(required = false) Integer year,
            @Parameter(description = "Numéro de page") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Taille de page") @RequestParam(defaultValue = "10") int size) {

        Page<Publication> results = publicationService.searchPublishedPublications(
                query, type, categoryId, year,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publicationYear")));
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détail d'une publication", description = "Retourne les détails d'une publication publiée")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Publication trouvée"),
        @ApiResponse(responseCode = "404", description = "Publication non trouvée")
    })
    public ResponseEntity<Publication> getById(@PathVariable Long id) {
        Publication pub = publicationService.getPublishedPublicationById(id);
        if (pub == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(pub);
    }

    @GetMapping("/all")
    @Operation(summary = "Lister toutes les publications (admin)", description = "Retourne toutes les publications quel que soit le statut")
    public ResponseEntity<List<Publication>> listAll() {
        return ResponseEntity.ok(publicationService.getAllPublications());
    }

    @PostMapping
    @Operation(summary = "Créer une publication", description = "Soumet une nouvelle publication")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Publication créée"),
        @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    public ResponseEntity<Publication> create(@RequestBody Publication publication) {
        if (publication.getUniqueIdentifier() == null || publication.getUniqueIdentifier().isEmpty()) {
            publication.setUniqueIdentifier("CRIS-" + System.currentTimeMillis());
        }
        publication.setStatus(PublicationStatus.BROUILLON);
        Publication saved = publicationService.savePublication(publication, null, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une publication", description = "Met à jour une publication existante")
    public ResponseEntity<Publication> update(@PathVariable Long id, @RequestBody Publication publication) {
        Publication existing = publicationService.getPublicationById(id);
        if (existing == null) return ResponseEntity.notFound().build();
        publication.setId(id);
        Publication saved = publicationService.savePublication(publication, null, null);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une publication", description = "Supprime une publication par son ID")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Publication supprimée"),
        @ApiResponse(responseCode = "404", description = "Publication non trouvée")
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Publication existing = publicationService.getPublicationById(id);
        if (existing == null) return ResponseEntity.notFound().build();
        publicationService.deletePublication(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count")
    @Operation(summary = "Compter les publications publiées")
    public ResponseEntity<Map<String, Long>> count() {
        return ResponseEntity.ok(Map.of("totalPublished", publicationService.countPublishedPublications()));
    }

    @GetMapping("/years")
    @Operation(summary = "Années distinctes des publications publiées")
    public ResponseEntity<List<Integer>> distinctYears() {
        return ResponseEntity.ok(publicationService.getDistinctPublishedYears());
    }

    @PostMapping("/{id}/download")
    @Operation(summary = "Incrémenter le compteur de téléchargement")
    public ResponseEntity<Void> incrementDownload(@PathVariable Long id) {
        publicationService.incrementDownloadCount(id);
        return ResponseEntity.ok().build();
    }
}