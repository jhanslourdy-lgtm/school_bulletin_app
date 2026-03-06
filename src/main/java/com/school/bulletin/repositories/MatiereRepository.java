package com.school.bulletin.repositories;

import com.school.bulletin.entities.Matiere;
import com.school.bulletin.entities.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface MatiereRepository extends JpaRepository<Matiere, Long> {
    List<Matiere> findByProfesseur(Utilisateur professeur);
    
    @Query("SELECT m FROM Matiere m WHERE m.professeur IS NULL")
    List<Matiere> findWithoutProfesseur();
    
    long countByProfesseur(Utilisateur professeur);
    
    // Vérifier si une matière avec ce nom existe déjà
    boolean existsByNom(String nom);
    
    // Vérifier si une matière avec ce nom existe pour un autre ID
    @Query("SELECT COUNT(m) > 0 FROM Matiere m WHERE LOWER(TRIM(m.nom)) = LOWER(TRIM(:nom)) AND m.id != :id")
    boolean existsByNomAndIdNot(@Param("nom") String nom, @Param("id") Long id);
    
    // Recherche insensible à la casse
    @Query("SELECT m FROM Matiere m WHERE LOWER(TRIM(m.nom)) = LOWER(TRIM(:nom))")
    Optional<Matiere> findByNomIgnoreCase(@Param("nom") String nom);
}