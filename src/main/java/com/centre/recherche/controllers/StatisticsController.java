package com.centre.recherche.controllers;

import com.centre.recherche.models.Publication;
import com.centre.recherche.services.PublicationService;
import com.centre.recherche.services.StatisticsService;
import com.centre.recherche.services.export.ExcelGeneratorService;
import com.centre.recherche.services.export.PdfGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.List;

/**
 * Contrôleur des statistiques globales et des exports (préfixe : /statistics).
 * Affiche le tableau de bord statistique et génère les rapports PDF/Excel
 * des publications publiées et administratifs.
 */
@Controller
@RequestMapping("/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private PublicationService publicationService;

    @Autowired
    private PdfGeneratorService pdfGeneratorService;

    @Autowired
    private ExcelGeneratorService excelGeneratorService;

    // ── Dashboard ────────────────────────────────────────────────────────

    /** Affiche le tableau de bord des statistiques globales. */
    @GetMapping("/dashboard")
    public String statisticsDashboard(Model model) {
        model.addAttribute("theme", "admin");
        model.addAttribute("activePage", "admin-statistics");

        // Published-only statistics
        model.addAttribute("publicationsByYear", statisticsService.getPublicationsCountByYear());
        model.addAttribute("publicationsByCategory", statisticsService.getPublicationsCountByCategory());
        model.addAttribute("publicationsByResearcher", statisticsService.getPublicationsCountByResearcher());
        model.addAttribute("publicationsByType", statisticsService.getPublicationsCountByType());
        model.addAttribute("publicationsByStatus", statisticsService.getPublicationsCountByStatus());
        model.addAttribute("totalPublished", statisticsService.getTotalPublished());
        model.addAttribute("totalAll", statisticsService.getTotalAll());

        // All publications statistics (for admin view showing data regardless of status)
        model.addAttribute("allByYear", statisticsService.getAllCountByYear());
        model.addAttribute("allByCategory", statisticsService.getAllCountByCategory());
        model.addAttribute("allByType", statisticsService.getAllCountByType());
        model.addAttribute("allByResearcher", statisticsService.getAllCountByResearcher());

        return "statistics/dashboard";
    }

    // ── Exports PDF ──────────────────────────────────────────────────────

    /** Génère un rapport PDF des publications publiées. */
    @GetMapping("/report/pdf")
    public ResponseEntity<ByteArrayResource> generatePdfReport() {
        List<Publication> publications = statisticsService.getPublishedForReport();
        byte[] pdfBytes = pdfGeneratorService.generatePublicationReport(publications);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=rapport_publications.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfBytes.length)
                .body(new ByteArrayResource(pdfBytes));
    }

    /** Génère un rapport PDF administratif de toutes les publications. */
    @GetMapping("/report/pdf/admin")
    public ResponseEntity<ByteArrayResource> generateAdminPdfReport() {
        List<Publication> publications = statisticsService.getAllForReport();
        byte[] pdfBytes = pdfGeneratorService.generateAdminReport(publications);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=rapport_admin.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfBytes.length)
                .body(new ByteArrayResource(pdfBytes));
    }

    /** Génère une fiche PDF pour une publication spécifique. */
    @GetMapping("/report/pdf/publication/{id}")
    public ResponseEntity<ByteArrayResource> generatePublicationPdf(@PathVariable Long id) {
        Publication pub = publicationService.getPublicationById(id);
        if (pub == null) return ResponseEntity.notFound().build();

        byte[] pdfBytes = pdfGeneratorService.generatePublicationDetailPdf(pub);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=fiche_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfBytes.length)
                .body(new ByteArrayResource(pdfBytes));
    }

    // ── Exports Excel ────────────────────────────────────────────────────

    /** Génère un rapport Excel des publications publiées. */
    @GetMapping("/report/excel")
    public ResponseEntity<ByteArrayResource> generateExcelReport() throws IOException {
        List<Publication> publications = statisticsService.getPublishedForReport();
        byte[] excelBytes = excelGeneratorService.generatePublicationExcel(publications);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=rapport_publications.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(excelBytes.length)
                .body(new ByteArrayResource(excelBytes));
    }

    /** Génère un rapport Excel administratif de toutes les publications. */
    @GetMapping("/report/excel/admin")
    public ResponseEntity<ByteArrayResource> generateAdminExcelReport() throws IOException {
        List<Publication> publications = statisticsService.getAllForReport();
        byte[] excelBytes = excelGeneratorService.generateAdminExcel(publications);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=rapport_admin.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(excelBytes.length)
                .body(new ByteArrayResource(excelBytes));
    }
}