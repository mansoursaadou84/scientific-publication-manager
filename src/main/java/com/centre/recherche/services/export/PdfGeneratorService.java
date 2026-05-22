package com.centre.recherche.services.export;

import com.centre.recherche.models.Publication;
import com.centre.recherche.services.StatisticsService;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.DashedLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service de generation de rapports PDF pour les publications.
 * <p>
 * Genere quatre types de rapports :
 * <ul>
 *   <li>Rapport institutionnel complet (catalogue public avec statistiques)</li>
 *   <li>Rapport administratif (toutes publications avec statuts)</li>
 *   <li>Fiche individuelle de publication</li>
 *   <li>Rapport chercheur (production individuelle)</li>
 * </ul>
 * Utilise la bibliotheque iText 7 pour la construction des documents PDF.
 * </p>
 *
 * @see StatisticsService
 */
@Service
public class PdfGeneratorService {

    private static final DeviceRgb HEADER_BG    = new DeviceRgb(30, 58, 95);
    private static final DeviceRgb STATS_BG     = new DeviceRgb(240, 244, 248);
    private static final DeviceRgb GREEN_BG     = new DeviceRgb(45, 106, 79);
    private static final DeviceRgb ROW_ALT_BG   = new DeviceRgb(227, 242, 253);
    private static final DeviceRgb BORDER_COLOR = new DeviceRgb(200, 210, 220);
    private static final DateTimeFormatter FMT   = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Autowired
    private StatisticsService statisticsService;

    // ── Rapport institutionnel complet ───────────────────────────────────

