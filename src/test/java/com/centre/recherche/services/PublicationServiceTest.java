package com.centre.recherche.services;

import com.centre.recherche.models.Category;
import com.centre.recherche.models.Keyword;
import com.centre.recherche.models.Publication;
import com.centre.recherche.models.PublicationStatus;
import com.centre.recherche.models.PublicationType;
import com.centre.recherche.models.Researcher;
import com.centre.recherche.repositories.CategoryRepository;
import com.centre.recherche.repositories.KeywordRepository;
import com.centre.recherche.repositories.PublicationRepository;
import com.centre.recherche.services.search.TFIDFService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PublicationServiceTest {

    @Mock private PublicationRepository publicationRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private KeywordRepository keywordRepository;
    @Mock private TFIDFService tfidfService;

    @InjectMocks
    private PublicationService publicationService;

    private Publication samplePublication;

    @BeforeEach
    void setUp() {
        samplePublication = new Publication();
        samplePublication.setId(1L);
        samplePublication.setTitle("Test Publication");
        samplePublication.setType(PublicationType.ARTICLE_SCIENTIFIQUE);
        samplePublication.setStatus(PublicationStatus.BROUILLON);
        samplePublication.setPublicationYear(2024);
    }

    @Test
    void getPublishedPublicationById_shouldReturnPublication_whenPublished() {
        samplePublication.setStatus(PublicationStatus.PUBLIEE);
        when(publicationRepository.findById(1L)).thenReturn(Optional.of(samplePublication));

        Publication result = publicationService.getPublishedPublicationById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(PublicationStatus.PUBLIEE, result.getStatus());
    }

    @Test
    void getPublishedPublicationById_shouldReturnNull_whenNotPublished() {
        when(publicationRepository.findById(1L)).thenReturn(Optional.of(samplePublication));

        Publication result = publicationService.getPublishedPublicationById(1L);

        assertNull(result);
    }

    @Test
    void getPublishedPublicationById_shouldReturnNull_whenNotFound() {
        when(publicationRepository.findById(99L)).thenReturn(Optional.empty());

        Publication result = publicationService.getPublishedPublicationById(99L);

        assertNull(result);
    }

    @Test
    void countPublishedPublications_shouldReturnCount() {
        when(publicationRepository.countByStatus(PublicationStatus.PUBLIEE)).thenReturn(5L);

        long count = publicationService.countPublishedPublications();

        assertEquals(5L, count);
    }

    @Test
    void savePublication_shouldGenerateUniqueIdentifier_whenNull() {
        Researcher researcher = new Researcher();
        researcher.setId(1L);
        when(publicationRepository.save(any(Publication.class))).thenAnswer(inv -> inv.getArgument(0));

        Publication result = publicationService.savePublication(samplePublication, null, researcher);

        assertNotNull(result.getUniqueIdentifier());
        assertTrue(result.getUniqueIdentifier().startsWith("CRIS-"));
    }

    @Test
    void savePublication_shouldParseKeywords() {
        Researcher researcher = new Researcher();
        researcher.setId(1L);
        when(keywordRepository.findByName("AI")).thenReturn(null);
        when(keywordRepository.save(any(Keyword.class))).thenAnswer(inv -> inv.getArgument(0));
        when(publicationRepository.save(any(Publication.class))).thenAnswer(inv -> inv.getArgument(0));

        Publication result = publicationService.savePublication(samplePublication, "AI, ML", researcher);

        assertNotNull(result.getKeywords());
        assertEquals(2, result.getKeywords().size());
    }

    @Test
    void deletePublication_shouldCallRepository() {
        doNothing().when(publicationRepository).deleteById(1L);
        when(tfidfService.isIndexReady()).thenReturn(true);
        doNothing().when(tfidfService).rebuildIndex();

        publicationService.deletePublication(1L);

        verify(publicationRepository).deleteById(1L);
    }

    @Test
    void getAllPublications_shouldReturnAll() {
        when(publicationRepository.findAll()).thenReturn(List.of(samplePublication));

        List<Publication> result = publicationService.getAllPublications();

        assertEquals(1, result.size());
        assertEquals("Test Publication", result.get(0).getTitle());
    }

    @Test
    void getPublicationById_shouldReturnPublication() {
        when(publicationRepository.findById(1L)).thenReturn(Optional.of(samplePublication));

        Publication result = publicationService.getPublicationById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void incrementDownloadCount_shouldCallRepository() {
        doNothing().when(publicationRepository).incrementDownloadCount(1L);

        publicationService.incrementDownloadCount(1L);

        verify(publicationRepository).incrementDownloadCount(1L);
    }
}