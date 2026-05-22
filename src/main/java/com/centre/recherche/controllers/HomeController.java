package com.centre.recherche.controllers;

import com.centre.recherche.models.Publication;
import com.centre.recherche.models.PublicationStatus;
import com.centre.recherche.models.PublicationType;
import com.centre.recherche.models.Researcher;
import com.centre.recherche.services.CategoryService;
import com.centre.recherche.services.PublicationService;
import com.centre.recherche.services.ResearcherService;
import com.centre.recherche.services.search.TFIDFService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Contrôleur des pages publiques du site (préfixe : /).
 * Gère la page d'accueil, la recherche de publications, le détail d'une publication,
 * les profils des chercheurs et le téléchargement des fichiers PDF.
 */
@Controller
public class HomeController {

    @Autowired
    private PublicationService publicationService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ResearcherService researcherService;

    @Autowired
    private TFIDFService tfidfService;

    /** Affiche la page d'accueil avec les publications les plus récentes. */
    @GetMapping("/")
    public String home(Model model) {
        Page<Publication> recent = publicationService.getPublishedPublications(
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "publicationYear")));
        model.addAttribute("publications", recent.getContent());
        model.addAttribute("totalPublished", publicationService.countPublishedPublications());
        return "index";
    }

    /** Recherche et affiche les publications publiées selon les critères saisis. */
    @GetMapping("/search")
    public String search(
            @RequestParam(name = "query", required = false, defaultValue = "") String query,
            @RequestParam(name = "type", required = false) PublicationType type,
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            @RequestParam(name = "year", required = false) Integer year,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10") int size,
            Model model) {

        Page<Publication> results = publicationService.searchPublishedPublications(
                query, type, categoryId, year,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publicationYear")));

        model.addAttribute("publications", results);
        model.addAttribute("query", query);
        model.addAttribute("type", type);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("year", year);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("publicationTypes", PublicationType.values());
        model.addAttribute("years", publicationService.getDistinctPublishedYears());
        model.addAttribute("tfidfReady", tfidfService.isIndexReady());
        model.addAttribute("tfidfDocs", tfidfService.getTotalDocuments());
        model.addAttribute("tfidfTerms", tfidfService.getVocabularySize());

        return "search";
    }

    /** Affiche le détail d'une publication publiée. */
    @GetMapping("/publication/{id}")
    public String viewPublication(@PathVariable Long id, Model model) {
        Publication publication = publicationService.getPublishedPublicationById(id);
        if (publication == null) return "redirect:/search";
        model.addAttribute("publication", publication);
        return "publication-detail";
    }

    /** Télécharge le fichier PDF d'une publication publiée. */
    @GetMapping("/download/pdf/{id}")
    public ResponseEntity<Resource> downloadPdf(@PathVariable Long id) throws IOException {
        Publication publication = publicationService.getPublishedPublicationById(id);
        if (publication == null || publication.getPdfFilePath() == null)
            return ResponseEntity.notFound().build();
        publicationService.incrementDownloadCount(id);
        Path pdfPath = Paths.get(publication.getPdfFilePath());
        Resource resource = new UrlResource(pdfPath.toUri());
        if (!resource.exists()) return ResponseEntity.notFound().build();
        String contentType = Files.probeContentType(pdfPath);
        if (contentType == null) contentType = "application/octet-stream";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    /** Affiche la liste de tous les chercheurs ayant des publications publiées. */
    @GetMapping("/researchers")
    public String listResearchers(Model model) {
        model.addAttribute("researchers", researcherService.findAllWithPublished());
        return "researchers";
    }

    /** Affiche le profil public d'un chercheur et ses publications publiées. */
    @GetMapping("/researchers/{id}")
    public String viewResearcher(@PathVariable Long id, Model model) {
        Researcher researcher = researcherService.findById(id);
        if (researcher == null) return "redirect:/researchers";
        List<Publication> published = researcher.getPublications() != null
                ? researcher.getPublications().stream()
                    .filter(p -> p.getStatus() == PublicationStatus.PUBLIEE)
                    .toList()
                : List.of();
        model.addAttribute("researcher", researcher);
        model.addAttribute("publications", published);
        return "researcher-profile";
    }
}