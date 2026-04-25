package com.br.zetta.fire.repository;

import com.br.zetta.fire.data.entity.FireEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FireEventRepository extends JpaRepository<FireEvent, UUID> {
    @Query("SELECT COUNT(f) FROM FireEvent f WHERE " +
            "(:city IS NULL OR f.city = :city) AND " +
            "(f.startTime >= :startDate)")
    Long countByLocationAndPeriod(
            @Param("city") String city,
            @Param("startDate") LocalDateTime startDate
    );

    boolean existsByIdFocoBdq(Long idFocoBdq);

    Optional<FireEvent> findByIdFocoBdq(Long idFocoBdq);

    Page<FireEvent> findAll(Pageable pageable);
}
