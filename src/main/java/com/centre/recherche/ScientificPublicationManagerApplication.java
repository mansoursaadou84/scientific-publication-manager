package com.centre.recherche;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Point d'entree de l'application Scientific Publication Manager.
 * Application Spring Boot 3.2 avec Java 17 permettant la gestion
 * des publications scientifiques, des chercheurs et des categories.
 * L'annotation {@link EnableAsync} active le support des taches asynchrones.
 */
@SpringBootApplication
@EnableAsync
public class ScientificPublicationManagerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ScientificPublicationManagerApplication.class, args);
    }
}
