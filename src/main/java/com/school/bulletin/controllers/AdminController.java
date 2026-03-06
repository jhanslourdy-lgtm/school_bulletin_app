
package com.school.bulletin.controllers;

import com.school.bulletin.entities.Etudiant;
import com.school.bulletin.entities.Matiere;
import com.school.bulletin.entities.Note;
import com.school.bulletin.entities.Professeur;
import com.school.bulletin.entities.Utilisateur;
import com.school.bulletin.repositories.EtudiantRepository;
import com.school.bulletin.repositories.MatiereRepository;
import com.school.bulletin.repositories.NoteRepository;
import com.school.bulletin.repositories.ProfesseurRepository;
import com.school.bulletin.repositories.UtilisateurRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.format.annotation.DateTimeFormat;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private MatiereRepository matiereRepository;

    @Autowired
    private EtudiantRepository etudiantRepository;

    @Autowired
    private ProfesseurRepository professeurRepository;

    @Autowired
    private NoteRepository noteRepository;

    // ============ DASHBOARD ============
    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        // Vérifier la session
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        
        List<Utilisateur> liste = utilisateurRepository.findAll();
        
        // Synchronisation automatique
        synchroniserEtudiantsEtProfesseurs();
        
        // Calculer les compteurs
        long etudiantsCount = liste.stream()
            .filter(u -> "ROLE_ETUDIANT".equals(u.getRole()))
            .count();
        
        long professeursCount = liste.stream()
            .filter(u -> "ROLE_PROFESSEUR".equals(u.getRole()))
            .count();
        
        long adminsCount = liste.stream()
            .filter(u -> "ROLE_ADMIN".equals(u.getRole()))
            .count();
        
        // Ajouter les attributs au modèle
        model.addAttribute("utilisateurs", liste);
        model.addAttribute("utilisateur", new Utilisateur());
        model.addAttribute("etudiantsCount", etudiantsCount);
        model.addAttribute("professeursCount", professeursCount);
        model.addAttribute("adminsCount", adminsCount);
        
        return "admin/dashboard"; 
    }

    // ============ AJOUTER/ENREGISTRER UTILISATEUR ============
    @PostMapping("/utilisateurs/sauvegarder")
    public String sauvegarderUtilisateur(@ModelAttribute Utilisateur user, 
                                        RedirectAttributes redirectAttributes,
                                        HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        
        try {
            // Vérifier les doublons d'email
            boolean emailExists;
            if (user.getId() == null) {
                // Nouvel utilisateur : vérifier si email existe déjà
                emailExists = utilisateurRepository.existsByEmail(user.getEmail());
            } else {
                // Modification : vérifier si email existe pour un autre utilisateur
                emailExists = utilisateurRepository.existsByEmailAndIdNot(user.getEmail(), user.getId());
            }
            
            if (emailExists) {
                redirectAttributes.addFlashAttribute("error", 
                    "L'adresse email '" + user.getEmail() + "' est déjà utilisée par un autre utilisateur !");
                return user.getId() == null ? 
                    "redirect:/admin/dashboard" : 
                    "redirect:/admin/utilisateurs/modifier/" + user.getId();
            }
            
            // Valider l'email
            if (!isValidEmail(user.getEmail())) {
                redirectAttributes.addFlashAttribute("error", 
                    "L'adresse email '" + user.getEmail() + "' n'est pas valide !");
                return user.getId() == null ? 
                    "redirect:/admin/dashboard" : 
                    "redirect:/admin/utilisateurs/modifier/" + user.getId();
            }
            
            // Si tout est OK, continuer avec la sauvegarde
            boolean isNewUser = user.getId() == null;
            
            // Si modification, garder l'ancien mot de passe si vide
            if (!isNewUser && user.getPassword().isEmpty()) {
                Utilisateur existingUser = utilisateurRepository.findById(user.getId()).orElse(null);
                if (existingUser != null) {
                    user.setPassword(existingUser.getPassword());
                }
            }
            
            utilisateurRepository.save(user);
            
            // Création automatique d'étudiant/professeur
            if (isNewUser) {
                if ("ROLE_ETUDIANT".equals(user.getRole())) {
                    Etudiant existingEtudiant = etudiantRepository.findByUtilisateur(user);
                    if (existingEtudiant == null) {
                        Etudiant etudiant = new Etudiant(user);
                        etudiant.setClasse("Non assigné");
                        etudiantRepository.save(etudiant);
                    }
                } else if ("ROLE_PROFESSEUR".equals(user.getRole())) {
                    Professeur existingProf = professeurRepository.findByUtilisateur(user);
                    if (existingProf == null) {
                        // Créer nouveau professeur
                        Professeur professeur = new Professeur(user);
                        professeurRepository.save(professeur);
                        System.out.println("DEBUG - Professeur créé pour: " + user.getNom());
                    }
                }
            }
            
            redirectAttributes.addFlashAttribute("success", "Utilisateur enregistré avec succès !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    // Méthode utilitaire pour valider l'email
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email != null && email.matches(emailRegex);
    }

    // ============ SUPPRIMER UTILISATEUR ============
    @GetMapping("/utilisateurs/supprimer/{id}")
    public String supprimerUtilisateur(@PathVariable Long id, 
                                      RedirectAttributes redirectAttributes,
                                      HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        
        try {
            Utilisateur user = utilisateurRepository.findById(id).orElse(null);
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "Utilisateur non trouvé !");
                return "redirect:/admin/dashboard";
            }
            
            // Supprimer l'étudiant ou professeur associé
            if ("ROLE_ETUDIANT".equals(user.getRole())) {
                Etudiant etudiant = etudiantRepository.findByUtilisateur(user);
                if (etudiant != null) {
                    etudiantRepository.delete(etudiant);
                }
            } else if ("ROLE_PROFESSEUR".equals(user.getRole())) {
                Professeur professeur = professeurRepository.findByUtilisateur(user);
                if (professeur != null) {
                    professeurRepository.delete(professeur);
                    System.out.println("DEBUG - Professeur supprimé: " + professeur.getNom());
                }
            }
            
            utilisateurRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Utilisateur supprimé avec succès !");
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("error", 
                "Impossible de supprimer : cet utilisateur est lié à d'autres données.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Erreur lors de la suppression : " + e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    // ============ MODIFIER UTILISATEUR ============
    @GetMapping("/utilisateurs/modifier/{id}")
    public String afficherFormulaireModification(@PathVariable Long id, 
                                                Model model, 
                                                HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        
        Utilisateur utilisateurAModifier = utilisateurRepository.findById(id).orElse(null);
        if (utilisateurAModifier == null) {
            return "redirect:/admin/dashboard";
        }
        
        model.addAttribute("utilisateur", utilisateurAModifier);
        return "admin/modifier_utilisateur";
    }

    // ============ GESTION DES MATIÈRES ============
    @GetMapping("/matieres")
    public String listeMatieres(Model model, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        
        model.addAttribute("matieres", matiereRepository.findAll());
        model.addAttribute("professeurs", utilisateurRepository.findByRole("ROLE_PROFESSEUR"));
        model.addAttribute("nouvelleMatiere", new Matiere());
        
        return "admin/matiere";
    }

    @PostMapping("/matieres/ajouter")
    public String ajouterMatiere(@RequestParam String nom,
                                @RequestParam int coefficient,
                                @RequestParam(required = false) Long professeurId,
                                RedirectAttributes redirectAttributes,
                                HttpSession session) {
        
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        
        try {
            System.out.println("=== AJOUT DE MATIÈRE ===");
            System.out.println("Nom: " + nom);
            System.out.println("Coefficient: " + coefficient);
            System.out.println("Professeur ID: " + professeurId);
            
            // 1. Nettoyer et valider le nom
            String nomMatiere = nom != null ? nom.trim() : "";
            if (nomMatiere.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", 
                    "Le nom de la matière est obligatoire !");
                return "redirect:/admin/matieres";
            }
            
            // 2. Vérifier si la matière existe déjà (insensible à la casse)
            List<Matiere> matieresExistantes = matiereRepository.findAll();
            boolean matiereExisteDeja = matieresExistantes.stream()
                .anyMatch(m -> m.getNom() != null && 
                              m.getNom().trim().equalsIgnoreCase(nomMatiere));
            
            if (matiereExisteDeja) {
                redirectAttributes.addFlashAttribute("error", 
                    "La matière '" + nomMatiere + "' existe déjà !");
                return "redirect:/admin/matieres";
            }
            
            // 3. Validation du coefficient
            if (coefficient < 1 || coefficient > 10) {
                redirectAttributes.addFlashAttribute("error", 
                    "Le coefficient doit être entre 1 et 10 !");
                return "redirect:/admin/matieres";
            }
            
            // 4. Créer la matière
            Matiere matiere = new Matiere();
            matiere.setNom(nomMatiere);
            matiere.setCoefficient(coefficient);
            
            // 5. Assigner le professeur si fourni
            if (professeurId != null && professeurId > 0) {
                Utilisateur professeur = utilisateurRepository.findById(professeurId)
                    .orElse(null);
                if (professeur != null && "ROLE_PROFESSEUR".equals(professeur.getRole())) {
                    matiere.setProfesseur(professeur);
                }
            }
            
            // 6. Sauvegarder
            matiereRepository.save(matiere);
            
            System.out.println("Matière sauvegardée avec ID: " + matiere.getId());
            
            redirectAttributes.addFlashAttribute("success", 
                "Matière '" + matiere.getNom() + "' ajoutée avec succès !");
            
        } catch (Exception e) {
            System.err.println("ERREUR lors de l'ajout de matière: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", 
                "Erreur: " + e.getMessage());
        }
        
        return "redirect:/admin/matieres";
    }
    
    @GetMapping("/matieres/modifier/{id}")
    public String afficherModifierMatiere(@PathVariable Long id, 
                                         Model model, 
                                         HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        
        Matiere matiere = matiereRepository.findById(id).orElse(null);
        if (matiere == null) {
            return "redirect:/admin/matieres";
        }
        
        model.addAttribute("matiere", matiere);
        model.addAttribute("professeurs", utilisateurRepository.findByRole("ROLE_PROFESSEUR"));
        model.addAttribute("action", "modifier");
        
        return "admin/modifier_matiere";
    }

    @PostMapping("/matieres/modifier/{id}")
    public String modifierMatiere(@PathVariable Long id,
                                 @RequestParam String nom,
                                 @RequestParam int coefficient,
                                 @RequestParam(required = false) Long professeurId,
                                 RedirectAttributes redirectAttributes,
                                 HttpSession session) {
        
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        
        try {
            Matiere matiere = matiereRepository.findById(id).orElse(null);
            if (matiere == null) {
                redirectAttributes.addFlashAttribute("error", "Matière non trouvée !");
                return "redirect:/admin/matieres";
            }
            
            // Vérifier si une autre matière avec le même nom existe
            List<Matiere> matieresExistantes = matiereRepository.findAll();
            boolean autreMatiereAvecMemeNom = matieresExistantes.stream()
                .anyMatch(m -> m.getId() != id && 
                              m.getNom() != null && 
                              m.getNom().trim().equalsIgnoreCase(nom.trim()));
            
            if (autreMatiereAvecMemeNom) {
                redirectAttributes.addFlashAttribute("error", 
                    "Une autre matière avec le nom '" + nom + "' existe déjà !");
                return "redirect:/admin/matieres/modifier/" + id;
            }
            
            // Mettre à jour les champs
            matiere.setNom(nom.trim());
            matiere.setCoefficient(coefficient);
            
            // Assigner le professeur
            if (professeurId != null && professeurId > 0) {
                Utilisateur professeur = utilisateurRepository.findById(professeurId)
                    .orElse(null);
                matiere.setProfesseur(professeur);
            } else {
                matiere.setProfesseur(null);
            }
            
            // Sauvegarder
            matiereRepository.save(matiere);
            
            redirectAttributes.addFlashAttribute("success", 
                "Matière '" + matiere.getNom() + "' modifiée avec succès !");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Erreur lors de la modification : " + e.getMessage());
        }
        
        return "redirect:/admin/matieres";
    }

    @GetMapping("/matieres/supprimer/{id}")
    public String supprimerMatiere(@PathVariable Long id,
                                  RedirectAttributes redirectAttributes,
                                  HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        
        try {
            matiereRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Matière supprimée avec succès !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/admin/matieres";
    }

   
@GetMapping("/notes")
public String gestionNotes(Model model, HttpSession session) {
    if (!isAdmin(session)) {
        return "redirect:/login";
    }
    
    // Récupérer toutes les notes
    List<Note> toutesNotes = noteRepository.findAll();
    
    // Calculer la moyenne générale sur 10
    double moyenneGenerale = 0.0;
    if (!toutesNotes.isEmpty()) {
        double somme = toutesNotes.stream()
            .mapToDouble(n -> {
                Double val = n.getValeur();
                // Conversion sur 10
                return val != null ? val / 2.0 : 0.0;
            })
            .sum();
        moyenneGenerale = somme / toutesNotes.size();
    }
    
    // Récupérer les classes distinctes
    List<String> classes = new ArrayList<>();
    try {
        classes = etudiantRepository.findAll().stream()
            .map(Etudiant::getClasse)
            .filter(classe -> classe != null && !classe.isEmpty())
            .distinct()
            .collect(Collectors.toList());
    } catch (Exception e) {
        System.out.println("Erreur récupération classes: " + e.getMessage());
    }
    
    model.addAttribute("toutesNotes", toutesNotes);
    model.addAttribute("nombreTotalNotes", toutesNotes.size());
    model.addAttribute("nombreEtudiants", etudiantRepository.count());
    model.addAttribute("nombreMatieres", matiereRepository.count());
    model.addAttribute("moyenneGenerale", Math.round(moyenneGenerale * 100.0) / 100.0);
    model.addAttribute("classes", classes);
    model.addAttribute("matieres", matiereRepository.findAll());
    
    return "admin/admin_notes";
}
    @GetMapping("/notes/ajouter")
    public String afficherFormulaireAjoutNote(Model model, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        
        model.addAttribute("note", new Note());
        model.addAttribute("etudiants", etudiantRepository.findAll());
        model.addAttribute("matieres", matiereRepository.findAll());
        model.addAttribute("semestres", new String[]{"Semestre 1", "Semestre 2"});
        
        return "admin/formulaire_note";
    }

    @PostMapping("/notes/ajouter")
    public String ajouterNote(@ModelAttribute Note note,
                             @RequestParam Long etudiantId,
                             @RequestParam Long matiereId,
                             @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateNote, 
                             RedirectAttributes redirectAttributes,
                             HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        
        try {
            Etudiant etudiant = etudiantRepository.findById(etudiantId).orElse(null);
            Matiere matiere = matiereRepository.findById(matiereId).orElse(null);
            
            if (etudiant == null || matiere == null) {
                redirectAttributes.addFlashAttribute("error", "Étudiant ou matière non trouvé !");
                return "redirect:/admin/notes/ajouter";
            }
            
            note.setEtudiant(etudiant);
            note.setMatiere(matiere);
            note.setDateNote(dateNote != null ? dateNote : new Date());
            noteRepository.save(note);
            
            redirectAttributes.addFlashAttribute("success", "Note ajoutée avec succès !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        
        return "redirect:/admin/notes";
    }

    // ============ MÉTHODES UTILITAIRES ============
    private void synchroniserEtudiantsEtProfesseurs() {
        // Synchroniser les étudiants
        utilisateurRepository.findByRole("ROLE_ETUDIANT").forEach(user -> {
            if (etudiantRepository.findByUtilisateur(user) == null) {
                Etudiant etudiant = new Etudiant(user);
                etudiant.setClasse("Non assigné");
                etudiantRepository.save(etudiant);
            }
        });
        
        // Synchroniser les professeurs
        List<Utilisateur> professeursUtilisateurs = utilisateurRepository.findByRole("ROLE_PROFESSEUR");
        for (Utilisateur user : professeursUtilisateurs) {
            Professeur professeur = professeurRepository.findByUtilisateur(user);
            if (professeur == null) {
                // Créer nouveau professeur
                professeur = new Professeur(user);
                professeurRepository.save(professeur);
                System.out.println("Créé professeur pour: " + user.getNom());
            } else {
                // Synchroniser le nom si nécessaire
                if (!user.getNom().equals(professeur.getNom())) {
                    professeur.setNom(user.getNom());
                    professeurRepository.save(professeur);
                    System.out.println("Mis à jour professeur: " + user.getNom());
                }
            }
        }
    }
    
    private boolean isAdmin(HttpSession session) {
        Utilisateur user = (Utilisateur) session.getAttribute("utilisateurConnecte");
        return user != null && "ROLE_ADMIN".equals(user.getRole());
    }
}