package com.centre.recherche.controllers;

import com.centre.recherche.models.Keyword;
import com.centre.recherche.models.Publication;
import com.centre.recherche.models.PublicationStatus;
import com.centre.recherche.models.PublicationType;
import com.centre.recherche.models.Researcher;
import com.centre.recherche.models.User;
import com.centre.recherche.repositories.CategoryRepository;
import com.centre.recherche.repositories.KeywordRepository;
import com.centre.recherche.repositories.PublicationRepository;
import com.centre.recherche.services.CategoryService;
import com.centre.recherche.services.ResearcherService;
import com.centre.recherche.services.UserService;
import com.centre.recherche.services.WorkflowService;
import com.centre.recherche.services.export.ExcelGeneratorService;
import com.centre.recherche.services.export.PdfGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Contrôleur de l'espace chercheur (préfixe : /researcher).
 * Gère le tableau de bord, le profil chercheur, le CRUD des publications,
 * les actions du workflow, les exports PDF/Excel et les statistiques individuelles.
 */
@Controller
@RequestMapping("/researcher")
public class ResearcherController {

    @Autowired
    private ResearcherService researcherService;

    @Autowired
    private PublicationRepository publicationRepository;

    @Autowired
    private KeywordRepository keywordRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private UserService userService;

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private PdfGeneratorService pdfGeneratorService;

    @Autowired
    private ExcelGeneratorService excelGeneratorService;

    private static final String UPLOAD_DIR = "./uploads";

    /** Redirige vers le tableau de bord chercheur. */
    @GetMapping
    public String root() {
        return "redirect:/researcher/dashboard";
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userService.findByUsername(auth.getName());
    }

    private Researcher getCurrentResearcher() {
        return researcherService.findByUser(getCurrentUser());
    }

    // ─── Dashboard ────────────────────────────────────────────────────────

    /** Affiche le tableau de bord du chercheur avec ses statistiques. */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        User user = getCurrentUser();
        Researcher researcher = researcherService.findByUser(user);

        if (researcher == null) {
            researcher = new Researcher();
            researcher.setUser(user);
            researcher = researcherService.save(researcher);
        }

        List<Publication> publications = publicationRepository.findByAuthorId(researcher.getId());

        model.addAttribute("theme", "researcher");
        model.addAttribute("activePage", "researcher-dashboard");
        model.addAttribute("researcher", researcher);
        model.addAttribute("publications", publications);
        model.addAttribute("totalPublications", publications.size());
        model.addAttribute("publicationsPubliees",
                publications.stream().filter(p -> p.getStatus() == PublicationStatus.PUBLIEE).count());
        model.addAttribute("publicationsEnCours",
                publications.stream().filter(p -> p.getStatus() != PublicationStatus.PUBLIEE
                        && p.getStatus() != PublicationStatus.BROUILLON).count());
        model.addAttribute("publicationsBrouillons",
                publications.stream().filter(p -> p.getStatus() == PublicationStatus.BROUILLON).count());

