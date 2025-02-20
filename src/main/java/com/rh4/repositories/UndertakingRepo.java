package com.rh4.repositories;

import com.rh4.entities.Intern;
import com.rh4.entities.Undertaking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UndertakingRepo extends JpaRepository<Undertaking, Long> {
    Optional<Undertaking> findTopByOrderByCreatedAtDesc();

//    Optional<Undertaking> findByInternId(String internId);

    // Fetch the latest undertaking form
//    @Query("SELECT u FROM Undertaking u ORDER BY u.createdAt DESC")
//    Optional<Undertaking> findLatestForm();

//    Optional<Undertaking> findByInternId(String internId); // Corrected method
@Query("SELECT u FROM Undertaking u ORDER BY u.createdAt DESC")
List<Undertaking> findLatestFormSorted(); // Returns a list, take the first one

    @Query("SELECT u FROM Undertaking u WHERE u.intern = :internId")
    Optional<Undertaking> findByInternId(@Param("internId") String internId);

    boolean existsByIntern(String internId);

    @Query("SELECT u.content FROM Undertaking u WHERE u.content IS NOT NULL AND u.content <> '' ORDER BY u.createdAt DESC LIMIT 1")
    String findLatestUndertakingContent();
}
