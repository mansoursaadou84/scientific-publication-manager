package com.centre.recherche.models;

/**
 * Enumeration des roles utilisateurs disponibles dans le systeme.
 *
 * <ul>
 *   <li>{@code CHERCHEUR} -- Soumet et gere ses propres publications.</li>
 *   <li>{@code ADMIN} -- Acces complet : gestion des utilisateurs, categories, mots-cles et workflow.</li>
 *   <li>{@code DOCUMENTALISTE} -- Valide et gere le workflow de publication.</li>
 *   <li>{@code VISITEUR} -- Acces au catalogue public uniquement.</li>
 * </ul>
 */
public enum Role {
    CHERCHEUR,
    ADMIN,
    DOCUMENTALISTE,
    VISITEUR
}