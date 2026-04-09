package com.br.zetta.fire.data.entity;


import com.br.zetta.fire.data.dto.request.UserRequestDTO;
import com.br.zetta.fire.data.entity.enums.UserRole;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "users")
@NoArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID idUser;

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

    @Column(name="userRole")
    private UserRole userRole;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "idAddress", referencedColumnName = "idAddress")
    private Address address;

    @ManyToMany(mappedBy = "userList")
    private List<Alert> alertList = new ArrayList<>();



    @Builder
    public User(UserRequestDTO userRequestDTO, String password) {
        this.name = userRequestDTO.name();
        this.email = userRequestDTO.email();
        this.password = userRequestDTO.password();
        this.phone = userRequestDTO.phone();
        this.password = password;
        this.createdAt = LocalDate.now();

        Address addr = new Address();
        addr.setCep(userRequestDTO.address().cep());
        addr.setCity(userRequestDTO.address().city());
        addr.setStreet(userRequestDTO.address().street());
        addr.setNumber(userRequestDTO.address().number());
        addr.setDistrict(userRequestDTO.address().district());
        addr.setState(userRequestDTO.address().state());

        this.address = addr;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (this.userRole == UserRole.ADMIN) {
            return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_USER"));
        } else {
            return List.of(new SimpleGrantedAuthority("ROLE_USER"));
        }
    }
    @Override
    public String getUsername() {
        throw new UnsupportedOperationException("Unimplemented method 'getUsername'");
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }


    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }


    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }


    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
