package com.br.zetta.fire.data.entity;


import com.br.zetta.fire.data.dto.request.UserRequestDTO;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private Long idUser;

    @Column(name="name", nullable = false)
    private String name;

    @Column(name="email", unique = true, nullable = false)
    private String email;

    @Column(name="password", nullable = false)
    private String password;

    @Column(name="phone",nullable = false)
    private String phone;

    @Column(name = "createdAt")
    private LocalDate createdAt;

    @Builder
    public User(UserRequestDTO userRequestDTO) {
        this.name = userRequestDTO.name();
        this.email = userRequestDTO.email();
        this.password = userRequestDTO.password();
        this.phone = userRequestDTO.phone();

    }


}
