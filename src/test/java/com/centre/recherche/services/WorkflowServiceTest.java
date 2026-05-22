package com.centre.recherche.services;

import com.centre.recherche.models.Publication;
import com.centre.recherche.models.PublicationStatus;
import com.centre.recherche.repositories.PublicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowServiceTest {

    @Mock private PublicationRepository publicationRepository;

    @InjectMocks
    private WorkflowService workflowService;

    private Publication publication;

    @BeforeEach
    void setUp() {
        publication = new Publication();
        publication.setId(1L);
        publication.setTitle("Test");
        publication.setStatus(PublicationStatus.BROUILLON);
    }

    @Test
    void canTransition_brouillonToSoumise_shouldBeTrue() {
        assertTrue(workflowService.canTransition(PublicationStatus.BROUILLON, PublicationStatus.SOUMISE));
    }

    @Test
    void canTransition_soumiseToEnValidation_shouldBeTrue() {
        assertTrue(workflowService.canTransition(PublicationStatus.SOUMISE, PublicationStatus.EN_VALIDATION));
    }

    @Test
    void canTransition_enValidationToApprouvee_shouldBeTrue() {
        assertTrue(workflowService.canTransition(PublicationStatus.EN_VALIDATION, PublicationStatus.APPROUVEE));
    }

    @Test
    void canTransition_approuveeToPubliee_shouldBeTrue() {
        assertTrue(workflowService.canTransition(PublicationStatus.APPROUVEE, PublicationStatus.PUBLIEE));
    }

    @Test
    void canTransition_enValidationToRejetee_shouldBeTrue() {
        assertTrue(workflowService.canTransition(PublicationStatus.EN_VALIDATION, PublicationStatus.REJETEE));
    }

    @Test
    void canTransition_rejeteeToBrouillon_shouldBeTrue() {
        assertTrue(workflowService.canTransition(PublicationStatus.REJETEE, PublicationStatus.BROUILLON));
    }

    @Test
    void canTransition_invalidTransition_shouldBeFalse() {
        assertFalse(workflowService.canTransition(PublicationStatus.BROUILLON, PublicationStatus.PUBLIEE));
    }

    @Test
    void canTransition_publieeToBrouillon_shouldBeTrue() {
        assertTrue(workflowService.canTransition(PublicationStatus.PUBLIEE, PublicationStatus.BROUILLON));
    }

    @Test
    void getAllowedTransitions_brouillon_shouldContainSoumise() {
        Set<PublicationStatus> transitions = workflowService.getAllowedTransitions(PublicationStatus.BROUILLON);
        assertTrue(transitions.contains(PublicationStatus.SOUMISE));
        assertEquals(1, transitions.size());
    }

    @Test
    void getAllowedTransitions_soumise_shouldContainEnValidationAndBrouillon() {
        Set<PublicationStatus> transitions = workflowService.getAllowedTransitions(PublicationStatus.SOUMISE);
        assertTrue(transitions.contains(PublicationStatus.EN_VALIDATION));
        assertTrue(transitions.contains(PublicationStatus.BROUILLON));
        assertEquals(2, transitions.size());
    }

    @Test
    void submit_shouldTransitionToSoumise() {
        when(publicationRepository.findById(1L)).thenReturn(Optional.of(publication));
        when(publicationRepository.save(any(Publication.class))).thenAnswer(inv -> inv.getArgument(0));

        Publication result = workflowService.submit(1L);

        assertEquals(PublicationStatus.SOUMISE, result.getStatus());
        verify(publicationRepository).save(any(Publication.class));
    }

    @Test
    void reject_shouldTransitionToRejeteeWithComment() {
        publication.setStatus(PublicationStatus.EN_VALIDATION);
        when(publicationRepository.findById(1L)).thenReturn(Optional.of(publication));
        when(publicationRepository.save(any(Publication.class))).thenAnswer(inv -> inv.getArgument(0));

        Publication result = workflowService.reject(1L, "Non conforme");

        assertEquals(PublicationStatus.REJETEE, result.getStatus());
        assertEquals("Non conforme", result.getValidationComment());
    }

    @Test
    void publish_shouldTransitionToPubliee() {
        publication.setStatus(PublicationStatus.APPROUVEE);
        when(publicationRepository.findById(1L)).thenReturn(Optional.of(publication));
        when(publicationRepository.save(any(Publication.class))).thenAnswer(inv -> inv.getArgument(0));

        Publication result = workflowService.publish(1L);

        assertEquals(PublicationStatus.PUBLIEE, result.getStatus());
    }
}