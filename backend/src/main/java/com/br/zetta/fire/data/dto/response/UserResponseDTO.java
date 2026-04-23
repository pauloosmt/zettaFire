package com.br.zetta.fire.data.dto.response;

import com.br.zetta.fire.data.entity.Address;
import com.br.zetta.fire.data.entity.User;

import java.util.UUID;

public record UserResponseDTO(
    UUID idUser,
    String name,
    String email,
    String phone,
    Address address
    ){
    public UserResponseDTO(User user) {
        this(user.getIdUser(), user.getName(), user.getEmail(), user.getPhone(), user.getAddress());
    }
}
