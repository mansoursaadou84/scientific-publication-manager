package com.centre.recherche.services;

import com.centre.recherche.models.Publication;
import com.centre.recherche.models.PublicationStatus;
import com.centre.recherche.models.PublicationType;
import com.centre.recherche.repositories.PublicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

    @Mock private PublicationRepository publicationRepository;

    @InjectMocks
    private StatisticsService statisticsService;

    private Publication pub1, pub2;

    @BeforeEach
    void setUp() {
        pub1 = new Publication();
        pub1.setId(1L);
        pub1.setTitle("Pub 1");
        pub1.setPublicationYear(2024);
        pub1.setType(PublicationType.ARTICLE_SCIENTIFIQUE);
        pub1.setStatus(PublicationStatus.PUBLIEE);

        pub2 = new Publication();
        pub2.setId(2L);
        pub2.setTitle("Pub 2");
        pub2.setPublicationYear(2023);
        pub2.setType(PublicationType.COMMUNICATION_CONFERENCE);
        pub2.setStatus(PublicationStatus.PUBLIEE);
    }

    @Test
    void countByStatus_shouldReturnCorrectCount() {
        when(publicationRepository.findAll()).thenReturn(List.of(pub1, pub2));

        long count = statisticsService.countByStatus(PublicationStatus.PUBLIEE);

        assertEquals(2L, count);
    }

    @Test
    void getTotalPublished_shouldReturnCount() {
        when(publicationRepository.findByStatus(PublicationStatus.PUBLIEE))
                .thenReturn(List.of(pub1, pub2, pub1, pub2, pub1));

        long total = statisticsService.getTotalPublished();

        assertEquals(5L, total);
    }

    @Test
    void getPublicationsCountByStatus_shouldReturnMap() {
        Publication pub3 = new Publication();
        pub3.setId(3L);
        pub3.setTitle("Pub 3");
        pub3.setStatus(PublicationStatus.BROUILLON);

        when(publicationRepository.findAll()).thenReturn(List.of(pub1, pub2, pub3));

        Map<String, Long> result = statisticsService.getPublicationsCountByStatus();

        assertNotNull(result);
        assertEquals(2L, result.get("PUBLIEE"));
        assertEquals(1L, result.get("BROUILLON"));
    }
}