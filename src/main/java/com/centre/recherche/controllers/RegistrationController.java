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

/**
 * Contrôleur d'inscription (préfixe : /register).
 * Gère l'affichage du formulaire d'inscription et la création d'un nouveau compte chercheur.
 */
@Controller
public class RegistrationController {

    @Autowired
    private UserRepository userRepository;  // ← direct, sans passer par UserService

    @Autowired
    private PasswordEncoder passwordEncoder;

    /** Affiche le formulaire d'inscription. */
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    /** Enregistre un nouvel utilisateur avec le rôle Chercheur. */
    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user) {
        // Encode UNE SEULE FOIS ici
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.CHERCHEUR);
        userRepository.save(user);  // ← sauvegarde directe sans re-encoder
        return "redirect:/login";
    }
}