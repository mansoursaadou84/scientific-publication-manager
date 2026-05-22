package com.centre.recherche.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration Spring MVC de l'application.
 * Mappe le chemin URL /uploads/** au repertoire physique ./uploads/
 * pour le stockage des fichiers PDF telecharges.
 */
@Configuration
public class MvcConfig implements WebMvcConfigurer {

    /**
     * Enregistre un gestionnaire de ressources statiques pour servir
     * les fichiers telecharges depuis le repertoire ./uploads/.
     *
     * @param registry le registre des gestionnaires de ressources
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./uploads/");
    }
}
