package com.centre.recherche.models;

/**
 * Enumeration des statuts du cycle de vie d'une publication.
 *
 * <p>Le workflow de validation suit les transitions suivantes :</p>
 * <pre>
 *   BROUILLON -&gt; SOUMISE -&gt; EN_VALIDATION -&gt; APPROUVEE -&gt; PUBLIEE
 *       ^                                    |
 *       +------------------------------------+
 *   REJETEE -&gt; BROUILLON
 *   PUBLIEE -&gt; BROUILLON
 * </pre>
 *
 * @see com.centre.recherche.services.WorkflowService
 */
public enum PublicationStatus {
    BROUILLON,
    SOUMISE,
    EN_VALIDATION,
    APPROUVEE,
    REJETEE,
    PUBLIEE
}