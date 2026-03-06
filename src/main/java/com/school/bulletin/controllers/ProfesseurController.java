package com.school.bulletin.controllers;

import jakarta.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;
import com.school.bulletin.entities.*;
import com.school.bulletin.repositories.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.school.bulletin.entities.Note;
import com.school.bulletin.repositories.NoteRepository;

import java.util.List;

@Controller
@RequestMapping("/professeur")
public class ProfesseurController {
   
    @Autowired 
    private NoteRepository noteRepository;
    
    @Autowired 
    private EtudiantRepository etudiantRepository;
    
    @Autowired 
    private MatiereRepository matiereRepository;
    
    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @GetMapping("/saisie")
    public String formulaireSaisie(Model model, 
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        // VÉRIFICATION IMPORTANTE DE SESSION
        Utilisateur user = (Utilisateur) session.getAttribute("utilisateurConnecte");
        if (user == null) {
            return "redirect:/login";
        }
        
        // Vérifier le rôle
        if (!"ROLE_PROFESSEUR".equals(user.getRole())) {
            return "redirect:/login";
        }
        
        System.out.println("=== DEBUG PROFESSEUR SAISIE ===");
        System.out.println("Professeur connecté: " + user.getNom() + " (" + user.getEmail() + ")");
        System.out.println("Nombre d'étudiants: " + etudiantRepository.count());
        System.out.println("Nombre de matières: " + matiereRepository.count());
        
        // TEST : Créez des données si la base est vide
        if (etudiantRepository.count() == 0) {
            System.out.println("DEBUG - Création d'étudiants de test...");
            
            // Créez d'abord un utilisateur étudiant
            Utilisateur etudiantUser = new Utilisateur();
            etudiantUser.setNom("Étudiant Test");
            etudiantUser.setEmail("etudiant@test.com");
            etudiantUser.setPassword("test123");
            etudiantUser.setRole("ROLE_ETUDIANT");
            utilisateurRepository.save(etudiantUser);
            
            // Créez l'étudiant
            Etudiant etudiant = new Etudiant();
            etudiant.setNom("Dupont");
            etudiant.setPrenom("Jean");
            etudiant.setClasse("Terminale A");
            etudiant.setUtilisateur(etudiantUser);
            etudiantRepository.save(etudiant);
            
            // Un deuxième étudiant
            Utilisateur etudiantUser2 = new Utilisateur();
            etudiantUser2.setNom("Martin Étudiant");
            etudiantUser2.setEmail("martin@test.com");
            etudiantUser2.setPassword("test123");
            etudiantUser2.setRole("ROLE_ETUDIANT");
            utilisateurRepository.save(etudiantUser2);
            
            Etudiant etudiant2 = new Etudiant();
            etudiant2.setNom("Martin");
            etudiant2.setPrenom("Sophie");
            etudiant2.setClasse("Terminale B");
            etudiant2.setUtilisateur(etudiantUser2);
            etudiantRepository.save(etudiant2);
        }
        
        if (matiereRepository.count() == 0) {
            System.out.println("DEBUG - Création de matières de test...");
            
            // Créez une matière
            Matiere matiere = new Matiere();
            matiere.setNom("Mathématiques");
            matiere.setCoefficient(5);
            // Assignez au professeur connecté si possible
            matiere.setProfesseur(user);
            matiereRepository.save(matiere);
            
            // Une deuxième matière
            Matiere matiere2 = new Matiere();
            matiere2.setNom("Physique");
            matiere2.setCoefficient(4);
            matiere2.setProfesseur(user);
            matiereRepository.save(matiere2);
            
            // Une troisième matière
            Matiere matiere3 = new Matiere();
            matiere3.setNom("Français");
            matiere3.setCoefficient(3);
            matiere3.setProfesseur(user);
            matiereRepository.save(matiere3);
        }
        
        // Récupérer les données
        List<Etudiant> etudiants = etudiantRepository.findAll();
        List<Matiere> matieres = matiereRepository.findAll();
        
        // Afficher dans les logs
        System.out.println("=== LISTE DES ÉTUDIANTS ===");
        for (Etudiant e : etudiants) {
            System.out.println("Étudiant: " + e.getNom() + " " + e.getPrenom() + " - Classe: " + e.getClasse());
        }
        
        System.out.println("=== LISTE DES MATIÈRES ===");
        for (Matiere m : matieres) {
            System.out.println("Matière: " + m.getNom() + " - Coef: " + m.getCoefficient() + 
                " - Prof: " + (m.getProfesseur() != null ? m.getProfesseur().getNom() : "Aucun"));
        }
        
        // VÉRIFICATION CRITIQUE : S'assurer que les données sont disponibles
        if (etudiants.isEmpty()) {
            System.out.println("ATTENTION: Aucun étudiant trouvé dans la base !");
            redirectAttributes.addFlashAttribute("warning", 
                "Aucun étudiant n'est inscrit. Veuillez d'abord créer des étudiants dans le tableau de bord admin.");
        }
        
        if (matieres.isEmpty()) {
            System.out.println("ATTENTION: Aucune matière trouvée dans la base !");
            redirectAttributes.addFlashAttribute("warning", 
                "Aucune matière n'est disponible. Veuillez d'abord créer des matières dans le tableau de bord admin.");
        }
        
        model.addAttribute("etudiants", etudiants);
        model.addAttribute("matieres", matieres);
        model.addAttribute("note", new Note());
         model.addAttribute("error", model.containsAttribute("error") ? model.getAttribute("error") : null);
    model.addAttribute("suggestionModification", model.containsAttribute("suggestionModification") ? model.getAttribute("suggestionModification") : false);
    model.addAttribute("success", model.containsAttribute("success") ? model.getAttribute("success") : null);
        
        return "prof/saisie";
    }
   @PostMapping("/enregistrer-note")
public String saveNote(@RequestParam Long etudiant,
                      @RequestParam Long matiere,
                      @RequestParam Double valeur,
                      @RequestParam String semestre,
                      @RequestParam(required = false) Long noteId,
                      HttpSession session,
                      RedirectAttributes redirectAttributes) {
    
    System.out.println("=== ENREGISTREMENT NOTE ===");
    
    // Vérifier la session
    Utilisateur user = (Utilisateur) session.getAttribute("utilisateurConnecte");
    if (user == null) {
        return "redirect:/login";
    }
    
    try {
        // 1. Validation de la note
        if (valeur < 0 || valeur > 20) {
            redirectAttributes.addFlashAttribute("error", 
                "La note doit être comprise entre 0 et 20 !");
            return "redirect:/professeur/saisie";
        }
        
        // 2. Récupérer l'étudiant et la matière
        Etudiant etudiantObj = etudiantRepository.findById(etudiant)
            .orElseThrow(() -> new RuntimeException("Étudiant non trouvé"));
        
        Matiere matiereObj = matiereRepository.findById(matiere)
            .orElseThrow(() -> new RuntimeException("Matière non trouvée"));
        
        // 3. Vérifier les permissions
        if (matiereObj.getProfesseur() == null || 
            !matiereObj.getProfesseur().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("error", 
                "Vous n'êtes pas assigné à cette matière !");
            return "redirect:/professeur/saisie";
        }
        
        // 4. Vérifier les doublons
        boolean doublonExiste;
        
        if (noteId != null && noteId > 0) {
            // Modification : vérifier les doublons en excluant cette note
            doublonExiste = noteRepository.existsByEtudiantAndMatiereAndSemestreExcludingId(
                etudiant, matiere, semestre, noteId);
        } else {
            // Nouvelle note : vérifier si une note existe déjà
            doublonExiste = noteRepository.existsByEtudiantAndMatiereAndSemestre(
                etudiant, matiere, semestre);
        }
        
        if (doublonExiste) {
            // Récupérer la note existante
            Optional<Note> noteExistante = noteRepository
                .findByEtudiantAndMatiereAndSemestre(etudiant, matiere, semestre);
            
            if (noteExistante.isPresent()) {
                Note note = noteExistante.get();
                redirectAttributes.addFlashAttribute("error", 
                    "Une note existe déjà pour " + etudiantObj.getNomComplet() + 
                    " en " + matiereObj.getNom() + " (" + semestre + ") : " + note.getValeur() + "/20");
                
                // Proposer de modifier la note existante
                redirectAttributes.addFlashAttribute("noteExistante", note);
                redirectAttributes.addFlashAttribute("suggestionModification", true);
                redirectAttributes.addFlashAttribute("etudiantSelectionne", etudiant);
                redirectAttributes.addFlashAttribute("matiereSelectionnee", matiere);
                redirectAttributes.addFlashAttribute("semestreSelectionne", semestre);
            }
            
            return "redirect:/professeur/saisie";
        }
        
        // 5. Créer ou modifier la note
        Note note;
        
        if (noteId != null && noteId > 0) {
            // MODIFICATION
            note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note à modifier non trouvée"));
            System.out.println("Modification de la note ID: " + noteId);
        } else {
            // NOUVELLE NOTE
            note = new Note();
            System.out.println("Création d'une nouvelle note");
        }
        
        // 6. Mettre à jour les informations
        note.setEtudiant(etudiantObj);
        note.setMatiere(matiereObj);
        note.setValeur(valeur);
        note.setSemestre(semestre);
        note.setDateNote(new Date());
        
        // 7. Sauvegarder
        noteRepository.save(note);
        
        System.out.println("Note enregistrée avec succès ! ID: " + note.getId());
        
        String message = noteId != null ? "Note modifiée" : "Note enregistrée";
        redirectAttributes.addFlashAttribute("success", 
            message + " avec succès pour " + etudiantObj.getNomComplet() + 
            " en " + matiereObj.getNom() + " (" + semestre + ")");
        
    } catch (Exception e) {
        System.out.println("ERREUR: " + e.getMessage());
        e.printStackTrace();
        redirectAttributes.addFlashAttribute("error", 
            "Erreur: " + e.getMessage());
    }
    
    return "redirect:/professeur/saisie";
}
    @GetMapping("/mes-notes")
public String mesNotes(Model model, HttpSession session) {
    Utilisateur user = (Utilisateur) session.getAttribute("utilisateurConnecte");
    if (user == null) {
        return "redirect:/login";
    }
    
    // Récupérer les matières du professeur
    List<Matiere> matieres = matiereRepository.findByProfesseur(user);
    
    // Récupérer toutes les notes pour ces matières
    List<Note> notes = new ArrayList<>();
    for (Matiere matiere : matieres) {
        notes.addAll(noteRepository.findByMatiereId(matiere.getId()));
    }
    
    // Grouper les notes par étudiant et matière
    Map<String, List<Note>> notesParEtudiantMatiere = notes.stream()
        .collect(Collectors.groupingBy(
            n -> n.getEtudiant().getId() + "-" + n.getMatiere().getId() + "-" + n.getSemestre()
        ));
    
    model.addAttribute("notes", notes);
    model.addAttribute("notesParEtudiantMatiere", notesParEtudiantMatiere);
    model.addAttribute("matieres", matieres);
    
    return "prof/mes_notes";
}

    @GetMapping("/liste-notes")
    public String listeNotes(Model model, HttpSession session) {
        // Vérifier la session
        Utilisateur user = (Utilisateur) session.getAttribute("utilisateurConnecte");
        if (user == null) {
            return "redirect:/login";
        }
        
        // Vérifier le rôle
        if (!"ROLE_PROFESSEUR".equals(user.getRole())) {
            return "redirect:/login";
        }
        
        List<Note> notes = noteRepository.findAll();
        
        System.out.println("=== DEBUG LISTE NOTES ===");
        System.out.println("Nombre de notes: " + notes.size());
        for (Note n : notes) {
            System.out.println("Note: " + n.getValeur() + 
                " - Étudiant: " + (n.getEtudiant() != null ? n.getEtudiant().getNom() : "N/A") +
                " - Matière: " + (n.getMatiere() != null ? n.getMatiere().getNom() : "N/A"));
        }
        
        model.addAttribute("notes", notes);
        return "prof/liste_notes";
    }
}