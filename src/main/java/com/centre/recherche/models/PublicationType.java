package com.centre.recherche.models;

/**
 * Enumeration des types de publications scientifiques.
 *
 * <p>Chaque type possede des champs specifiques dans {@link Publication} :</p>
 * <ul>
 *   <li>{@code ARTICLE_SCIENTIFIQUE} -- impactFactor, journalName, doi, volume, issue, pages.</li>
 *   <li>{@code COMMUNICATION_CONFERENCE} -- conferenceName, conferenceLocation, conferenceDate, proceedings.</li>
 *   <li>{@code RAPPORT_RECHERCHE} -- reportNumber, sponsor, distribution.</li>
 *   <li>{@code THESE_MEMOIRE} -- jury, institution, discipline, degreeObtained.</li>
 *   <li>{@code OUVRAGE} -- isbn, coordinators, publisher.</li>
 * </ul>
 */
public enum PublicationType {
    ARTICLE_SCIENTIFIQUE,
    COMMUNICATION_CONFERENCE,
    RAPPORT_RECHERCHE,
    THESE_MEMOIRE,
    OUVRAGE
}
