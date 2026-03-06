package com.school.bulletin.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "professeurs")
public class Professeur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nom;
    private String specialite;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", referencedColumnName = "id", unique = true)
    private Utilisateur utilisateur;
    
    // Constructeurs
    public Professeur() {}
    
    public Professeur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
        this.nom = utilisateur.getNom();
        this.specialite = "À définir";
    }
    
    // Méthode pour synchroniser
    public void synchroniserAvecUtilisateur() {
        if (utilisateur != null && (nom == null || !nom.equals(utilisateur.getNom()))) {
            nom = utilisateur.getNom();
        }
    }
    
    // Getters et Setters manuels
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getNom() {
        return nom;
    }
    
    public void setNom(String nom) {
        this.nom = nom;
    }
    
    public String getSpecialite() {
        return specialite;
    }
    
    public void setSpecialite(String specialite) {
        this.specialite = specialite;
    }
    
    public Utilisateur getUtilisateur() {
        return utilisateur;
    }
    
    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
        synchroniserAvecUtilisateur();
    }
    
    // Méthode utilitaire pour avoir le nom complet
    public String getNomComplet() {
        return nom;
    }
}