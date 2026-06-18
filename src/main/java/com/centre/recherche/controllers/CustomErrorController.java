package com.centre.recherche.controllers;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controleur personnalise pour les pages d'erreur (403, 404, 500).
 * Affiche des pages d'erreur professionnelles au lieu des pages par defaut de Spring Boot.
 */
@Controller
public class CustomErrorController implements ErrorController {

    @GetMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object statusObj = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int status = 500;
        if (statusObj != null) {
            try {
                status = Integer.parseInt(statusObj.toString());
            } catch (NumberFormatException ignored) {}
        }

        String errorMessage;
        switch (status) {
            case 403:
                errorMessage = "Vous ne disposez pas des droits necessaires pour acceder a cette page. "
                        + "Cette section est reservee aux administrateurs du centre de recherche.";
                model.addAttribute("errorMessage", errorMessage);
                return "error/403";
            case 404:
                return "error/404";
            default:
                errorMessage = "Une erreur inattendue s'est produite sur le serveur. "
                        + "Veuillez reessayer plus tard.";
                model.addAttribute("errorMessage", errorMessage);
                return "error/500";
        }
    }
}