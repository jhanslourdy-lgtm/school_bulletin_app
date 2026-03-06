/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.school.bulletin.entities;

/**
 *
 * @author USER
 */
import java.util.Date;
import jakarta.persistence.*;
import lombok.*;
@Entity
@Data
@NoArgsConstructor 
@AllArgsConstructor
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Double valeur; // La note sur 20
    private String semestre; // Ex: "Semestre 1"
     private Date dateNote; 
     
     
     
    public Matiere getMatiere() {
        return matiere;
    }

    public void setMatiere(Matiere matiere) {
        this.matiere = matiere;
    }

    public double getValeur() {
        return valeur;
    }

    public void setValeur(double valeur) {
        this.valeur = valeur;
    }
    // Dans Note.java, ajoutez cette méthode
public Double getValeurSur10() {
    return valeur != null ? valeur / 2.0 : null;
}

public String getValeurSur10Formatee() {
    return valeur != null ? String.format("%.1f/10", valeur / 2.0) : "N/A";
}
     public Date getDateNote() { return dateNote; }
    public void setDateNote(Date dateNote) { this.dateNote = dateNote; }
    @ManyToOne
    private Etudiant etudiant;

    @ManyToOne
    private Matiere matiere;
}