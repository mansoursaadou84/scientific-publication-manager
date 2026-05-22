package com.centre.recherche.controllers;

import com.centre.recherche.models.Category;
import com.centre.recherche.models.Keyword;
import com.centre.recherche.models.Publication;
import com.centre.recherche.models.PublicationStatus;
import com.centre.recherche.models.User;
import com.centre.recherche.repositories.PublicationRepository;
import com.centre.recherche.services.CategoryService;
import com.centre.recherche.services.KeywordService;
import com.centre.recherche.services.PublicationService;
import com.centre.recherche.services.UserService;
import com.centre.recherche.services.WorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Contrôleur de l'espace administrateur (préfixe : /admin).
 * Gère le tableau de bord, le CRUD des utilisateurs, catégories et mots-clés,
 * ainsi que les actions du workflow de validation des publications.
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired private UserService userService;
    @Autowired private CategoryService categoryService;
    @Autowired private KeywordService keywordService;
    @Autowired private PublicationService publicationService;
    @Autowired private PublicationRepository publicationRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private WorkflowService workflowService;

    // ── Dashboard ──────────────────────────────────────
    /** Affiche le tableau de bord administrateur avec les statistiques globales. */
    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        List<Publication> publications = publicationService.getAllPublications();
        long soumises = publications.stream().filter(p -> p.getStatus() == PublicationStatus.SOUMISE).count();
        long enValidation = publications.stream().filter(p -> p.getStatus() == PublicationStatus.EN_VALIDATION).count();
        long approuvees = publications.stream().filter(p -> p.getStatus() == PublicationStatus.APPROUVEE).count();
        model.addAttribute("theme", "admin");
        model.addAttribute("activePage", "admin-dashboard");
        model.addAttribute("totalPublications", publications.size());
        model.addAttribute("totalUsers", userService.findAll().size());
        model.addAttribute("totalCategories", categoryService.findAll().size());
        model.addAttribute("totalKeywords", keywordService.findAll().size());
        model.addAttribute("pendingPublications", soumises + enValidation + approuvees);
        model.addAttribute("soumises", soumises);
        model.addAttribute("enValidation", enValidation);
        model.addAttribute("approuvees", approuvees);
        return "admin/dashboard";
    }

    // ── Gestion Publications ───────────────────────────
    /** Affiche la liste de toutes les publications pour gestion. */
    @GetMapping("/publications")
    public String managePublications(Model model) {
        model.addAttribute("theme", "admin");
        model.addAttribute("activePage", "admin-publications");
        model.addAttribute("publications", publicationService.getAllPublications());
        model.addAttribute("workflowService", workflowService);
        return "admin/publications";
    }

    // ── Workflow actions (POST) ────────────────────────

    /** Soumet une publication pour validation. */
    @PostMapping("/publications/{id}/soumettre")
    public String soumettre(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            workflowService.submit(id);
            redirectAttrs.addFlashAttribute("successMessage", "Publication soumise pour validation.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/publications";
    }

    /** Démarre la validation d'une publication. */
    @PostMapping("/publications/{id}/valider")
    public String startValidation(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            workflowService.startValidation(id);
            redirectAttrs.addFlashAttribute("successMessage", "Publication mise en validation.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/publications";
    }

    /** Approuve une publication. */
    @PostMapping("/publications/{id}/approuver")
    public String approve(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            workflowService.approve(id);
            redirectAttrs.addFlashAttribute("successMessage", "Publication approuvee.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/publications";
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
        return "redirect:/admin/publications";
    }

    /** Publie une publication dans le catalogue. */
    @PostMapping("/publications/{id}/publier")
    public String publish(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            workflowService.publish(id);
            redirectAttrs.addFlashAttribute("successMessage", "Publication publiee dans le catalogue.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/publications";
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
        return "redirect:/admin/publications";
    }

    // ── Gestion Utilisateurs ───────────────────────────
    /** Affiche la liste des utilisateurs. */
    @GetMapping("/users")
    public String manageUsers(Model model) {
        model.addAttribute("theme", "admin");
        model.addAttribute("activePage", "admin-users");
        model.addAttribute("users", userService.findAll());
        return "admin/users";
    }

    /** Affiche le formulaire de création d'un nouvel utilisateur. */
    @GetMapping("/users/new")
    public String newUser(Model model) {
        model.addAttribute("theme", "admin");
        model.addAttribute("activePage", "admin-users");
        model.addAttribute("user", new User());
        return "admin/user-form";
    }

    /** Affiche le formulaire de modification d'un utilisateur existant. */
    @GetMapping("/users/edit/{id}")
    public String editUser(@PathVariable Long id, Model model) {
        model.addAttribute("theme", "admin");
        model.addAttribute("activePage", "admin-users");
        model.addAttribute("user", userService.findById(id));
        return "admin/user-form";
    }

    /** Enregistre un utilisateur (création ou mise à jour). */
    @PostMapping("/users/save")
    public String saveUser(@ModelAttribute User user,
                           @RequestParam(value = "rawPassword", required = false) String rawPassword) {
        if (rawPassword != null && !rawPassword.trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(rawPassword));
        } else if (user.getId() != null) {
            User existing = userService.findById(user.getId());
            if (existing != null) user.setPassword(existing.getPassword());
        } else {
            user.setPassword(passwordEncoder.encode("1234"));
        }
        userService.save(user);
        return "redirect:/admin/users";
    }

    /** Supprime un utilisateur. */
    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteById(id);
        return "redirect:/admin/users";
    }

    // ── Gestion Categories ─────────────────────────────
    /** Affiche la liste des catégories. */
    @GetMapping("/categories")
    public String manageCategories(Model model) {
        model.addAttribute("theme", "admin");
        model.addAttribute("activePage", "admin-categories");
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("newCategory", new Category());
        return "admin/categories";
    }

    /** Enregistre une catégorie (création ou mise à jour). */
    @PostMapping("/categories/save")
    public String saveCategory(@ModelAttribute Category category) {
        categoryService.save(category);
        return "redirect:/admin/categories";
    }

    /** Supprime une catégorie. */
    @GetMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable Long id) {
        categoryService.deleteById(id);
        return "redirect:/admin/categories";
    }

    // ── Gestion Mots-cles ──────────────────────────────
    /** Affiche la liste des mots-clés. */
    @GetMapping("/keywords")
    public String manageKeywords(Model model) {
        model.addAttribute("theme", "admin");
        model.addAttribute("activePage", "admin-keywords");
        model.addAttribute("keywords", keywordService.findAll());
        model.addAttribute("newKeyword", new Keyword());
        return "admin/keywords";
    }

    /** Enregistre un mot-clé (création ou mise à jour). */
    @PostMapping("/keywords/save")
    public String saveKeyword(@ModelAttribute Keyword keyword) {
        keywordService.save(keyword);
        return "redirect:/admin/keywords";
    }

    /** Supprime un mot-clé. */
    @GetMapping("/keywords/delete/{id}")
    public String deleteKeyword(@PathVariable Long id) {
        keywordService.deleteById(id);
        return "redirect:/admin/keywords";
    }
}