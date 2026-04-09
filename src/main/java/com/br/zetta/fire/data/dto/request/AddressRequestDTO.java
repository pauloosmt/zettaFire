package com.br.zetta.fire.data.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AddressRequestDTO(
        @NotBlank(message = "CEP is required")
        @Pattern(regexp = "\\d{8}", message = "Invalid CEP")
        String cep,

        @NotBlank(message = "Street is required")
        String street,

        String number,

        @NotBlank(message = "District is required")
        String district,

        @NotBlank(message = "City is required")
        String city,

        @NotBlank(message = "State is required")
        String state

) {
}