    /**
     * Genere le rapport institutionnel complet (catalogue public).
     * Inclut une page de titre, des statistiques et le tableau des publications publiees.
     *
     * @param publications la liste des publications publiees
     * @return le contenu binaire du fichier PDF
     */
    public byte[] generatePublicationReport(List<Publication> publications) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf);
        doc.setMargins(50, 50, 60, 50);

        // Page de titre
        doc.add(new Paragraph("\n\n\n"));
        doc.add(new Paragraph("Centre de Recherche et d'Innovation")
                .setFontSize(14).setFontColor(new DeviceRgb(30, 58, 95))
                .setTextAlignment(TextAlignment.CENTER));
        doc.add(new Paragraph("\n"));
        doc.add(new Paragraph("Rapport de Production Scientifique")
                .setFontSize(22).setBold()
                .setTextAlignment(TextAlignment.CENTER));
        doc.add(new Paragraph("\n"));
        doc.add(new LineSeparator(new DashedLine(1)));
        doc.add(new Paragraph("\n"));
        doc.add(new Paragraph("Genere le : " + LocalDateTime.now().format(FMT))
                .setFontSize(10).setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER));
        doc.add(new Paragraph("Total : " + publications.size() + " publication(s) publiee(s)")
                .setFontSize(11).setTextAlignment(TextAlignment.CENTER));

        // Section statistiques
        doc.add(new Paragraph("\n\n"));
        doc.add(new Paragraph("Synthese statistique")
                .setFontSize(14).setBold().setFontColor(HEADER_BG));
        doc.add(new LineSeparator(new DashedLine(0.5f)));
        doc.add(new Paragraph("\n"));

        addStatsTable(doc,
                statisticsService.getPublicationsCountByType(),
                statisticsService.getPublicationsCountByCategory(),
                statisticsService.getPublicationsCountByYear());

        // Tableau des publications
        doc.add(new Paragraph("\n\n"));
        doc.add(new Paragraph("Catalogue des publications")
                .setFontSize(14).setBold().setFontColor(HEADER_BG));
        doc.add(new LineSeparator(new DashedLine(0.5f)));
        doc.add(new Paragraph("\n"));

        float[] cols = new float[]{0.4f, 2.5f, 1.8f, 0.6f, 1.2f, 1.0f, 0.8f, 1.0f};
        Table table = new Table(UnitValue.createPercentArray(cols));
        table.setWidth(UnitValue.createPercentValue(100));

        for (String h : new String[]{"#", "Titre", "Auteurs", "Annee", "Type", "Categorie", "DOI", "Revue/Conf."}) {
            table.addHeaderCell(headerCell(h));
        }

        boolean alt = false;
        for (Publication pub : publications) {
            table.addCell(dataCell(String.valueOf(pub.getId()), alt));
            table.addCell(dataCell(safe(pub.getTitle()), alt));
            table.addCell(dataCell(auteurs(pub), alt));
            table.addCell(dataCell(annee(pub), alt));
            table.addCell(dataCell(pub.getType() != null ? pub.getType().name() : "-", alt));
            table.addCell(dataCell(pub.getCategory() != null ? pub.getCategory().getName() : "-", alt));
            table.addCell(dataCell(safe(pub.getDoi()), alt));
            table.addCell(dataCell(biblioRef(pub), alt));
            alt = !alt;
        }

        doc.add(table);

        // Pied de page
        doc.add(new Paragraph("\n- Document genere automatiquement - Centre de Recherche -")
                .setFontSize(8).setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER));

        doc.close();
        return baos.toByteArray();
    }

    // ── Rapport admin (toutes publications, avec statut) ──────────────────

    /**
     * Genere le rapport administratif (toutes publications, tous statuts).
     * Inclut la repartition par statut et le tableau complet des publications.
     *
     * @param publications la liste de toutes les publications
     * @return le contenu binaire du fichier PDF
     */
    public byte[] generateAdminReport(List<Publication> publications) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf);
        doc.setMargins(50, 50, 60, 50);

        doc.add(new Paragraph("Centre de Recherche — Rapport Administratif")
                .setFontSize(18).setBold().setFontColor(HEADER_BG)
                .setTextAlignment(TextAlignment.CENTER));
        doc.add(new Paragraph("Genere le : " + LocalDateTime.now().format(FMT))
                .setFontSize(10).setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER));
        doc.add(new Paragraph("Total : " + publications.size() + " publication(s) (tous statuts)")
                .setFontSize(11).setTextAlignment(TextAlignment.CENTER));

        // Stats par statut
        doc.add(new Paragraph("\n"));
        doc.add(new Paragraph("Repartition par statut").setFontSize(12).setBold());
        Map<String, Long> byStatus = statisticsService.getPublicationsCountByStatus();
        Table statusTable = new Table(UnitValue.createPercentArray(new float[]{3f, 1f}));
        statusTable.setWidth(UnitValue.createPercentValue(50));
        statusTable.addHeaderCell(headerCell("Statut"));
        statusTable.addHeaderCell(headerCell("Nombre"));
        boolean alt = false;
        for (Map.Entry<String, Long> e : byStatus.entrySet()) {
            statusTable.addCell(dataCell(e.getKey(), alt));
            statusTable.addCell(dataCell(String.valueOf(e.getValue()), alt));
            alt = !alt;
        }
        doc.add(statusTable);

        // Tableau complet
        doc.add(new Paragraph("\n"));
        doc.add(new Paragraph("Liste complete").setFontSize(12).setBold());
        float[] cols = new float[]{0.4f, 2.5f, 1.5f, 0.6f, 1.2f, 1.0f};
        Table table = new Table(UnitValue.createPercentArray(cols));
        table.setWidth(UnitValue.createPercentValue(100));

        for (String h : new String[]{"#", "Titre", "Auteurs", "Annee", "Type", "Statut"}) {
            table.addHeaderCell(headerCell(h));
        }

        alt = false;
        for (Publication pub : publications) {
            table.addCell(dataCell(String.valueOf(pub.getId()), alt));
            table.addCell(dataCell(safe(pub.getTitle()), alt));
            table.addCell(dataCell(auteurs(pub), alt));
            table.addCell(dataCell(annee(pub), alt));
            table.addCell(dataCell(pub.getType() != null ? pub.getType().name() : "-", alt));
            table.addCell(dataCell(pub.getStatus() != null ? pub.getStatus().name() : "-", alt));
            alt = !alt;
        }
        doc.add(table);

        doc.add(new Paragraph("\n- Rapport administratif confidentiel -")
                .setFontSize(8).setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER));

        doc.close();
        return baos.toByteArray();
    }

    // ── Fiche individuelle ───────────────────────────────────────────────

    /**
     * Genere la fiche individuelle d'une publication au format PDF.
     * Inclut tous les champs de la publication (titre, auteurs, DOI, resume, etc.).
     *
     * @param publication la publication a afficher
     * @return le contenu binaire du fichier PDF
     */
    public byte[] generatePublicationDetailPdf(Publication publication) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf);

        doc.add(new Paragraph("Fiche de Publication Scientifique")
                .setFontSize(20).setBold().setFontColor(HEADER_BG)
                .setTextAlignment(TextAlignment.CENTER));
        doc.add(new Paragraph("Genere le : " + LocalDateTime.now().format(FMT))
                .setFontSize(10).setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER));
        doc.add(new Paragraph("\n"));

        addField(doc, "Titre", safe(publication.getTitle()));
        addField(doc, "Auteurs", auteurs(publication));
        addField(doc, "Annee", annee(publication));
        addField(doc, "Type", publication.getType() != null ? publication.getType().name() : "-");
        addField(doc, "Categorie", publication.getCategory() != null ? publication.getCategory().getName() : "-");
        addField(doc, "Statut", publication.getStatus() != null ? publication.getStatus().name() : "-");
        addField(doc, "Identifiant interne", safe(publication.getUniqueIdentifier()));
        addField(doc, "DOI", safe(publication.getDoi()));
        addField(doc, "Revue / Journal", safe(publication.getJournalName()));
        addField(doc, "Conference", safe(publication.getConferenceName()));
        addField(doc, "Editeur", safe(publication.getPublisher()));
        addField(doc, "Volume / Numero / Pages",
                biblioFull(publication));
        addField(doc, "Langue", safe(publication.getLanguage()));
        addField(doc, "Mots-cles", publication.getKeywords() != null
                ? publication.getKeywords().stream().map(k -> k.getName()).collect(Collectors.joining(", "))
                : "-");

        doc.add(new Paragraph("\nResume :").setBold().setFontSize(12));
        doc.add(new Paragraph(safe(publication.getResume())).setFontSize(11));

        if (publication.getValidationComment() != null) {
            doc.add(new Paragraph("\nCommentaire de validation :").setBold().setFontSize(12));
            doc.add(new Paragraph(safe(publication.getValidationComment())).setFontSize(11)
                    .setFontColor(ColorConstants.RED));
        }

        doc.close();
        return baos.toByteArray();
    }

    // ── Rapport chercheur ────────────────────────────────────────────────

    /**
     * Genere le rapport individuel de production d'un chercheur.
     * Inclut un resume des statuts et le tableau des publications du chercheur.
     *
     * @param researcherName le nom complet du chercheur
     * @param publications   la liste des publications du chercheur
     * @return le contenu binaire du fichier PDF
     */
    public byte[] generateResearcherReport(String researcherName, List<Publication> publications) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf);

        doc.add(new Paragraph("Rapport individuel de production")
                .setFontSize(18).setBold().setFontColor(HEADER_BG)
                .setTextAlignment(TextAlignment.CENTER));
        doc.add(new Paragraph(researcherName)
                .setFontSize(14).setTextAlignment(TextAlignment.CENTER));
        doc.add(new Paragraph("Genere le : " + LocalDateTime.now().format(FMT))
                .setFontSize(10).setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER));
        doc.add(new Paragraph(publications.size() + " publication(s)")
                .setFontSize(11).setTextAlignment(TextAlignment.CENTER));

        // Stats rapides
        long publiees = publications.stream().filter(p -> p.getStatus() != null && p.getStatus().name().equals("PUBLIEE")).count();
        long enCours = publications.stream().filter(p -> p.getStatus() != null
                && !p.getStatus().name().equals("PUBLIEE") && !p.getStatus().name().equals("BROUILLON")).count();
        doc.add(new Paragraph(publiees + " publiee(s) | " + enCours + " en cours | "
                + (publications.size() - publiees - enCours) + " brouillon(s)")
                .setFontSize(10).setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER));

        // Tableau
        doc.add(new Paragraph("\n"));
        float[] cols = new float[]{2.5f, 0.6f, 1.2f, 1.0f, 1.0f};
        Table table = new Table(UnitValue.createPercentArray(cols));
        table.setWidth(UnitValue.createPercentValue(100));

        for (String h : new String[]{"Titre", "Annee", "Type", "Statut", "Revue/Conf."}) {
            table.addHeaderCell(headerCell(h));
        }

        boolean alt = false;
        for (Publication pub : publications) {
            table.addCell(dataCell(safe(pub.getTitle()), alt));
            table.addCell(dataCell(annee(pub), alt));
            table.addCell(dataCell(pub.getType() != null ? pub.getType().name() : "-", alt));
            table.addCell(dataCell(pub.getStatus() != null ? pub.getStatus().name() : "-", alt));
            table.addCell(dataCell(biblioRef(pub), alt));
            alt = !alt;
        }
        doc.add(table);

        doc.add(new Paragraph("\n- Rapport individuel - Centre de Recherche -")
                .setFontSize(8).setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER));

        doc.close();
        return baos.toByteArray();
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private void addStatsTable(Document doc, Map<String, Long> byType, Map<String, Long> byCat, Map<Integer, Long> byYear) {
        // Table par type
        doc.add(new Paragraph("Par type de publication").setBold().setFontSize(11));
        Table typeTable = new Table(UnitValue.createPercentArray(new float[]{3f, 1f}));
        typeTable.setWidth(UnitValue.createPercentValue(50));
        typeTable.addHeaderCell(headerCell("Type"));
        typeTable.addHeaderCell(headerCell("Nombre"));
        boolean alt = false;
        for (Map.Entry<String, Long> e : byType.entrySet()) {
            typeTable.addCell(dataCell(e.getKey(), alt));
            typeTable.addCell(dataCell(String.valueOf(e.getValue()), alt));
            alt = !alt;
        }
        doc.add(typeTable);

        // Table par categorie
        doc.add(new Paragraph("\nPar categorie").setBold().setFontSize(11));
        Table catTable = new Table(UnitValue.createPercentArray(new float[]{3f, 1f}));
        catTable.setWidth(UnitValue.createPercentValue(50));
        catTable.addHeaderCell(headerCell("Categorie"));
        catTable.addHeaderCell(headerCell("Nombre"));
        alt = false;
        for (Map.Entry<String, Long> e : byCat.entrySet()) {
            catTable.addCell(dataCell(e.getKey(), alt));
            catTable.addCell(dataCell(String.valueOf(e.getValue()), alt));
            alt = !alt;
        }
        doc.add(catTable);

        // Table par annee
        doc.add(new Paragraph("\nPar annee").setBold().setFontSize(11));
        Table yearTable = new Table(UnitValue.createPercentArray(new float[]{3f, 1f}));
        yearTable.setWidth(UnitValue.createPercentValue(50));
        yearTable.addHeaderCell(headerCell("Annee"));
        yearTable.addHeaderCell(headerCell("Nombre"));
        alt = false;
        for (Map.Entry<Integer, Long> e : byYear.entrySet()) {
            yearTable.addCell(dataCell(String.valueOf(e.getKey()), alt));
            yearTable.addCell(dataCell(String.valueOf(e.getValue()), alt));
            alt = !alt;
        }
        doc.add(yearTable);
    }

    private String annee(Publication pub) {
        int y = pub.getPublicationYear() != null ? pub.getPublicationYear() : 0;
        return y > 0 ? String.valueOf(y) : "-";
    }

    private String biblioRef(Publication pub) {
        StringBuilder sb = new StringBuilder();
        if (pub.getJournalName() != null) sb.append(pub.getJournalName());
        if (pub.getConferenceName() != null) {
            if (sb.length() > 0) sb.append(" / ");
            sb.append(pub.getConferenceName());
        }
        return sb.length() > 0 ? sb.toString() : "-";
    }

    private String biblioFull(Publication pub) {
        StringBuilder sb = new StringBuilder();
        if (pub.getVolume() != null) sb.append("Vol. ").append(pub.getVolume());
        if (pub.getIssue() != null) sb.append(", No. ").append(pub.getIssue());
        if (pub.getPages() != null) sb.append(", pp. ").append(pub.getPages());
        return sb.length() > 0 ? sb.toString() : "-";
    }

    private Cell headerCell(String text) {
        return new Cell()
                .add(new Paragraph(text).setBold().setFontSize(9)
                        .setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(HEADER_BG);
    }

    private Cell dataCell(String text, boolean alt) {
        Cell c = new Cell().add(new Paragraph(text != null ? text : "-").setFontSize(8));
        if (alt) c.setBackgroundColor(ROW_ALT_BG);
        return c;
    }

    private void addField(Document doc, String label, String value) {
        doc.add(new Paragraph()
                .add(new Text(label + " : ").setBold())
                .add(new Text(value != null ? value : "-"))
                .setFontSize(11));
    }

    private String safe(String s) {
        return s != null ? s : "-";
    }

    private String auteurs(Publication pub) {
        if (pub.getAuthors() == null || pub.getAuthors().isEmpty()) return "Inconnu";
        return pub.getAuthors().stream()
                .map(a -> (a.getFirstName() != null ? a.getFirstName() : "") + " " +
                          (a.getLastName() != null ? a.getLastName() : ""))
                .map(String::trim)
                .collect(Collectors.joining(", "));
    }
}