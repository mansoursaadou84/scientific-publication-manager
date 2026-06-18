package com.centre.recherche.services;

import com.centre.recherche.models.Publication;
import com.centre.recherche.models.PublicationStatus;
import com.centre.recherche.repositories.PublicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service de statistiques sur les publications.
 * <p>
 * Fournit des comptages regroupes par annee, categorie, type, chercheur et statut.
 * Les methodes prefixees {@code get} concernent uniquement les publications publiees (PUBLIEE),
 * tandis que les methodes prefixees {@code getAll} couvrent tous les statuts.
 * </p>
 *
 * @see Publication
 * @see PublicationRepository
 */
@Service
public class StatisticsService {

    @Autowired
    private PublicationRepository publicationRepository;

    // ── Stats publiques (PUBLIEE uniquement) ──────────────────────────────

    /** Retourne le nombre de publications publiees par annee, trie par annee decroissante. */
    public Map<Integer, Long> getPublicationsCountByYear() {
        return filterPublished().stream()
                .filter(p -> p.getPublicationYear() != null)
                .collect(Collectors.groupingBy(Publication::getPublicationYear, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<Integer, Long>comparingByKey().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Long::sum, LinkedHashMap::new));
    }

    /** Retourne le nombre de publications publiees par categorie, trie par nombre decroissant. */
    public Map<String, Long> getPublicationsCountByCategory() {
        return filterPublished().stream()
                .filter(p -> p.getCategory() != null && p.getCategory().getName() != null)
                .collect(Collectors.groupingBy(p -> p.getCategory().getName(), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Long::sum, LinkedHashMap::new));
    }

    /** Retourne le nombre de publications publiees par chercheur, trie par nombre decroissant. */
    public Map<String, Long> getPublicationsCountByResearcher() {
        return filterPublished().stream()
                .filter(p -> p.getAuthors() != null)
                .flatMap(p -> p.getAuthors().stream())
                .filter(r -> r.getFirstName() != null || r.getLastName() != null || r.getUser() != null)
                .collect(Collectors.groupingBy(
                        r -> {
                            String name = ((r.getFirstName() != null ? r.getFirstName() : "") + " " +
                                    (r.getLastName() != null ? r.getLastName() : "")).trim();
                            return name.isEmpty() && r.getUser() != null ? r.getUser().getUsername() : name;
                        },
                        Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Long::sum, LinkedHashMap::new));
    }

    /** Retourne le nombre de publications publiees par type, trie par nombre decroissant. */
    public Map<String, Long> getPublicationsCountByType() {
        return filterPublished().stream()
                .filter(p -> p.getType() != null)
                .collect(Collectors.groupingBy(p -> p.getType().name(), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Long::sum, LinkedHashMap::new));
    }

    // ── Stats admin (toutes publications) ─────────────────────────────────

    /** Retourne le nombre de publications par statut (tous statuts confondus), trie par nombre decroissant. */
    public Map<String, Long> getPublicationsCountByStatus() {
        return filterAll().stream()
                .filter(p -> p.getStatus() != null)
                .collect(Collectors.groupingBy(p -> p.getStatus().name(), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Long::sum, LinkedHashMap::new));
    }

    // ── Stats admin (toutes publications, pas seulement PUBLIEE) ──────────

    /** Retourne le nombre de toutes les publications par annee, trie par annee decroissante. */
    public Map<Integer, Long> getAllCountByYear() {
        return filterAll().stream()
                .filter(p -> p.getPublicationYear() != null)
                .collect(Collectors.groupingBy(Publication::getPublicationYear, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<Integer, Long>comparingByKey().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Long::sum, LinkedHashMap::new));
    }

    /** Retourne le nombre de toutes les publications par categorie, trie par nombre decroissant. */
    public Map<String, Long> getAllCountByCategory() {
        return filterAll().stream()
                .filter(p -> p.getCategory() != null && p.getCategory().getName() != null)
                .collect(Collectors.groupingBy(p -> p.getCategory().getName(), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Long::sum, LinkedHashMap::new));
    }

    /** Retourne le nombre de toutes les publications par type, trie par nombre decroissant. */
    public Map<String, Long> getAllCountByType() {
        return filterAll().stream()
                .filter(p -> p.getType() != null)
                .collect(Collectors.groupingBy(p -> p.getType().name(), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Long::sum, LinkedHashMap::new));
    }

    /** Retourne le nombre de toutes les publications par chercheur, trie par nombre decroissant. */
    public Map<String, Long> getAllCountByResearcher() {
        return filterAll().stream()
                .filter(p -> p.getAuthors() != null)
                .flatMap(p -> p.getAuthors().stream())
                .filter(r -> r.getFirstName() != null || r.getLastName() != null || r.getUser() != null)
                .collect(Collectors.groupingBy(
                        r -> {
                            String name = ((r.getFirstName() != null ? r.getFirstName() : "") + " " +
                                    (r.getLastName() != null ? r.getLastName() : "")).trim();
                            return name.isEmpty() && r.getUser() != null ? r.getUser().getUsername() : name;
                        },
                        Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Long::sum, LinkedHashMap::new));
    }

    /** Retourne le nombre de publications pour un statut donne. */
    public long countByStatus(PublicationStatus status) {
        return filterAll().stream().filter(p -> p.getStatus() == status).count();
    }

    /** Retourne le nombre total de publications publiees (PUBLIEE). */
    public long getTotalPublished() {
        return filterPublished().size();
    }

    /** Retourne le nombre total de publications tous statuts confondus. */
    public long getTotalAll() {
        return filterAll().size();
    }

    // ── Donnees pour les rapports ─────────────────────────────────────────

    /** Retourne les publications publiees triees par annee decroissante puis par titre, pour les rapports. */
    public List<Publication> getPublishedForReport() {
        return filterPublished().stream()
                .sorted((a, b) -> {
                    int yearCompare = Integer.compare(
                            b.getPublicationYear() != null ? b.getPublicationYear() : 0,
                            a.getPublicationYear() != null ? a.getPublicationYear() : 0);
                    if (yearCompare != 0) return yearCompare;
                    return a.getTitle() != null && b.getTitle() != null
                            ? a.getTitle().compareToIgnoreCase(b.getTitle()) : 0;
                })
                .collect(Collectors.toList());
    }

    /** Retourne toutes les publications triees par annee decroissante puis par titre, pour les rapports admin. */
    public List<Publication> getAllForReport() {
        return filterAll().stream()
                .sorted((a, b) -> {
                    int yearCompare = Integer.compare(
                            b.getPublicationYear() != null ? b.getPublicationYear() : 0,
                            a.getPublicationYear() != null ? a.getPublicationYear() : 0);
                    if (yearCompare != 0) return yearCompare;
                    return a.getTitle() != null && b.getTitle() != null
                            ? a.getTitle().compareToIgnoreCase(b.getTitle()) : 0;
                })
                .collect(Collectors.toList());
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private List<Publication> filterPublished() {
        return publicationRepository.findByStatus(PublicationStatus.PUBLIEE);
    }

    private List<Publication> filterAll() {
        return publicationRepository.findAll();
    }
}