package com.school.bulletin.repositories;

import com.school.bulletin.entities.Etudiant;
import com.school.bulletin.entities.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EtudiantRepository extends JpaRepository<Etudiant, Long> {
    // Trouver par utilisateur
    Etudiant findByUtilisateur(Utilisateur utilisateur);
    
    // Trouver par ID utilisateur
    @Query("SELECT e FROM Etudiant e WHERE e.utilisateur.id = :userId")
    Etudiant findByUtilisateurId(@Param("userId") Long userId);
    
    // Tous les étudiants ordonnés
    @Query("SELECT e FROM Etudiant e ORDER BY e.nom, e.prenom")
    List<Etudiant> findAllOrdered();
    
    // Trouver par nom (recherche)
    List<Etudiant> findByNomContainingIgnoreCase(String nom);
    
    // Trouver par classe
    List<Etudiant> findByClasse(String classe);
    
    // Compter les étudiants
    long count();
}