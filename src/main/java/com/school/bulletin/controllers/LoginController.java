
package com.school.bulletin.controllers;

import com.school.bulletin.entities.Utilisateur;
import com.school.bulletin.repositories.UtilisateurRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class LoginController {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @GetMapping("/login")
    public String showLoginPage() {
        return "login"; // Retourne login.html
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, 
                        @RequestParam String password, 
                        HttpSession session, 
                        Model model) {
        
        System.out.println("=== DEBUG LOGIN ===");
        System.out.println("Username reçu: " + username);
        System.out.println("Password reçu: " + password);
        
        // On cherche l'utilisateur par son email (ou username)
        Utilisateur user = utilisateurRepository.findByEmail(username).orElse(null);

        // Vérification en texte clair
        if (user != null && user.getPassword().equals(password)) {
            System.out.println("Login réussi pour: " + user.getEmail() + " - Rôle: " + user.getRole());
            session.setAttribute("utilisateurConnecte", user);
            
            // Redirection selon le rôle
            if ("ROLE_ADMIN".equals(user.getRole())) {
                return "redirect:/admin/dashboard";
            }
            else if ("ROLE_ETUDIANT".equals(user.getRole())) {
                System.out.println("Redirection vers /etudiant/notes");
                return "redirect:/etudiant/notes";
            }
            else if ("ROLE_PROFESSEUR".equals(user.getRole())) {
                return "redirect:/professeur/saisie";
            }
        }

        // Si ça échoue
        System.out.println("Login échoué pour: " + username);
        model.addAttribute("error", "Email ou mot de passe incorrect");
        return "login";
    }
    
    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

@GetMapping("/logout")
public String logout(HttpSession session) {
    session.invalidate();
    return "redirect:/login";
}
    
}