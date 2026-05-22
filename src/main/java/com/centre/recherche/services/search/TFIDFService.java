package com.centre.recherche.services.search;

import com.centre.recherche.models.Keyword;
import com.centre.recherche.models.Publication;
import com.centre.recherche.models.PublicationStatus;
import com.centre.recherche.models.PublicationType;
import com.centre.recherche.repositories.PublicationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service de recherche textuelle basee sur l'algorithme TF-IDF (Term Frequency - Inverse Document Frequency).
 * <p>
 * Construit et maintient un index vectoriel des publications publiees afin de
 * permettre des recherches par similarite cosinus. L'index est reconstruit
 * automatiquement au demarrage de l'application et a chaque modification
 * d'une publication publiee.
 * </p>
 * <p>
 * La tokenisation elimine les mots vides (stop words) en francais et en anglais,
 * et les termes de moins de 3 caracteres. Les titres et mots-cles sont ponderees
 * pour ameliorer la pertinence des resultats.
 * </p>
 *
 * @see com.centre.recherche.services.PublicationService
 */
@Service
public class TFIDFService {

    private static final Logger log = LoggerFactory.getLogger(TFIDFService.class);
    private static final Set<String> STOP_WORDS = Set.of(
            "the", "and", "for", "are", "but", "not", "you", "all", "can", "had",
            "her", "was", "one", "our", "out", "has", "have", "from", "this",
            "that", "with", "they", "will", "what", "when", "who", "how",
            "each", "she", "more", "than", "been", "into", "them", "then",
            "les", "des", "une", "que", "qui", "est", "dans", "pour", "sur",
            "par", "avec", "son", "ses", "aux", "ont", "mais", "comme", "tout",
            "also", "new", "just", "very", "often", "however", "too", "any",
            "may", "these", "using", "based", "which", "their", "other",
            "de", "la", "le", "du", "en", "et", "un", "ce", "se", "ne",
            "pas", "ou", "au", "it", "its", "an", "as", "at", "be", "by",
            "if", "is", "no", "of", "on", "or", "so", "up", "we");

    @Autowired
    private PublicationRepository publicationRepository;

    // Index structures
    private final Map<Long, Map<String, Double>> documentVectors = new ConcurrentHashMap<>();
    private final Map<String, Double> idfCache = new ConcurrentHashMap<>();
    private final Map<Long, Double> documentNorms = new ConcurrentHashMap<>();
    private volatile int totalDocuments = 0;
    private volatile boolean indexReady = false;

