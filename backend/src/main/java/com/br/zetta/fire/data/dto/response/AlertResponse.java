package com.br.zetta.fire.data.dto.response;

public record AlertResponse(
        String message,
        String status,
        String timestamp
) {
}
