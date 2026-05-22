package com.centre.recherche.controllers;

import com.centre.recherche.models.Publication;
import com.centre.recherche.models.PublicationStatus;
import com.centre.recherche.repositories.PublicationRepository;
import com.centre.recherche.services.StatisticsService;
import com.centre.recherche.services.WorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Contrôleur de l'espace documentaliste (préfixe : /documentaliste).
 * Gère le tableau de bord et le workflow de validation des publications
 * (mise en validation, approbation, rejet, publication).
 */
@Controller
@RequestMapping("/documentaliste")
public class DocumentalisteController {

    @Autowired
    private PublicationRepository publicationRepository;

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private StatisticsService statisticsService;

    /** Redirige vers le tableau de bord documentaliste. */
    @GetMapping
    public String root() {
        return "redirect:/documentaliste/dashboard";
    }

    /** Affiche le tableau de bord documentaliste avec les publications en attente. */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("theme", "documentaliste");
        model.addAttribute("activePage", "doc-dashboard");
        List<Publication> soumises = publicationRepository.findByStatus(PublicationStatus.SOUMISE);
        List<Publication> enValidation = publicationRepository.findByStatus(PublicationStatus.EN_VALIDATION);
        List<Publication> approuvees = publicationRepository.findByStatus(PublicationStatus.APPROUVEE);

        model.addAttribute("soumises", soumises);
        model.addAttribute("enValidation", enValidation);
        model.addAttribute("approuvees", approuvees);
        model.addAttribute("totalEnAttente", soumises.size() + enValidation.size() + approuvees.size());
        return "documentaliste/dashboard";
    }

    /** Liste les publications en attente de traitement. */
    @GetMapping("/publications")
    public String listPublications(Model model) {
        model.addAttribute("theme", "documentaliste");
        model.addAttribute("activePage", "doc-publications");
        List<Publication> pending = publicationRepository.findAll().stream()
                .filter(p -> p.getStatus() == PublicationStatus.SOUMISE
                        || p.getStatus() == PublicationStatus.EN_VALIDATION
                        || p.getStatus() == PublicationStatus.APPROUVEE)
                .collect(Collectors.toList());
        model.addAttribute("publications", pending);
        model.addAttribute("workflowService", workflowService);
        return "documentaliste/publications";
    }

    /** Démarre la validation d'une publication soumise. */
    @PostMapping("/publications/{id}/valider")
    public String startValidation(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            workflowService.startValidation(id);
            redirectAttrs.addFlashAttribute("successMessage", "Publication mise en validation.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/documentaliste/publications";
    }

    /** Approuve une publication validée. */
    @PostMapping("/publications/{id}/approuver")
    public String approve(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            workflowService.approve(id);
            redirectAttrs.addFlashAttribute("successMessage", "Publication approuvee.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/documentaliste/publications";
    }

    /** Rejette une publication avec un commentaire optionnel. */
    @PostMapping("/publications/{id}/rejeter")
    public String reject(@PathVariable Long id,
                        @RequestParam(value = "comment", required = false, defaultValue = "") String comment,
                        RedirectAttributes redirectAttrs) {
        try {
            workflowService.reject(id, comment);
            redirectAttrs.addFlashAttribute("successMessage", "Publication rejetee.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/documentaliste/publications";
    }

    /** Publie une publication approuvée dans le catalogue. */
    @PostMapping("/publications/{id}/publier")
    public String publish(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            workflowService.publish(id);
            redirectAttrs.addFlashAttribute("successMessage", "Publication publiee dans le catalogue.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/documentaliste/publications";
    }

    /** Remet une publication en brouillon. */
    @PostMapping("/publications/{id}/brouillon")
    public String backToDraft(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            workflowService.backToDraft(id);
            redirectAttrs.addFlashAttribute("successMessage", "Publication remise en brouillon.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/documentaliste/publications";
    }
}