package com.br.zetta.fire.data.dto.response;

import com.br.zetta.fire.data.entity.FireEvent;

import java.time.LocalDateTime;
import java.util.UUID;

public record FireEventResponseDTO(
        UUID id,
        String city,
        Double latitude,
        Double longitude,
        String status_fire,
        Double fire_risk,
        LocalDateTime start_time
) {
    public FireEventResponseDTO(FireEvent entity) {
        this(
                entity.getIdFireEvent(),
                entity.getCity(),
                entity.getLatitude(),
                entity.getLongitude(),
                String.valueOf(entity.getStatusFire()),
                entity.getFireRisk(),
                entity.getStartTime()
        );
    }
}
