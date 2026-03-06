
package com.school.bulletin.repositories;

import com.school.bulletin.entities.Etudiant;
import com.school.bulletin.entities.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    
    List<Note> findByEtudiant(Etudiant etudiant);
    List<Note> findByMatiereId(Long matiereId);
    List<Note> findByEtudiantClasse(String classe);
    
    // Vérifier si une note existe déjà pour un étudiant, matière et semestre
    @Query("SELECT COUNT(n) > 0 FROM Note n WHERE n.etudiant.id = :etudiantId AND n.matiere.id = :matiereId AND n.semestre = :semestre")
    boolean existsByEtudiantAndMatiereAndSemestre(@Param("etudiantId") Long etudiantId,
                                                 @Param("matiereId") Long matiereId,
                                                 @Param("semestre") String semestre);
    
    // Trouver une note existante
    @Query("SELECT n FROM Note n WHERE n.etudiant.id = :etudiantId AND n.matiere.id = :matiereId AND n.semestre = :semestre")
    Optional<Note> findByEtudiantAndMatiereAndSemestre(@Param("etudiantId") Long etudiantId,
                                                      @Param("matiereId") Long matiereId,
                                                      @Param("semestre") String semestre);
    
    // Vérifier l'existence pour une note spécifique (pour modification)
    @Query("SELECT COUNT(n) > 0 FROM Note n WHERE n.etudiant.id = :etudiantId AND n.matiere.id = :matiereId AND n.semestre = :semestre AND n.id != :noteId")
    boolean existsByEtudiantAndMatiereAndSemestreExcludingId(@Param("etudiantId") Long etudiantId,
                                                            @Param("matiereId") Long matiereId,
                                                            @Param("semestre") String semestre,
                                                            @Param("noteId") Long noteId);
}