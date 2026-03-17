package com.br.zetta.fire.data.dto.request;


import com.br.zetta.fire.data.entity.enums.StatusFire;
import jakarta.validation.constraints.NotBlank;

public record FireEventRequestDTO(
        String description,

        @NotBlank(message = "Latitude is required")
        Double latitude,

        @NotBlank(message = "Longitude is required")
        Double longitude,

        @NotBlank(message = "Radius of Risk is required")
        Long radiusOfDisk,

        @NotBlank(message = "Status is required")
        StatusFire statusFire
) {
}