    /**
     * Reconstruit l'index TF-IDF a partir de toutes les publications publiees.
     * Appele automatiquement au demarrage et apres chaque modification d'une publication publiee.
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional(readOnly = true)
    public synchronized void rebuildIndex() {
        long start = System.currentTimeMillis();
        documentVectors.clear();
        idfCache.clear();
        documentNorms.clear();

        List<Publication> published = publicationRepository.findByStatus(PublicationStatus.PUBLIEE);
        // Forcer le chargement des collections lazy dans la session
        for (Publication pub : published) {
            if (pub.getKeywords() != null) pub.getKeywords().size();
            if (pub.getAuthors() != null) pub.getAuthors().size();
            if (pub.getCategory() != null) pub.getCategory().getName();
        }
        totalDocuments = published.size();

        if (totalDocuments == 0) {
            indexReady = true;
            log.info("TF-IDF index built: 0 documents, {}ms", System.currentTimeMillis() - start);
            return;
        }

        // Etape 1 : calculer la frequence documentaire (DF) pour chaque terme
        Map<String, Integer> docFrequency = new HashMap<>();
        for (Publication pub : published) {
            Set<String> uniqueTerms = new HashSet<>(tokenize(extractDocumentText(pub)));
            for (String term : uniqueTerms) {
                docFrequency.merge(term, 1, Integer::sum);
            }
        }

        // Etape 2 : pre-calculer l'IDF pour chaque terme
        for (Map.Entry<String, Integer> entry : docFrequency.entrySet()) {
            double idf = Math.log((double) totalDocuments / (1.0 + entry.getValue())) + 1.0;
            idfCache.put(entry.getKey(), idf);
        }

        // Etape 3 : construire les vecteurs TF-IDF de chaque document
        for (Publication pub : published) {
            Map<String, Double> vector = buildDocumentVector(pub);
            documentVectors.put(pub.getId(), vector);
            documentNorms.put(pub.getId(), computeNorm(vector));
        }

        indexReady = true;
        long elapsed = System.currentTimeMillis() - start;
        log.info("TF-IDF index built: {} documents, {} terms, {}ms",
                totalDocuments, idfCache.size(), elapsed);
    }

    /**
     * Recherche TF-IDF : retourne les publications triees par score de pertinence decroissant.
     * Combine la similarite cosinus TF-IDF avec les filtres type/categorie/annee.
     * Garantit < 300ms pour des catalogues de taille standard.
     */
    public List<SearchResult> search(String query, PublicationType type, Long categoryId, Integer year) {
        if (!indexReady || query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }

        long start = System.currentTimeMillis();

        List<String> queryTokens = tokenize(query.toLowerCase().trim());
        if (queryTokens.isEmpty()) {
            return Collections.emptyList();
        }

        // Vecteur TF-IDF de la requete
        Map<String, Double> queryVector = buildQueryVector(queryTokens);
        double queryNorm = computeNorm(queryVector);

        if (queryNorm == 0.0) {
            return Collections.emptyList();
        }

        // Calculer la similarite cosinus avec chaque document
        List<SearchResult> results = new ArrayList<>();
        for (Map.Entry<Long, Map<String, Double>> entry : documentVectors.entrySet()) {
            Long docId = entry.getKey();
            Map<String, Double> docVector = entry.getValue();
            Double docNorm = documentNorms.get(docId);

            if (docNorm == null || docNorm == 0.0) continue;

            double cosine = dotProduct(queryVector, docVector) / (queryNorm * docNorm);
            if (cosine > 0.01) {
                results.add(new SearchResult(docId, cosine));
            }
        }

        // Trier par score decroissant
        results.sort(Comparator.comparingDouble(SearchResult::getScore).reversed());

        // Appliquer les filtres type/categorie/annee
        if (type != null || categoryId != null || year != null) {
            Map<Long, Publication> pubCache = new HashMap<>();
            results = results.stream().filter(sr -> {
                Publication pub = pubCache.computeIfAbsent(sr.getPublicationId(),
                        id -> publicationRepository.findById(id).orElse(null));
                if (pub == null) return false;
                if (type != null && pub.getType() != type) return false;
                if (categoryId != null && (pub.getCategory() == null || !pub.getCategory().getId().equals(categoryId)))
                    return false;
                if (year != null && !year.equals(pub.getPublicationYear())) return false;
                return true;
            }).collect(Collectors.toList());
        }

        long elapsed = System.currentTimeMillis() - start;
        log.info("TF-IDF search '{}' : {} results in {}ms", query, results.size(), elapsed);

        return results;
    }

    // ── Construction du texte indexable d'une publication ──

    private String extractDocumentText(Publication pub) {
        StringBuilder sb = new StringBuilder();
        // Le titre a un poids plus eleve (repete 3 fois)
        if (pub.getTitle() != null) {
            sb.append(pub.getTitle()).append(" ");
            sb.append(pub.getTitle()).append(" ");
            sb.append(pub.getTitle()).append(" ");
        }
        if (pub.getResume() != null) sb.append(pub.getResume()).append(" ");
        if (pub.getKeywords() != null) {
            for (Keyword kw : pub.getKeywords()) {
                if (kw.getName() != null) {
                    sb.append(kw.getName()).append(" ");
                    sb.append(kw.getName()).append(" "); // poids double
                }
            }
        }
        if (pub.getAuthors() != null) {
            for (var a : pub.getAuthors()) {
                if (a.getFirstName() != null) sb.append(a.getFirstName()).append(" ");
                if (a.getLastName() != null) sb.append(a.getLastName()).append(" ");
            }
        }
        if (pub.getJournalName() != null) sb.append(pub.getJournalName()).append(" ");
        if (pub.getConferenceName() != null) sb.append(pub.getConferenceName()).append(" ");
        if (pub.getCategory() != null && pub.getCategory().getName() != null)
            sb.append(pub.getCategory().getName()).append(" ");
        return sb.toString();
    }