        return "researcher/dashboard";
    }

    // ─── Profil ──────────────────────────────────────────────────────────

    /** Affiche le profil du chercheur connecté. */
    @GetMapping("/profile")
    public String viewProfile(Model model) {
        User user = getCurrentUser();
        Researcher researcher = researcherService.findByUser(user);
        model.addAttribute("theme", "researcher");
        model.addAttribute("activePage", "researcher-profile");
        model.addAttribute("researcher", researcher);
        return "researcher/profile";
    }

    /** Affiche le formulaire de modification du profil chercheur. */
    @GetMapping("/profile/edit")
    public String editProfile(Model model) {
        User user = getCurrentUser();
        Researcher researcher = researcherService.findByUser(user);
        if (researcher == null) {
            researcher = new Researcher();
            researcher.setUser(user);
        }
        model.addAttribute("theme", "researcher");
        model.addAttribute("activePage", "researcher-profile");
        model.addAttribute("researcher", researcher);
        return "researcher/profile-edit";
    }

    /** Enregistre les modifications du profil chercheur (photo incluse). */
    @PostMapping("/profile/save")
    public String saveProfile(
            @ModelAttribute Researcher researcher,
            @RequestParam(value = "photoFile", required = false) MultipartFile photoFile,
            RedirectAttributes redirectAttrs) throws IOException {

        // Recharger le researcher existant pour ne pas ecraser la photo si pas de nouveau fichier
        Researcher existing = researcherService.findById(researcher.getId());
        if (existing != null && researcher.getPhotoUrl() == null) {
            researcher.setPhotoUrl(existing.getPhotoUrl());
        }

        researcher.setUser(getCurrentUser());

        if (photoFile != null && !photoFile.isEmpty()) {
            String fileName = UUID.randomUUID() + "-" + photoFile.getOriginalFilename();
            Path filePath = Paths.get(UPLOAD_DIR, fileName);
            Files.createDirectories(filePath.getParent());
            Files.copy(photoFile.getInputStream(), filePath);
            researcher.setPhotoUrl("/uploads/" + fileName);
        }

        researcherService.save(researcher);
        redirectAttrs.addFlashAttribute("successMessage", "Profil mis a jour avec succes.");
        return "redirect:/researcher/profile";
    }

    // ─── Publications ─────────────────────────────────────────────────────

    /** Liste les publications du chercheur connecté. */
    @GetMapping("/publications")
    public String listPublications(Model model) {
        User user = getCurrentUser();
        Researcher researcher = researcherService.findByUser(user);

        List<Publication> publications = List.of();
        if (researcher != null) {
            publications = publicationRepository.findByAuthorId(researcher.getId());
        }
        model.addAttribute("theme", "researcher");
        model.addAttribute("activePage", "researcher-publications");
        model.addAttribute("publications", publications);
        return "researcher/publications";
    }

    /** Affiche le formulaire de création d'une nouvelle publication. */
    @GetMapping("/publications/new")
    public String newPublication(Model model) {
        model.addAttribute("theme", "researcher");
        model.addAttribute("activePage", "researcher-submit");
        model.addAttribute("publication", new Publication());
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("publicationTypes", PublicationType.values());
        model.addAttribute("isEdit", false);
        return "researcher/publication-form";
    }

    /** Affiche le formulaire de modification d'une publication existante (brouillon ou rejetée uniquement). */
    @GetMapping("/publications/{id}/edit")
    public String editPublication(@PathVariable Long id, Model model, RedirectAttributes redirectAttrs) {
        User user = getCurrentUser();
        Researcher researcher = researcherService.findByUser(user);
        Publication publication = publicationRepository.findById(id).orElse(null);

        if (publication == null) {
            redirectAttrs.addFlashAttribute("errorMessage", "Publication introuvable.");
            return "redirect:/researcher/publications";
        }

        // Seul l'auteur peut modifier, et seulement si brouillon ou rejetee
        if (researcher == null || publication.getAuthors() == null
                || !publication.getAuthors().stream().anyMatch(a -> a.getId().equals(researcher.getId()))) {
            redirectAttrs.addFlashAttribute("errorMessage", "Vous n'etes pas l'auteur de cette publication.");
            return "redirect:/researcher/publications";
        }

        if (publication.getStatus() != PublicationStatus.BROUILLON && publication.getStatus() != PublicationStatus.REJETEE) {
            redirectAttrs.addFlashAttribute("errorMessage", "Seuls les brouillons et les publications rejetees peuvent etre modifies.");
            return "redirect:/researcher/publications";
        }

        model.addAttribute("publication", publication);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("publicationTypes", PublicationType.values());
        model.addAttribute("isEdit", true);
        model.addAttribute("theme", "researcher");
        model.addAttribute("activePage", "researcher-publications");

        // Reconstituer les mots-cles en chaine
        if (publication.getKeywords() != null) {
            String kwString = publication.getKeywords().stream()
                    .map(Keyword::getName)
                    .collect(Collectors.joining(", "));
            model.addAttribute("keywordsString", kwString);
        } else {
            model.addAttribute("keywordsString", "");
        }

        return "researcher/publication-form";
    }

    /** Enregistre une publication (création ou mise à jour) avec fichier PDF et mots-clés. */
    @PostMapping("/publications/save")
    public String savePublication(
            @ModelAttribute Publication publication,
            @RequestParam(value = "keywordsString", required = false, defaultValue = "") String keywordsString,
            @RequestParam(value = "pdfFile", required = false) MultipartFile pdfFile,
            RedirectAttributes redirectAttrs) throws IOException {

        User user = getCurrentUser();
        Researcher researcher = researcherService.findByUser(user);

        if (researcher == null) {
            return "redirect:/researcher/profile/edit";
        }

        boolean isEdit = publication.getId() != null;

        if (isEdit) {
            Publication existing = publicationRepository.findById(publication.getId()).orElse(null);
            if (existing != null) {
                // Conserver le PDF existant si pas de nouveau fichier
                if ((pdfFile == null || pdfFile.isEmpty()) && existing.getPdfFilePath() != null) {
                    publication.setPdfFilePath(existing.getPdfFilePath());
                }
                // Conserver le statut et les auteurs existants
                publication.setStatus(existing.getStatus());
                publication.setAuthors(existing.getAuthors());
            }
        }

        // Gestion du fichier PDF
        if (pdfFile != null && !pdfFile.isEmpty()) {
            String fileName = UUID.randomUUID() + "-" + pdfFile.getOriginalFilename();
            Path filePath = Paths.get(UPLOAD_DIR, fileName);
            Files.createDirectories(filePath.getParent());
            Files.copy(pdfFile.getInputStream(), filePath);
            publication.setPdfFilePath(filePath.toString());
        }

        // Gestion des mots-cles
        if (!keywordsString.isBlank()) {
            List<Keyword> keywords = Arrays.stream(keywordsString.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(name -> {
                        Keyword existing = keywordRepository.findByName(name);
                        return existing != null ? existing : keywordRepository.save(new Keyword(name));
                    })
                    .collect(Collectors.toList());
            publication.setKeywords(keywords);
        }

        // Gestion de la categorie
        if (publication.getCategory() != null && publication.getCategory().getId() != null) {
            categoryRepository.findById(publication.getCategory().getId())
                    .ifPresent(publication::setCategory);
        } else {
            publication.setCategory(null);
        }

        if (!isEdit) {
            // Nouvelle publication
            if (publication.getUniqueIdentifier() == null || publication.getUniqueIdentifier().isBlank()) {
                publication.setUniqueIdentifier("CRIS-" + System.currentTimeMillis());
            }
            publication.setStatus(PublicationStatus.BROUILLON);
            publication.setAuthors(List.of(researcher));
        }

        publicationRepository.save(publication);

        if (isEdit) {
            redirectAttrs.addFlashAttribute("successMessage", "Publication modifiee avec succes.");
        } else {
            redirectAttrs.addFlashAttribute("successMessage", "Publication creee. Vous pouvez la soumettre pour validation.");
        }

        return "redirect:/researcher/publications";
    }

    /** Supprime une publication en brouillon appartenant au chercheur. */
    @PostMapping("/publications/{id}/delete")
    public String deletePublication(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        User user = getCurrentUser();
        Researcher researcher = researcherService.findByUser(user);
        Publication pub = publicationRepository.findById(id).orElse(null);

        if (pub == null || researcher == null || pub.getAuthors() == null
                || !pub.getAuthors().stream().anyMatch(a -> a.getId().equals(researcher.getId()))) {
            redirectAttrs.addFlashAttribute("errorMessage", "Impossible de supprimer cette publication.");
            return "redirect:/researcher/publications";
        }

        if (pub.getStatus() != PublicationStatus.BROUILLON) {
            redirectAttrs.addFlashAttribute("errorMessage", "Seuls les brouillons peuvent etre supprimes.");
            return "redirect:/researcher/publications";
        }

        publicationRepository.deleteById(id);
        redirectAttrs.addFlashAttribute("successMessage", "Publication supprimee.");
        return "redirect:/researcher/publications";
    }

    // ─── Actions workflow ─────────────────────────────────────────────────

    /** Soumet une publication pour validation. */
    @PostMapping("/publications/{id}/soumettre")
    public String soumettre(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            workflowService.submit(id);
            redirectAttrs.addFlashAttribute("successMessage",
                    "Publication soumise pour validation. Vous serez notifie par email.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/researcher/publications";
    }

    /** Remet une publication en brouillon pour correction. */
    @PostMapping("/publications/{id}/remettre-en-brouillon")
    public String remettreEnBrouillon(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            workflowService.backToDraft(id);
            redirectAttrs.addFlashAttribute("successMessage", "Publication remise en brouillon. Corrigez et resoumettez.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/researcher/publications";
    }

    // ─── Exports chercheur ─────────────────────────────────────────────────

    /** Génère un rapport PDF des publications du chercheur. */
    @GetMapping("/report/pdf")
    public ResponseEntity<ByteArrayResource> exportPdf() {
        Researcher researcher = getCurrentResearcher();
        if (researcher == null) return ResponseEntity.notFound().build();

        List<Publication> publications = publicationRepository.findByAuthorId(researcher.getId());
        String name = (researcher.getFirstName() != null ? researcher.getFirstName() : "") + " " +
                      (researcher.getLastName() != null ? researcher.getLastName() : "");
        byte[] pdfBytes = pdfGeneratorService.generateResearcherReport(name.trim(), publications);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=rapport_chercheur.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfBytes.length)
                .body(new ByteArrayResource(pdfBytes));
    }

    /** Génère un rapport Excel des publications du chercheur. */
    @GetMapping("/report/excel")
    public ResponseEntity<ByteArrayResource> exportExcel() throws java.io.IOException {
        Researcher researcher = getCurrentResearcher();
        if (researcher == null) return ResponseEntity.notFound().build();

        List<Publication> publications = publicationRepository.findByAuthorId(researcher.getId());
        byte[] excelBytes = excelGeneratorService.generateResearcherExcel(publications);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=rapport_chercheur.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(excelBytes.length)
                .body(new ByteArrayResource(excelBytes));
    }

    // ─── Statistiques individuelles ──────────────────────────────────────

    /** Affiche les statistiques individuelles du chercheur (par statut, type, année). */
    @GetMapping("/statistics")
    public String statistics(Model model) {
        User user = getCurrentUser();
        Researcher researcher = researcherService.findByUser(user);

        model.addAttribute("theme", "researcher");
        model.addAttribute("activePage", "researcher-statistics");

        if (researcher == null) {
            model.addAttribute("researcher", researcher);
            return "researcher/statistics";
        }

        List<Publication> publications = publicationRepository.findByAuthorId(researcher.getId());

        // Par statut
        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (PublicationStatus s : PublicationStatus.values()) {
            long count = publications.stream().filter(p -> p.getStatus() == s).count();
            if (count > 0) byStatus.put(s.name(), count);
        }

        // Par type
        Map<String, Long> byType = new LinkedHashMap<>();
        for (PublicationType t : PublicationType.values()) {
            long count = publications.stream().filter(p -> p.getType() == t).count();
            if (count > 0) byType.put(t.name(), count);
        }

        // Par annee
        Map<Integer, Long> byYear = new LinkedHashMap<>();
        publications.stream()
                .filter(p -> p.getPublicationYear() != null)
                .collect(Collectors.groupingBy(Publication::getPublicationYear, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<Integer, Long>comparingByKey().reversed())
                .forEach(e -> byYear.put(e.getKey(), e.getValue()));

        // Publications publiees seulement (pour le taux d'acceptation)
        long publiees = publications.stream().filter(p -> p.getStatus() == PublicationStatus.PUBLIEE).count();
        long soumises = publications.stream()
                .filter(p -> p.getStatus() != PublicationStatus.BROUILLON).count();
        double tauxAcceptation = soumises > 0 ? (publiees * 100.0 / soumises) : 0;

        model.addAttribute("researcher", researcher);
        model.addAttribute("totalPublications", publications.size());
        model.addAttribute("byStatus", byStatus);
        model.addAttribute("byType", byType);
        model.addAttribute("byYear", byYear);
        model.addAttribute("maxYearCount", byYear.values().stream().mapToLong(Long::longValue).max().orElse(1L));
        model.addAttribute("tauxAcceptation", Math.round(tauxAcceptation * 10.0) / 10.0);
        model.addAttribute("publicationsPubliees", publiees);
        model.addAttribute("publicationsEnCours",
                publications.stream().filter(p -> p.getStatus() != PublicationStatus.PUBLIEE
                        && p.getStatus() != PublicationStatus.BROUILLON).count());
        model.addAttribute("publicationsBrouillons",
                publications.stream().filter(p -> p.getStatus() == PublicationStatus.BROUILLON).count());

        return "researcher/statistics";
    }
}