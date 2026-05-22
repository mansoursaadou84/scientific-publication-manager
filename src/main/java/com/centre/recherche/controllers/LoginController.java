package com.centre.recherche.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Contrôleur de la page de connexion (préfixe : /login).
 * Affiche le formulaire d'authentification.
 */
@Controller
public class LoginController {
    /** Affiche le formulaire de connexion. */
    @GetMapping("/login")
    public String login() { return "login"; }
}

