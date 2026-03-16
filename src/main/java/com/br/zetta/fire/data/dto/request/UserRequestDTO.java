package com.br.zetta.fire.data.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRequestDTO(

        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 6, message="The password must be at least 6 characters long")
        String password,

        @NotBlank(message = "Phone is required")
        @Pattern(regexp = "\\(\\d{2}\\)\\d{5}-\\d{4}", message = "Invalid phone. Use (XX)XXXXX-XXXX")
        String phone
) {
}
