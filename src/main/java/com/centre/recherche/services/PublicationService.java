package com.centre.recherche.services;

import com.centre.recherche.models.Keyword;
import com.centre.recherche.models.Publication;
import com.centre.recherche.models.PublicationStatus;
import com.centre.recherche.models.PublicationType;
import com.centre.recherche.models.Researcher;
import com.centre.recherche.repositories.CategoryRepository;
import com.centre.recherche.repositories.KeywordRepository;
import com.centre.recherche.repositories.PublicationRepository;
import com.centre.recherche.services.search.TFIDFService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service principal de gestion des publications.
 * <p>
 * Assure les operations de recherche (TF-IDF et JPQL), CRUD, comptage de telechargements
 * et acces au catalogue public (publications publiees uniquement) ainsi qu'aux donnees
 * d'administration (toutes les publications).
 * </p>
 * <p>
 * La recherche textuelle est deleguee a {@link TFIDFService} lorsque l'index est pret ;
 * sinon, un repli JPQL est utilise via le depot.
 * </p>
 *
 * @see TFIDFService
 * @see PublicationRepository
 */
@Service
public class PublicationService {

    @Autowired
    private PublicationRepository publicationRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private KeywordRepository keywordRepository;

    @Autowired
    private TFIDFService tfidfService;

    // ── Published-only methods (public catalog) ──

    /** Retourne toutes les publications dont le statut est PUBLIEE. */
    public List<Publication> getPublishedPublications() {
        return publicationRepository.findByStatus(PublicationStatus.PUBLIEE);
    }

    /** Retourne les publications publiees avec pagination. */
    public Page<Publication> getPublishedPublications(Pageable pageable) {
        return publicationRepository.findByStatus(PublicationStatus.PUBLIEE, pageable);
    }

    /**
     * Recherche dans le catalogue public :
     * - Si un query texte est present : utilise TF-IDF (tri par pertinence decroissante)
     * - Si aucun query : utilise JPQL avec filtres type/categorie/annee (tri par annee)
     */
    public Page<Publication> searchPublishedPublications(String query, PublicationType type,
                                                           Long categoryId, Integer year, Pageable pageable) {
        boolean hasTextQuery = query != null && !query.trim().isEmpty();

        if (hasTextQuery && tfidfService.isIndexReady()) {
            return searchByTFIDF(query.trim(), type, categoryId, year, pageable);
        } else {
            return publicationRepository.searchPublished(PublicationStatus.PUBLIEE, query, type, categoryId, year, pageable);
        }
    }

    private Page<Publication> searchByTFIDF(String query, PublicationType type, Long categoryId, Integer year, Pageable pageable) {
        List<TFIDFService.SearchResult> tfidfResults = tfidfService.search(query, type, categoryId, year);

        // Pagination des resultats TF-IDF
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), tfidfResults.size());

        if (start >= tfidfResults.size()) {
            return new PageImpl<>(List.of(), pageable, tfidfResults.size());
        }

        List<Long> pageIds = tfidfResults.subList(start, end).stream()
                .map(TFIDFService.SearchResult::getPublicationId)
                .collect(Collectors.toList());

        Map<Long, Double> scoreMap = tfidfResults.stream()
                .collect(Collectors.toMap(TFIDFService.SearchResult::getPublicationId, TFIDFService.SearchResult::getScore));

        // Charger les publications et les trier selon l'ordre TF-IDF
        List<Publication> pubs = publicationRepository.findAllById(pageIds).stream()
                .sorted(Comparator.comparingInt(p -> pageIds.indexOf(p.getId())))
                .peek(p -> p.setTfidfScore(scoreMap.getOrDefault(p.getId(), 0.0)))
                .collect(Collectors.toList());

        return new PageImpl<>(pubs, pageable, tfidfResults.size());
    }

    /** Retourne une publication publiee par son identifiant, ou null si introuvable ou non publiee. */
    public Publication getPublishedPublicationById(Long id) {
        return publicationRepository.findById(id)
                .filter(p -> p.getStatus() == PublicationStatus.PUBLIEE)
                .orElse(null);
    }

    /** Retourne le nombre total de publications publiees. */
    public long countPublishedPublications() {
        return publicationRepository.countByStatus(PublicationStatus.PUBLIEE);
    }

    /** Retourne les annees distinctes parmi les publications publiees. */
    public List<Integer> getDistinctPublishedYears() {
        return publicationRepository.findDistinctPublishedYears(PublicationStatus.PUBLIEE);
    }

    // ── All-publications methods (admin/researcher use) ──

    /** Sauvegarde une publication en gerant les mots-cles, la categorie et l'identifiant unique. Reindexe TF-IDF si publiee. */
    public Publication savePublication(Publication publication, String keywordsString, Researcher researcher) {
        publication.setAuthors(List.of(researcher));

        if (keywordsString != null && !keywordsString.trim().isEmpty()) {
            List<Keyword> keywords = Arrays.stream(keywordsString.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(name -> {
                        Keyword existing = keywordRepository.findByName(name);
                        return existing != null ? existing : keywordRepository.save(new Keyword(name));
                    })
                    .collect(Collectors.toList());
            publication.setKeywords(keywords);
        }

        if (publication.getCategory() != null && publication.getCategory().getId() != null) {
            categoryRepository.findById(publication.getCategory().getId())
                    .ifPresent(publication::setCategory);
        }

        if (publication.getUniqueIdentifier() == null || publication.getUniqueIdentifier().isEmpty()) {
            publication.setUniqueIdentifier("CRIS-" + System.currentTimeMillis());
        }

        Publication saved = publicationRepository.save(publication);

        // Reconstruire l'index TF-IDF si la publication est publiee
        if (saved.getStatus() == PublicationStatus.PUBLIEE) {
            tfidfService.rebuildIndex();
        }

        return saved;
    }

    /** Retourne toutes les publications, tous statuts confondus. */
    public List<Publication> getAllPublications() {
        return publicationRepository.findAll();
    }

    /** Retourne une publication par son identifiant, ou null si introuvable. */
    public Publication getPublicationById(Long id) {
        return publicationRepository.findById(id).orElse(null);
    }

    /** Supprime une publication par son identifiant et reconstruit l'index TF-IDF. */
    public void deletePublication(Long id) {
        publicationRepository.deleteById(id);
        tfidfService.rebuildIndex();
    }

    /** Incremente le compteur de telechargements d'une publication. */
    @Transactional
    public void incrementDownloadCount(Long id) {
        publicationRepository.incrementDownloadCount(id);
    }
}