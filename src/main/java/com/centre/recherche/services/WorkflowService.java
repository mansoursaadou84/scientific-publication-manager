package com.centre.recherche.services;

import com.centre.recherche.models.Publication;
import com.centre.recherche.models.PublicationStatus;
import com.centre.recherche.repositories.PublicationRepository;
import com.centre.recherche.services.search.TFIDFService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Service de gestion du cycle de vie des publications (machine a etats).
 * <p>
 * Definit les transitions autorisees entre les statuts {@link PublicationStatus}
 * et orchestre les changements d'etat : validation, approbation, rejet, publication,
 * et retour en brouillon. Chaque transition declenche une notification email
 * via {@link EmailService} et reconstruit l'index {@link TFIDFService} si necessaire.
 * </p>
 *
 * @see PublicationStatus
 * @see EmailService
 * @see TFIDFService
 */
@Service
public class WorkflowService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowService.class);

    private static final Map<PublicationStatus, Set<PublicationStatus>> TRANSITIONS = Map.of(
            PublicationStatus.BROUILLON, EnumSet.of(PublicationStatus.SOUMISE),
            PublicationStatus.SOUMISE, EnumSet.of(PublicationStatus.EN_VALIDATION, PublicationStatus.BROUILLON),
            PublicationStatus.EN_VALIDATION, EnumSet.of(PublicationStatus.APPROUVEE, PublicationStatus.REJETEE),
            PublicationStatus.APPROUVEE, EnumSet.of(PublicationStatus.PUBLIEE, PublicationStatus.BROUILLON),
            PublicationStatus.REJETEE, EnumSet.of(PublicationStatus.BROUILLON),
            PublicationStatus.PUBLIEE, EnumSet.of(PublicationStatus.BROUILLON)
    );

    @Autowired
    private PublicationRepository publicationRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private TFIDFService tfidfService;

    /** Verifie si la transition d'un statut a un autre est autorisee. */
    public boolean canTransition(PublicationStatus from, PublicationStatus to) {
        Set<PublicationStatus> allowed = TRANSITIONS.get(from);
        return allowed != null && allowed.contains(to);
    }

    /** Retourne l'ensemble des statuts accessibles depuis le statut courant. */
    public Set<PublicationStatus> getAllowedTransitions(PublicationStatus current) {
        return TRANSITIONS.getOrDefault(current, EnumSet.noneOf(PublicationStatus.class));
    }

    /** Effectue une transition de statut sur une publication, avec commentaire optionnel et notifications email. */
    @Transactional
    public Publication transition(Long publicationId, PublicationStatus newStatus, String comment) {
        Publication pub = publicationRepository.findById(publicationId).orElse(null);
        if (pub == null) {
            throw new IllegalArgumentException("Publication introuvable : " + publicationId);
        }

        if (!canTransition(pub.getStatus(), newStatus)) {
            throw new IllegalStateException(
                    "Transition impossible : " + pub.getStatus() + " → " + newStatus);
        }

        PublicationStatus oldStatus = pub.getStatus();
        pub.setStatus(newStatus);

        // Commentaire de validation (rejet ou autre)
        if (comment != null && !comment.isBlank()) {
            pub.setValidationComment(comment);
        } else if (newStatus == PublicationStatus.BROUILLON) {
            // Remise a zero du commentaire quand on revient en brouillon
            pub.setValidationComment(null);
        }

        publicationRepository.save(pub);

        // Reconstruire l'index TF-IDF si la publication entre/sort de PUBLIEE
        if (oldStatus == PublicationStatus.PUBLIEE || newStatus == PublicationStatus.PUBLIEE) {
            tfidfService.rebuildIndex();
        }

        // Envoyer la notification email
        emailService.sendWorkflowNotification(pub, newStatus,
                newStatus == PublicationStatus.REJETEE ? comment : null);

        log.info("Workflow: Publication #{} {} → {} (comment: {})",
                publicationId, oldStatus, newStatus,
                comment != null && !comment.isBlank() ? "oui" : "non");

        return pub;
    }

    /** Soumet une publication (transition vers SOUMISE) et notifie les administrateurs. */
    @Transactional
    public Publication submit(Long publicationId) {
        Publication pub = transition(publicationId, PublicationStatus.SOUMISE, null);
        emailService.sendAdminNotification(pub, "SOUMISE");
        return pub;
    }

    /** Place une publication en validation (transition vers EN_VALIDATION). */
    @Transactional
    public Publication startValidation(Long publicationId) {
        return transition(publicationId, PublicationStatus.EN_VALIDATION, null);
    }

    /** Approuve une publication (transition vers APPROUVEE). */
    @Transactional
    public Publication approve(Long publicationId) {
        return transition(publicationId, PublicationStatus.APPROUVEE, null);
    }

    /** Rejette une publication avec un motif (transition vers REJETEE). */
    @Transactional
    public Publication reject(Long publicationId, String reason) {
        return transition(publicationId, PublicationStatus.REJETEE, reason);
    }

    /** Publie une publication (transition vers PUBLIEE). */
    @Transactional
    public Publication publish(Long publicationId) {
        return transition(publicationId, PublicationStatus.PUBLIEE, null);
    }

    /** Replace une publication en brouillon (transition vers BROUILLON). */
    @Transactional
    public Publication backToDraft(Long publicationId) {
        return transition(publicationId, PublicationStatus.BROUILLON, null);
    }
}