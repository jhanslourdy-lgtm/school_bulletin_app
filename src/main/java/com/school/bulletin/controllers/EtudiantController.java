package com.school.bulletin.controller;

import com.school.bulletin.entities.Etudiant;
import com.school.bulletin.entities.Note;
import com.school.bulletin.entities.Utilisateur;
import com.school.bulletin.repositories.EtudiantRepository;
import com.school.bulletin.repositories.NoteRepository;
import com.school.bulletin.repositories.UtilisateurRepository;
import com.school.bulletin.services.PdfService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/etudiant")
public class EtudiantController {

    @Autowired
    private EtudiantRepository etudiantRepository;
    
    @Autowired
    private NoteRepository noteRepository;
    
    @Autowired
    private PdfService pdfService;

    @GetMapping("/notes")
    public String voirNotes(Model model, HttpSession session) {
        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateurConnecte");
        
        if (utilisateur == null) {
            return "redirect:/login";
        }
        
        System.out.println("=== DEBUG ETUDIANT NOTES ===");
        System.out.println("Utilisateur en session: " + utilisateur.getEmail() + " - Rôle: " + utilisateur.getRole());
        
        Etudiant etudiant = etudiantRepository.findByUtilisateur(utilisateur);
        
        if (etudiant == null) {
            model.addAttribute("error", "Étudiant non trouvé");
            return "redirect:/login";
        }
        
        System.out.println("Étudiant trouvé: " + etudiant.getNom() + " " + etudiant.getPrenom());
        
        List<Note> notes = noteRepository.findByEtudiant(etudiant);
        System.out.println("Notes trouvées: " + notes.size());
        
        // Afficher les notes pour debug
        for (Note note : notes) {
            System.out.println("Note: " + note.getValeur() + " - Matière: " + 
                (note.getMatiere() != null ? note.getMatiere().getNom() : "N/A"));
        }
        
        // CORRECTION ICI : Calculer les statistiques avec null-safety
        Double noteMax = null;
        Double noteMin = null;
        double somme = 0;
        int nombreNotesValides = 0;
        
        for (Note note : notes) {
            Double valeur = note.getValeur();
            if (valeur != null) {
                  Double valeurSur10 = valeur / 2.0;
                if (noteMax == null || valeurSur10 > noteMax) {
                    noteMax = valeurSur10;
                }
                if (noteMin == null || valeurSur10 < noteMin) {
                    noteMin = valeurSur10;
                }
                somme += valeurSur10;
                nombreNotesValides++;
            }
        }
        
        // Si aucune note valide
        if (noteMax == null) {
            noteMax = 0.0;
            noteMin = 0.0;
        }
        
        // Calculer la moyenne
        double moyenne = nombreNotesValides > 0 ? somme / nombreNotesValides : 0;
        
        // Arrondir les valeurs
        noteMax = Math.round(noteMax * 10.0) / 10.0;
        noteMin = Math.round(noteMin * 10.0) / 10.0;
        moyenne = Math.round(moyenne * 10.0) / 10.0;
        
        model.addAttribute("etudiant", etudiant);
        model.addAttribute("notes", notes);
        model.addAttribute("noteMax", noteMax);
        model.addAttribute("noteMin", noteMin);
        model.addAttribute("moyenne", moyenne);
        model.addAttribute("nombreNotes", notes.size());
        
        return "etudiant/notes";
    }
    
    @GetMapping("/download-bulletin")
    public void downloadBulletin(HttpServletResponse response, HttpSession session) throws IOException {
        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateurConnecte");
        
        if (utilisateur == null) {
            response.sendRedirect("/login");
            return;
        }
        
        Etudiant etudiant = etudiantRepository.findByUtilisateur(utilisateur);
        List<Note> notes = noteRepository.findByEtudiant(etudiant);
        
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=bulletin_" + 
            etudiant.getNom() + "_" + etudiant.getPrenom() + ".pdf");
        
        pdfService.genererBulletin(response, etudiant, notes);
    }
}