    // ── Vectorisation ──

    private Map<String, Double> buildDocumentVector(Publication pub) {
        String text = extractDocumentText(pub);
        List<String> tokens = tokenize(text);
        Map<String, Double> tfMap = calculateTF(tokens);

        Map<String, Double> vector = new HashMap<>();
        for (Map.Entry<String, Double> entry : tfMap.entrySet()) {
            Double idf = idfCache.get(entry.getKey());
            if (idf != null) {
                vector.put(entry.getKey(), entry.getValue() * idf);
            }
        }
        return vector;
    }

    private Map<String, Double> buildQueryVector(List<String> tokens) {
        Map<String, Double> tfMap = calculateTF(tokens);
        Map<String, Double> vector = new HashMap<>();
        for (Map.Entry<String, Double> entry : tfMap.entrySet()) {
            Double idf = idfCache.get(entry.getKey());
            if (idf != null) {
                vector.put(entry.getKey(), entry.getValue() * idf);
            } else {
                // Terme inconnu dans le corpus : IDF eleve
                double unknownIdf = Math.log((double) totalDocuments / 1.0) + 1.0;
                vector.put(entry.getKey(), entry.getValue() * unknownIdf);
            }
        }
        return vector;
    }

    // ── Mathematiques ──

    private double dotProduct(Map<String, Double> v1, Map<String, Double> v2) {
        double sum = 0.0;
        for (Map.Entry<String, Double> entry : v1.entrySet()) {
            Double v2Val = v2.get(entry.getKey());
            if (v2Val != null) {
                sum += entry.getValue() * v2Val;
            }
        }
        return sum;
    }

    private double computeNorm(Map<String, Double> vector) {
        double sum = 0.0;
        for (double val : vector.values()) {
            sum += val * val;
        }
        return Math.sqrt(sum);
    }

    // ── Tokenisation ──

    /**
     * Decoupe un texte en tokens : mise en minuscules, suppression des mots vides
     * et des termes de moins de 3 caracteres.
     *
     * @param text le texte a tokeniser
     * @return la liste des tokens extraits
     */
    public List<String> tokenize(String text) {
        if (text == null) return new ArrayList<>();
        return Arrays.stream(text.toLowerCase().split("[\\W_]+"))
                .filter(s -> s.length() > 2)
                .filter(s -> !STOP_WORDS.contains(s))
                .collect(Collectors.toList());
    }

    private Map<String, Double> calculateTF(List<String> tokens) {
        Map<String, Double> tfMap = new HashMap<>();
        if (tokens.isEmpty()) return tfMap;
        for (String token : tokens) {
            tfMap.put(token, tfMap.getOrDefault(token, 0.0) + 1.0);
        }
        double size = tokens.size();
        for (Map.Entry<String, Double> entry : tfMap.entrySet()) {
            entry.setValue(entry.getValue() / size);
        }
        return tfMap;
    }

    // ── Resultat de recherche ──

    /** Resultat de recherche TF-IDF contenant l'identifiant de la publication et le score de pertinence. */
    public static class SearchResult {
        private final Long publicationId;
        private final double score;

        public SearchResult(Long publicationId, double score) {
            this.publicationId = publicationId;
            this.score = score;
        }

        public Long getPublicationId() { return publicationId; }
        public double getScore() { return score; }
    }

    /** Indique si l'index TF-IDF est pret pour la recherche. */
    public boolean isIndexReady() { return indexReady; }
    /** Retourne le nombre total de documents indexes. */
    public int getTotalDocuments() { return totalDocuments; }
    /** Retourne la taille du vocabulaire (nombre de termes distincts dans l'index). */
    public int getVocabularySize() { return idfCache.size(); }
}