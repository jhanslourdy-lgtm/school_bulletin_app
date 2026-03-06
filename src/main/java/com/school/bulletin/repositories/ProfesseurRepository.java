package com.school.bulletin.repositories;

import com.school.bulletin.entities.Professeur;
import com.school.bulletin.entities.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfesseurRepository extends JpaRepository<Professeur, Long> {
    // Trouver par utilisateur
    Professeur findByUtilisateur(Utilisateur utilisateur);
    
    // Trouver par ID utilisateur
    @Query("SELECT p FROM Professeur p WHERE p.utilisateur.id = :userId")
    Professeur findByUtilisateurId(@Param("userId") Long userId);
    
    // Tous les professeurs ordonnés
    @Query("SELECT p FROM Professeur p ORDER BY p.nom")
    List<Professeur> findAllOrdered();
    
    // Trouver par spécialité
    List<Professeur> findBySpecialiteContainingIgnoreCase(String specialite);
}