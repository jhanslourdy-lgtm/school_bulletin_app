//
//package com.school.bulletin.services;
//
//import com.school.bulletin.entities.Etudiant;
//import com.school.bulletin.entities.Note;
//import com.lowagie.text.*;
//import com.lowagie.text.pdf.PdfPTable;
//import com.lowagie.text.pdf.PdfWriter;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.stereotype.Service;
//
//import java.io.IOException;
//import java.util.List;
//
//@Service
//public class PdfService {
//
//    public void genererBulletin(HttpServletResponse response, Etudiant etudiant, List<Note> notes) throws IOException {
//        Document document = new Document(PageSize.A4);
//        PdfWriter.getInstance(document, response.getOutputStream());
//
//        document.open();
//
//        // Style du titre
//        Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
//        fontTitle.setSize(18);
//
//        // --- CORRECTION LIGNE 45 ---
//        // On s'assure d'utiliser l'objet 'etudiant' et non une String
//        Paragraph title = new Paragraph("Bulletin de Notes : " + etudiant.getNom() + " " + etudiant.getPrenom(), fontTitle);
//        title.setAlignment(Paragraph.ALIGN_CENTER);
//        document.add(title);
//
//        document.add(new Paragraph(" ")); // Espace
//
//        // Création du tableau des notes
//        PdfPTable table = new PdfPTable(3); // 3 colonnes : Matière, Note, Coefficient
//        table.setWidthPercentage(100);
//        
//        table.addCell("Matière");
//        table.addCell("Note");
//        table.addCell("Coefficient");
//
//        for (Note note : notes) {
//            // Ici, note.getMatiere() doit retourner un objet Matiere qui a un getNom()
//            table.addCell(note.getMatiere().getNom()); 
//            table.addCell(String.valueOf(note.getValeur()));
//            table.addCell(String.valueOf(note.getMatiere().getCoefficient()));
//        }
//
//        document.add(table);
//        document.close();
//    }
//}
package com.school.bulletin.services;

import com.school.bulletin.entities.Etudiant;
import com.school.bulletin.entities.Note;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class PdfService {

    public void genererBulletin(HttpServletResponse response, Etudiant etudiant, List<Note> notes) throws IOException {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();

        // Style du titre
        Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        fontTitle.setSize(18);

        // Titre
        Paragraph title = new Paragraph("Bulletin de Notes : " + etudiant.getNom() + " " + etudiant.getPrenom(), fontTitle);
        title.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(title);

        document.add(new Paragraph(" ")); // Espace

        // Information étudiant
        Font infoFont = FontFactory.getFont(FontFactory.HELVETICA);
        infoFont.setSize(12);
        
        Paragraph info = new Paragraph();
        info.add(new Chunk("Classe: " + etudiant.getClasse(), infoFont));
        info.add(Chunk.NEWLINE);
        info.add(new Chunk("Année: 2025-2026", infoFont));
        info.setAlignment(Paragraph.ALIGN_LEFT);
        document.add(info);
        
        document.add(new Paragraph(" ")); // Espace

        // Création du tableau des notes
        PdfPTable table = new PdfPTable(3); // 3 colonnes
        table.setWidthPercentage(100);
        
        // En-têtes
        table.addCell("Matière");
        table.addCell("Note");
        table.addCell("Coefficient");

        // Données
        for (Note note : notes) {
            if (note.getMatiere() != null) {
                table.addCell(note.getMatiere().getNom()); 
                table.addCell(String.valueOf(note.getValeur()));
                table.addCell(String.valueOf(note.getMatiere().getCoefficient()));
            }
        }

        document.add(table);
        
        // Pied de page
        document.add(new Paragraph(" "));
        Paragraph footer = new Paragraph("Généré le: " + new java.util.Date(), infoFont);
        footer.setAlignment(Paragraph.ALIGN_RIGHT);
        document.add(footer);
        
        document.close();
    }
}