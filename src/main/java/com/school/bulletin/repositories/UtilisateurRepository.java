package com.school.bulletin.repositories;

import java.util.ArrayList;
import java.util.List;
import com.school.bulletin.entities.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    Optional<Utilisateur> findByEmail(String email);
    List<Utilisateur> findByRole(String role);
    
    // Nouvelle méthode pour vérifier si un email existe déjà
    boolean existsByEmail(String email);
    
    // Méthode pour vérifier si un email existe pour un autre utilisateur
    @Query("SELECT COUNT(u) > 0 FROM Utilisateur u WHERE u.email = :email AND u.id != :id")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("id") Long id);
}