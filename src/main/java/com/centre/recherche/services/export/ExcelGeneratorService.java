package com.centre.recherche.services.export;

import com.centre.recherche.models.Publication;
import com.centre.recherche.services.StatisticsService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service de generation de rapports Excel (XLSX) pour les publications.
 * <p>
 * Genere trois types de classeurs :
 * <ul>
 *   <li>Publication : catalogue public avec feuilles donnees + statistiques</li>
 *   <li>Admin : rapport administratif avec statut et motif de rejet</li>
 *   <li>Chercheur : rapport individuel des publications d'un chercheur</li>
 * </ul>
 * Les statistiques sont fournies par {@link StatisticsService}.
 * </p>
 *
 * @see StatisticsService
 */
@Service
public class ExcelGeneratorService {

    @Autowired
    private StatisticsService statisticsService;

    /**
     * Genere un classeur Excel pour le catalogue public (publications publiees).
     * Contient une feuille de donnees et une feuille de statistiques.
     *
     * @param publications la liste des publications publiees
     * @return le contenu binaire du fichier XLSX
     * @throws IOException en cas d'erreur d'ecriture
     */
    public byte[] generatePublicationExcel(List<Publication> publications) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            // ── Feuille 1 : Publications ────────────────────────────────
            Sheet pubSheet = workbook.createSheet("Publications");

            String[] pubHeaders = {
                    "ID", "Titre", "Auteurs", "Annee", "Type", "Categorie",
                    "Mots-cles", "Resume", "Identifiant", "DOI",
                    "Revue/Journal", "Conference", "Editeur",
                    "Volume", "Numero", "Pages", "Langue", "Statut"
            };

