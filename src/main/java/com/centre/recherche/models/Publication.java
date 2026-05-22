package com.centre.recherche.models;

import jakarta.persistence.*;
import java.util.List;

/**
 * Entite JPA representant une publication scientifique.
 *
 * <p>Une publication peut etre un article, une communication de conference,
 * un rapport de recherche, une these/memoire ou un ouvrage. Elle possede
 * un cycle de vie defini par {@link PublicationStatus} : BROUILLON -> SOUMISE
 * -> EN_VALIDATION -> APPROUVEE -> PUBLIEE, avec retour possible en brouillon.</p>
 *
 * <p>Les publications sont liees a des {@link Researcher} (auteurs),
 * une {@link Category} et des {@link Keyword} via des relations ManyToMany/ManyToOne.</p>
 */
@Entity
public class Publication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    /** Resume ou abstract de la publication (stocke en TEXT). */
    @Column(columnDefinition = "TEXT")
    private String resume;

    private Integer publicationYear;

    @Enumerated(EnumType.STRING)
    private PublicationType type;

    @Enumerated(EnumType.STRING)
    private PublicationStatus status = PublicationStatus.BROUILLON;

    /** Chemin vers le fichier PDF uploade sur le serveur. */
    private String pdfFilePath;

    /** Score TF-IDF calcule pour le classement par pertinence lors des recherches. */
    private Double tfidfScore = 0.0;

    /** Identifiant interne unique genere automatiquement (format CRIS-timestamp). */
    private String uniqueIdentifier;

    /** Metadonnees supplementaires au format texte. */
    @Column(columnDefinition = "TEXT")
    private String metadata;

    /** Commentaire laisse par le validateur lors de l'approbation ou du rejet. */
    @Column(columnDefinition = "TEXT")
    private String validationComment;

    /** Identifiant DOI (Digital Object Identifier) de la publication. */
    private String doi;
    private String journalName;
    private String conferenceName;
    private String publisher;
    private String volume;
    private String issue;
    private String pages;
    private String language = "fr";

    // Article scientifique
    private Double impactFactor;

    // Communication conference
    private String conferenceLocation;
    private String conferenceDate;
    private String proceedings;

    // Rapport recherche
    private String reportNumber;
    private String sponsor;
    private String distribution;

    // These / Memoire
    private String jury;
    private String institution;
    private String discipline;
    private String degreeObtained;

    // Ouvrage
    private String isbn;
    private String coordinators;

    /** Nombre de telechargements du fichier PDF. */
    private Integer downloadCount = 0;

    /** Liste des chercheurs auteurs de cette publication (relation ManyToMany via publication_authors). */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "publication_authors",
            joinColumns = @JoinColumn(name = "publication_id"),
            inverseJoinColumns = @JoinColumn(name = "researcher_id")
    )
    private List<Researcher> authors;

    /** Categorie disciplinaire de la publication (relation ManyToOne). */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private Category category;

    /** Mots-cles associes a la publication (relation ManyToMany via publication_keywords). */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "publication_keywords",
            joinColumns = @JoinColumn(name = "publication_id"),
            inverseJoinColumns = @JoinColumn(name = "keyword_id")
    )
    private List<Keyword> keywords;

    // Constructeur vide
    public Publication() {}

    // ===== Getters et Setters =====

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getResume() { return resume; }
    public void setResume(String resume) { this.resume = resume; }

    public Integer getPublicationYear() { return publicationYear; }
    public void setPublicationYear(Integer publicationYear) { this.publicationYear = publicationYear; }

    public PublicationType getType() { return type; }
    public void setType(PublicationType type) { this.type = type; }

    public PublicationStatus getStatus() { return status; }
    public void setStatus(PublicationStatus status) { this.status = status; }

    public String getPdfFilePath() { return pdfFilePath; }
    public void setPdfFilePath(String pdfFilePath) { this.pdfFilePath = pdfFilePath; }

    public Double getTfidfScore() { return tfidfScore; }
    public void setTfidfScore(Double tfidfScore) { this.tfidfScore = tfidfScore; }

    public String getUniqueIdentifier() { return uniqueIdentifier; }
    public void setUniqueIdentifier(String uniqueIdentifier) { this.uniqueIdentifier = uniqueIdentifier; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    public String getValidationComment() { return validationComment; }
    public void setValidationComment(String validationComment) { this.validationComment = validationComment; }

    public String getDoi() { return doi; }
    public void setDoi(String doi) { this.doi = doi; }

    public String getJournalName() { return journalName; }
    public void setJournalName(String journalName) { this.journalName = journalName; }

    public String getConferenceName() { return conferenceName; }
    public void setConferenceName(String conferenceName) { this.conferenceName = conferenceName; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    public String getVolume() { return volume; }
    public void setVolume(String volume) { this.volume = volume; }

    public String getIssue() { return issue; }
    public void setIssue(String issue) { this.issue = issue; }

    public String getPages() { return pages; }
    public void setPages(String pages) { this.pages = pages; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public List<Researcher> getAuthors() { return authors; }
    public void setAuthors(List<Researcher> authors) { this.authors = authors; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public List<Keyword> getKeywords() { return keywords; }
    public void setKeywords(List<Keyword> keywords) { this.keywords = keywords; }

    public Double getImpactFactor() { return impactFactor; }
    public void setImpactFactor(Double impactFactor) { this.impactFactor = impactFactor; }

    public String getConferenceLocation() { return conferenceLocation; }
    public void setConferenceLocation(String conferenceLocation) { this.conferenceLocation = conferenceLocation; }

    public String getConferenceDate() { return conferenceDate; }
    public void setConferenceDate(String conferenceDate) { this.conferenceDate = conferenceDate; }

    public String getProceedings() { return proceedings; }
    public void setProceedings(String proceedings) { this.proceedings = proceedings; }

    public String getReportNumber() { return reportNumber; }
    public void setReportNumber(String reportNumber) { this.reportNumber = reportNumber; }

    public String getSponsor() { return sponsor; }
    public void setSponsor(String sponsor) { this.sponsor = sponsor; }

    public String getDistribution() { return distribution; }
    public void setDistribution(String distribution) { this.distribution = distribution; }

    public String getJury() { return jury; }
    public void setJury(String jury) { this.jury = jury; }

    public String getInstitution() { return institution; }
    public void setInstitution(String institution) { this.institution = institution; }

    public String getDiscipline() { return discipline; }
    public void setDiscipline(String discipline) { this.discipline = discipline; }

    public String getDegreeObtained() { return degreeObtained; }
    public void setDegreeObtained(String degreeObtained) { this.degreeObtained = degreeObtained; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getCoordinators() { return coordinators; }
    public void setCoordinators(String coordinators) { this.coordinators = coordinators; }

    public Integer getDownloadCount() { return downloadCount; }
    public void setDownloadCount(Integer downloadCount) { this.downloadCount = downloadCount; }
}