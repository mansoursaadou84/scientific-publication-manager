package com.centre.recherche.services;

import com.centre.recherche.models.Category;
import com.centre.recherche.models.Keyword;
import com.centre.recherche.models.Publication;
import com.centre.recherche.models.PublicationStatus;
import com.centre.recherche.models.PublicationType;
import com.centre.recherche.repositories.PublicationRepository;
import com.centre.recherche.services.search.TFIDFService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TFIDFServiceTest {

    @Mock private PublicationRepository publicationRepository;

    private TFIDFService tfidfService;

    @BeforeEach
    void setUp() {
        tfidfService = new TFIDFService();
        // Inject mock manually since TFIDFService uses @Autowired
        try {
            var field = TFIDFService.class.getDeclaredField("publicationRepository");
            field.setAccessible(true);
            field.set(tfidfService, publicationRepository);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void rebuildIndex_shouldBuildFromPublishedPublications() {
        Publication pub = new Publication();
        pub.setId(1L);
        pub.setTitle("Intelligence Artificielle en Sante");
        pub.setResume("Application de l'intelligence artificielle dans le domaine de la sante");
        pub.setStatus(PublicationStatus.PUBLIEE);
        pub.setType(PublicationType.ARTICLE_SCIENTIFIQUE);
        pub.setPublicationYear(2024);
        Keyword kw = new Keyword();
        kw.setName("Intelligence Artificielle");
        pub.setKeywords(List.of(kw));
        Category cat = new Category();
        cat.setId(1L);
        pub.setCategory(cat);

        when(publicationRepository.findByStatus(PublicationStatus.PUBLIEE)).thenReturn(List.of(pub));

        tfidfService.rebuildIndex();

        assertTrue(tfidfService.isIndexReady());
        assertEquals(1, tfidfService.getTotalDocuments());
    }

    @Test
    void search_shouldReturnResults_whenQueryMatches() {
        Publication pub = new Publication();
        pub.setId(1L);
        pub.setTitle("Intelligence Artificielle en Sante");
        pub.setResume("Application de l'intelligence artificielle dans le domaine de la sante");
        pub.setStatus(PublicationStatus.PUBLIEE);
        pub.setType(PublicationType.ARTICLE_SCIENTIFIQUE);
        pub.setPublicationYear(2024);
        Keyword kw = new Keyword();
        kw.setName("Intelligence Artificielle");
        pub.setKeywords(List.of(kw));
        Category cat = new Category();
        cat.setId(1L);
        pub.setCategory(cat);

        when(publicationRepository.findByStatus(PublicationStatus.PUBLIEE)).thenReturn(List.of(pub));

        tfidfService.rebuildIndex();

        var results = tfidfService.search("intelligence artificielle", null, null, null);
        assertFalse(results.isEmpty());
        assertEquals(1L, results.get(0).getPublicationId());
        assertTrue(results.get(0).getScore() > 0);
    }

    @Test
    void search_shouldReturnEmpty_whenNoMatch() {
        Publication pub = new Publication();
        pub.setId(1L);
        pub.setTitle("Chimie Organique");
        pub.setResume("Etude des composés organiques");
        pub.setStatus(PublicationStatus.PUBLIEE);
        pub.setType(PublicationType.ARTICLE_SCIENTIFIQUE);
        pub.setPublicationYear(2024);

        when(publicationRepository.findByStatus(PublicationStatus.PUBLIEE)).thenReturn(List.of(pub));

        tfidfService.rebuildIndex();

        var results = tfidfService.search("intelligence artificielle", null, null, null);
        assertTrue(results.isEmpty());
    }

    @Test
    void tokenize_shouldSplitAndNormalizeText() {
        List<String> tokens = tfidfService.tokenize("L'Intelligence Artificielle en Sante 2024!");
        assertFalse(tokens.isEmpty());
        assertTrue(tokens.contains("intelligence"));
        assertTrue(tokens.contains("artificielle"));
    }

    @Test
    void isIndexReady_shouldBeFalseBeforeRebuild() {
        assertFalse(tfidfService.isIndexReady());
    }
}