            Row headerRow = pubSheet.createRow(0);
            for (int i = 0; i < pubHeaders.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(pubHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Publication pub : publications) {
                Row row = pubSheet.createRow(rowNum++);
                setCell(row, 0, pub.getId() != null ? String.valueOf(pub.getId()) : "", dataStyle);
                setCell(row, 1, safe(pub.getTitle()), dataStyle);
                setCell(row, 2, auteurs(pub), dataStyle);
                setCell(row, 3, pub.getPublicationYear() != null ? String.valueOf(pub.getPublicationYear()) : "", dataStyle);
                setCell(row, 4, pub.getType() != null ? pub.getType().name() : "", dataStyle);
                setCell(row, 5, pub.getCategory() != null ? pub.getCategory().getName() : "", dataStyle);
                setCell(row, 6, keywords(pub), dataStyle);
                setCell(row, 7, safe(pub.getResume()), dataStyle);
                setCell(row, 8, safe(pub.getUniqueIdentifier()), dataStyle);
                setCell(row, 9, safe(pub.getDoi()), dataStyle);
                setCell(row, 10, safe(pub.getJournalName()), dataStyle);
                setCell(row, 11, safe(pub.getConferenceName()), dataStyle);
                setCell(row, 12, safe(pub.getPublisher()), dataStyle);
                setCell(row, 13, safe(pub.getVolume()), dataStyle);
                setCell(row, 14, safe(pub.getIssue()), dataStyle);
                setCell(row, 15, safe(pub.getPages()), dataStyle);
                setCell(row, 16, safe(pub.getLanguage()), dataStyle);
                setCell(row, 17, pub.getStatus() != null ? pub.getStatus().name() : "", dataStyle);
            }

            // Auto-size columns
            for (int i = 0; i < pubHeaders.length; i++) {
                pubSheet.autoSizeColumn(i);
            }

            // ── Feuille 2 : Statistiques ───────────────────────────────
            Sheet statsSheet = workbook.createSheet("Statistiques");

            int statsRow = 0;

            // Par type
            statsRow = writeStatsSection(statsSheet, statsRow, headerStyle, dataStyle,
                    "Publications par type", statisticsService.getPublicationsCountByType());

            // Par categorie
            statsRow = writeStatsSection(statsSheet, statsRow + 1, headerStyle, dataStyle,
                    "Publications par categorie", statisticsService.getPublicationsCountByCategory());

            // Par annee
            Map<String, Long> byYearStr = statisticsService.getPublicationsCountByYear().entrySet().stream()
                    .collect(Collectors.toMap(e -> String.valueOf(e.getKey()), Map.Entry::getValue,
                            Long::sum, java.util.LinkedHashMap::new));
            writeStatsSection(statsSheet, statsRow + 1, headerStyle, dataStyle,
                    "Publications par annee", byYearStr);

            // Par chercheur
            writeStatsSection(statsSheet, statsRow + 1, headerStyle, dataStyle,
                    "Publications par chercheur", statisticsService.getPublicationsCountByResearcher());

            for (int i = 0; i < 3; i++) {
                statsSheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    // ── Rapport admin (toutes publications) ──────────────────────────────

    /**
     * Genere un classeur Excel administratif (toutes publications, tous statuts).
     * Contient une feuille de donnees avec le motif de rejet et une feuille de statistiques par statut.
     *
     * @param publications la liste de toutes les publications
     * @return le contenu binaire du fichier XLSX
     * @throws IOException en cas d'erreur d'ecriture
     */
    public byte[] generateAdminExcel(List<Publication> publications) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            Sheet sheet = workbook.createSheet("Toutes Publications");

            String[] headers = {
                    "ID", "Titre", "Auteurs", "Annee", "Type", "Categorie",
                    "Statut", "DOI", "Revue/Journal", "Conference",
                    "Motif rejet"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Publication pub : publications) {
                Row row = sheet.createRow(rowNum++);
                setCell(row, 0, pub.getId() != null ? String.valueOf(pub.getId()) : "", dataStyle);
                setCell(row, 1, safe(pub.getTitle()), dataStyle);
                setCell(row, 2, auteurs(pub), dataStyle);
                setCell(row, 3, pub.getPublicationYear() != null ? String.valueOf(pub.getPublicationYear()) : "", dataStyle);
                setCell(row, 4, pub.getType() != null ? pub.getType().name() : "", dataStyle);
                setCell(row, 5, pub.getCategory() != null ? pub.getCategory().getName() : "", dataStyle);
                setCell(row, 6, pub.getStatus() != null ? pub.getStatus().name() : "", dataStyle);
                setCell(row, 7, safe(pub.getDoi()), dataStyle);
                setCell(row, 8, safe(pub.getJournalName()), dataStyle);
                setCell(row, 9, safe(pub.getConferenceName()), dataStyle);
                setCell(row, 10, safe(pub.getValidationComment()), dataStyle);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Feuille stats admin
            Sheet statsSheet = workbook.createSheet("Statistiques");
            writeStatsSection(statsSheet, 0, headerStyle, dataStyle,
                    "Repartition par statut", statisticsService.getPublicationsCountByStatus());
            writeStatsSection(statsSheet, 2, headerStyle, dataStyle,
                    "Par type", statisticsService.getPublicationsCountByType());

            workbook.write(out);
            return out.toByteArray();
        }
    }

    // ── Rapport chercheur ────────────────────────────────────────────────

    /**
     * Genere un classeur Excel individuel pour un chercheur.
     * Contient les publications du chercheur avec leurs mots-cles et references bibliographiques.
     *
     * @param publications la liste des publications du chercheur
     * @return le contenu binaire du fichier XLSX
     * @throws IOException en cas d'erreur d'ecriture
     */
    public byte[] generateResearcherExcel(List<Publication> publications) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            Sheet sheet = workbook.createSheet("Mes Publications");

            String[] headers = {"Titre", "Annee", "Type", "Statut", "Categorie", "DOI", "Revue/Conf.", "Mots-cles"};

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Publication pub : publications) {
                Row row = sheet.createRow(rowNum++);
                setCell(row, 0, safe(pub.getTitle()), dataStyle);
                setCell(row, 1, pub.getPublicationYear() != null ? String.valueOf(pub.getPublicationYear()) : "", dataStyle);
                setCell(row, 2, pub.getType() != null ? pub.getType().name() : "", dataStyle);
                setCell(row, 3, pub.getStatus() != null ? pub.getStatus().name() : "", dataStyle);
                setCell(row, 4, pub.getCategory() != null ? pub.getCategory().getName() : "", dataStyle);
                setCell(row, 5, safe(pub.getDoi()), dataStyle);
                setCell(row, 6, biblioRef(pub), dataStyle);
                setCell(row, 7, keywords(pub), dataStyle);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private int writeStatsSection(Sheet sheet, int startRow, CellStyle headerStyle, CellStyle dataStyle,
                                  String title, Map<String, Long> data) {
        Row titleRow = sheet.createRow(startRow);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(title);
        titleCell.setCellStyle(headerStyle);

        Row headerRow = sheet.createRow(startRow + 1);
        Cell labelCell = headerRow.createCell(0);
        labelCell.setCellValue("Libelle");
        labelCell.setCellStyle(headerStyle);
        Cell valueCell = headerRow.createCell(1);
        valueCell.setCellValue("Nombre");
        valueCell.setCellStyle(headerStyle);

        int row = startRow + 2;
        for (Map.Entry<String, Long> entry : data.entrySet()) {
            Row dataRow = sheet.createRow(row++);
            setCell(dataRow, 0, entry.getKey(), dataStyle);
            setCell(dataRow, 1, String.valueOf(entry.getValue()), dataStyle);
        }
        return row;
    }

    private void setCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(new XSSFColor(new byte[]{(byte) 30, (byte) 58, (byte) 95}, null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setWrapText(true);
        return style;
    }

    private String safe(String s) {
        return s != null ? s : "";
    }

    private String auteurs(Publication pub) {
        if (pub.getAuthors() == null || pub.getAuthors().isEmpty()) return "";
        return pub.getAuthors().stream()
                .map(a -> (a.getFirstName() != null ? a.getFirstName() : "") + " " +
                          (a.getLastName() != null ? a.getLastName() : ""))
                .map(String::trim)
                .collect(Collectors.joining(", "));
    }

    private String keywords(Publication pub) {
        if (pub.getKeywords() == null || pub.getKeywords().isEmpty()) return "";
        return pub.getKeywords().stream()
                .map(k -> k.getName())
                .collect(Collectors.joining(", "));
    }

    private String biblioRef(Publication pub) {
        StringBuilder sb = new StringBuilder();
        if (pub.getJournalName() != null) sb.append(pub.getJournalName());
        if (pub.getConferenceName() != null) {
            if (sb.length() > 0) sb.append(" / ");
            sb.append(pub.getConferenceName());
        }
        return sb.toString();
    }
}