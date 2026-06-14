package com.centre.recherche.controllers;

import com.centre.recherche.models.Role;
import com.centre.recherche.models.User;
import com.centre.recherche.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Contrôleur d'inscription (préfixe : /register).
 * Gère l'affichage du formulaire d'inscription et la création d'un nouveau compte chercheur.
 */
@Controller
public class RegistrationController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /** Affiche le formulaire d'inscription. */
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    /**
     * Enregistre un nouvel utilisateur avec le rôle Chercheur.
     * Utilise des @RequestParam au lieu de @ModelAttribute pour éviter
     * le binding de champs sensibles (id, role) depuis le formulaire.
     */
    @PostMapping("/register")
    public String registerUser(@RequestParam String username,
                               @RequestParam String password,
                               @RequestParam(value = "email", required = false) String email) {
        if (userRepository.findByUsername(username) != null) {
            return "redirect:/register?error=exists";
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Role.CHERCHEUR);
        user.setEmail(email);
        userRepository.save(user);
        return "redirect:/login";
    }
}