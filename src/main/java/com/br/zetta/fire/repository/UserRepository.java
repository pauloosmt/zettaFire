package com.br.zetta.fire.repository;

import com.br.zetta.fire.data.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    @Query(value = "SELECT u.* FROM users u " +
            "JOIN address a ON u.id_address = a.id_address " +
            "WHERE ST_DWithin(a.geom, ST_SetSRID(ST_Point(:lon, :lat), 4326)::geography, :radius)",
            nativeQuery = true)
    List<User> findUsersInRadius(@Param("lat") double lat, @Param("lon") double lon, @Param("radius") double radius);

    @Query(value = """
    SELECT u.* FROM users u
    JOIN address a ON u.id_address = a.id_address
    WHERE ST_DWithin(
        a.geom, 
        (SELECT geom FROM fire_event WHERE id_fire_event = :fireEventId),
        (SELECT radius_of_risk FROM fire_event WHERE id_fire_event = :fireEventId)
    )
    """, nativeQuery = true)
    List<User> findUsersAtRisk(@Param("fireEventId") UUID fireEventId);

    UserDetails findByEmail(String email);
}
