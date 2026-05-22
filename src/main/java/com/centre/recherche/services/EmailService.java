package com.centre.recherche.services;

import com.centre.recherche.models.Publication;
import com.centre.recherche.models.PublicationStatus;
import com.centre.recherche.models.Researcher;
import com.centre.recherche.models.Role;
import com.centre.recherche.models.User;
import com.centre.recherche.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

/**
 * Service de notification par email pour les changements de statut du workflow.
 * <p>
 * Envoie de maniere asynchrone des emails HTML aux auteurs lors des transitions
 * de statut de leurs publications, et aux administrateurs/documentalistes
 * lors de la soumission d'une nouvelle publication.
 * </p>
 *
 * @see WorkflowService
 * @see PublicationStatus
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserRepository userRepository;

    @Value("${app.mail.from:centre.recherche@univ.edu}")
    private String fromAddress;

    /** Notifie les auteurs d'une publication du changement de statut, de maniere asynchrone. */
    @Async
    public void sendWorkflowNotification(Publication publication, PublicationStatus newStatus, String comment) {
        if (publication.getAuthors() == null || publication.getAuthors().isEmpty()) return;

        for (Researcher author : publication.getAuthors()) {
            if (author.getUser() == null || author.getUser().getEmail() == null) continue;
            try {
                String to = author.getUser().getEmail();
                String subject = buildSubject(publication, newStatus);
                String body = buildBody(publication, newStatus, comment, author);
                sendHtml(to, subject, body);
            } catch (Exception e) {
                log.warn("Echec envoi email a {} : {}", author.getUser().getEmail(), e.getMessage());
            }
        }
    }

    /** Notifie les administrateurs et documentalistes d'une action sur une publication, de maniere asynchrone. */
    @Async
    public void sendAdminNotification(Publication publication, String action) {
        if (publication.getAuthors() == null) return;
        String authorName = publication.getAuthors().stream()
                .map(a -> (a.getFirstName() != null ? a.getFirstName() : "") + " " + (a.getLastName() != null ? a.getLastName() : ""))
                .reduce((a, b) -> a + ", " + b)
                .orElse("Inconnu");

        String subject = "Nouvelle publication soumise : " + publication.getTitle();
        String body = "<div style='font-family:Arial,sans-serif;max-width:600px;margin:0 auto'>"
                + "<h2 style='color:#1e3a5f'>Publication soumise pour validation</h2>"
                + "<p><strong>Titre :</strong> " + publication.getTitle() + "</p>"
                + "<p><strong>Auteur(s) :</strong> " + authorName + "</p>"
                + "<p><strong>Type :</strong> " + publication.getType() + "</p>"
                + "<p>Connectez-vous au <a href='http://localhost:8080/admin/publications'>panneau d'administration</a> pour traiter cette publication.</p>"
                + "</div>";

        // Notifier les admins
        for (User admin : userRepository.findByRole(Role.ADMIN)) {
            if (admin.getEmail() != null) {
                try { sendHtml(admin.getEmail(), subject, body); } catch (Exception e) {
                    log.warn("Echec envoi notification admin {} : {}", admin.getEmail(), e.getMessage());
                }
            }
        }

        // Notifier les documentalistes
        String docBody = "<div style='font-family:Arial,sans-serif;max-width:600px;margin:0 auto'>"
                + "<h2 style='color:#5b3a8c'>Publication soumise pour validation</h2>"
                + "<p><strong>Titre :</strong> " + publication.getTitle() + "</p>"
                + "<p><strong>Auteur(s) :</strong> " + authorName + "</p>"
                + "<p><strong>Type :</strong> " + publication.getType() + "</p>"
                + "<p>Connectez-vous a votre <a href='http://localhost:8080/documentaliste/publications'>espace documentaliste</a> pour traiter cette publication.</p>"
                + "</div>";

        for (User doc : userRepository.findByRole(Role.DOCUMENTALISTE)) {
            if (doc.getEmail() != null) {
                try { sendHtml(doc.getEmail(), subject, docBody); } catch (Exception e) {
                    log.warn("Echec envoi notification documentaliste {} : {}", doc.getEmail(), e.getMessage());
                }
            }
        }
    }

    private void sendHtml(String to, String subject, String body) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(fromAddress);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, true);
        mailSender.send(message);
        log.info("Email envoye a {} : {}", to, subject);
    }

    private String buildSubject(Publication pub, PublicationStatus status) {
        return "Publication \"" + pub.getTitle() + "\" — " + labelForStatus(status);
    }

    private String buildBody(Publication pub, PublicationStatus status, String comment, Researcher author) {
        String name = (author.getFirstName() != null ? author.getFirstName() : "");
        String color = colorForStatus(status);
        String label = labelForStatus(status);

        StringBuilder sb = new StringBuilder();
        sb.append("<div style='font-family:Arial,sans-serif;max-width:600px;margin:0 auto'>");
        sb.append("<h2 style='color:#1e3a5f'>Bonjour ").append(name).append(",</h2>");
        sb.append("<p>Le statut de votre publication a ete mis a jour :</p>");
        sb.append("<div style='background:#f7fafc;border:1px solid #e2e8f0;border-radius:8px;padding:1.2rem;margin:1rem 0'>");
        sb.append("<p style='margin:0 0 0.5rem'><strong>Titre :</strong> ").append(pub.getTitle()).append("</p>");
        sb.append("<p style='margin:0 0 0.5rem'><strong>Nouveau statut :</strong> ")
          .append("<span style='background:").append(color).append(";color:#fff;padding:2px 12px;border-radius:12px;font-size:0.8rem;font-weight:600'>")
          .append(label).append("</span></p>");
        if (pub.getPublicationYear() != null) {
            sb.append("<p style='margin:0'><strong>Annee :</strong> ").append(pub.getPublicationYear()).append("</p>");
        }
        sb.append("</div>");

        if (comment != null && !comment.isBlank()) {
            sb.append("<div style='background:#fef3c7;border:1px solid #fbd38d;border-radius:8px;padding:1rem;margin:1rem 0'>");
            sb.append("<strong>Commentaire :</strong> ").append(comment);
            sb.append("</div>");
        }

        if (status == PublicationStatus.PUBLIEE) {
            sb.append("<p> Votre publication est desormais visible dans le <a href='http://localhost:8080/search'>catalogue public</a>.</p>");
        } else if (status == PublicationStatus.REJETEE) {
            sb.append("<p>Vous pouvez modifier votre publication et la resoumettre depuis votre <a href='http://localhost:8080/researcher/publications'>espace chercheur</a>.</p>");
        }

        sb.append("<hr style='border:none;border-top:1px solid #e2e8f0;margin:1.5rem 0'/>");
        sb.append("<p style='color:#a0aec0;font-size:0.8rem'>Centre de Recherche — Systeme de Gestion des Publications</p>");
        sb.append("</div>");
        return sb.toString();
    }

    private String labelForStatus(PublicationStatus s) {
        return switch (s) {
            case BROUILLON -> "Brouillon";
            case SOUMISE -> "Soumise";
            case EN_VALIDATION -> "En validation";
            case APPROUVEE -> "Approuvee";
            case REJETEE -> "Rejetee";
            case PUBLIEE -> "Publiee";
        };
    }

    private String colorForStatus(PublicationStatus s) {
        return switch (s) {
            case BROUILLON -> "#78909c";
            case SOUMISE -> "#1976d2";
            case EN_VALIDATION -> "#f57c00";
            case APPROUVEE -> "#2e7d32";
            case REJETEE -> "#d32f2f";
            case PUBLIEE -> "#7b1fa2";
        };
    }
}