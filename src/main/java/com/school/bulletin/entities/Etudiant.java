package com.school.bulletin.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "etudiants")
public class Etudiant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String prenom;
    private String classe;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", referencedColumnName = "id", unique = true)
    private Utilisateur utilisateur;

    // Constructeur par défaut
    public Etudiant() {}

    // Constructeur avec Utilisateur - AMÉLIORÉ
    public Etudiant(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
        
        // Extraire nom et prénom du nom complet
        String nomComplet = utilisateur.getNom().trim();
        String[] parts = nomComplet.split(" ");
        
        if (parts.length >= 2) {
            // Dernier mot = nom
            this.nom = parts[parts.length - 1];
            
            // Tous les autres mots = prénom
            StringBuilder prenomBuilder = new StringBuilder();
            for (int i = 0; i < parts.length - 1; i++) {
                if (i > 0) prenomBuilder.append(" ");
                prenomBuilder.append(parts[i]);
            }
            this.prenom = prenomBuilder.toString();
        } else {
            // Si un seul mot, c'est le nom
            this.nom = nomComplet;
            this.prenom = "À définir";
        }
        
        this.classe = "Non assigné";
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { 
        if (nom != null && !nom.isEmpty()) {
            return nom;
        } else if (utilisateur != null) {
            // Extraire le nom de l'utilisateur
            String nomComplet = utilisateur.getNom();
            String[] parts = nomComplet.split(" ");
            return parts.length > 0 ? parts[parts.length - 1] : nomComplet;
        }
        return null;
    }
    
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { 
        if (prenom != null && !prenom.isEmpty() && !prenom.equals("Nouveau")) {
            return prenom;
        } else if (utilisateur != null) {
            // Extraire le prénom de l'utilisateur
            String nomComplet = utilisateur.getNom();
            String[] parts = nomComplet.split(" ");
            if (parts.length >= 2) {
                StringBuilder prenomBuilder = new StringBuilder();
                for (int i = 0; i < parts.length - 1; i++) {
                    if (i > 0) prenomBuilder.append(" ");
                    prenomBuilder.append(parts[i]);
                }
                return prenomBuilder.toString();
            }
        }
        return prenom;
    }
    
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getClasse() { return classe; }
    public void setClasse(String classe) { this.classe = classe; }

    public Utilisateur getUtilisateur() { return utilisateur; }
    
    public void setUtilisateur(Utilisateur utilisateur) { 
        this.utilisateur = utilisateur;
        
        // Mettre à jour le nom et prénom si nécessaire
        if ((this.nom == null || this.nom.isEmpty() || this.nom.equals("Nouveau")) && utilisateur != null) {
            String nomComplet = utilisateur.getNom();
            String[] parts = nomComplet.split(" ");
            
            if (parts.length >= 2) {
                this.nom = parts[parts.length - 1];
                StringBuilder prenomBuilder = new StringBuilder();
                for (int i = 0; i < parts.length - 1; i++) {
                    if (i > 0) prenomBuilder.append(" ");
                    prenomBuilder.append(parts[i]);
                }
                this.prenom = prenomBuilder.toString();
            } else {
                this.nom = nomComplet;
            }
        }
    }
    
    // Méthode pour avoir le nom complet
    public String getNomComplet() {
        return (getPrenom() != null ? getPrenom() + " " : "") + getNom();
    }
}